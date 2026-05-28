package com.senai.monsai.application.service;

import com.senai.monsai.application.dto.AsiloCreateDTO; // ajuste o nome do DTO se diferente
import com.senai.monsai.domain.entity.Asilo;
import com.senai.monsai.domain.exception.RecursoDuplicadoException;
import com.senai.monsai.domain.exception.RecursoNaoEncontradoException;
import com.senai.monsai.domain.exception.RegraNegocioException;
import com.senai.monsai.domain.repository.AsiloRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para AsiloService (AACI-112).
 * <p>
 * Cobre os cenários básicos de criação e busca de asilo,
 * isolando completamente o banco de dados via mock do repositório.
 *
 */

@ExtendWith(MockitoExtension.class)
public class AsiloServiceTest {

    @Mock
    private AsiloRepository asiloRepository;

    @InjectMocks
    private AsiloService asiloService;

    // =========================================================
    // EDGE 1: Criar asilo com sucesso
    // =========================================================

    @Test
    @DisplayName("AACI-112: Deve criar asilo com sucesso")
    void criaAsiloComSucesso() {
        // GIVEN: DTO com os dados mínimos para cadastro de um asilo
        var dto = new AsiloCreateDTO(
                "Asilo das Flores",
                "12.345.678/0001-99",
                "Rua das Mariolas, 123"
        );

        when(asiloRepository.existsByCnpj(dto.cnpj()))
                .thenReturn(false);
        when(asiloRepository.save(any(Asilo.class))).thenAnswer(i -> i.getArguments()[0]);


        // WHEN & THEN
        assertDoesNotThrow(() -> asiloService.criarAsilo(dto));

        // THEN: O repositório deve ter recebido exatamente uma chamada de save
        verify(asiloRepository, times(1)).save(any(Asilo.class));
    }

    // =========================================================
    // EDGE 2: Criar Asilo com CNPJ duplicado
    // =========================================================
    @Test
    @DisplayName("AACI-112: Deve lançar RecursoDuplicadoException ao criar asilo com CNPJ já existente")
    void criaAsiloComCnpjDuplicado() {
        // GIVEN: CNPJ já existe no banco
        var dto = new AsiloCreateDTO(
                "Outro Asilo",
                "12.345.678/0001-99",
                "Rua X, 1"
        );

        when(asiloRepository.existsByCnpj(dto.cnpj()))
                .thenReturn(true);

        // WHEN & THEN: Captura a exceção de uma vez só e valida
        RecursoDuplicadoException exception = assertThrows(
                RecursoDuplicadoException.class,
                () -> asiloService.criarAsilo(dto)
        );

        assertEquals("Já existe um asilo cadastrado com este CNPJ.", exception.getMessage());

        // Nenhum save deve ocorrer
        verify(asiloRepository, never()).save(any(Asilo.class));
    }

    // =========================================================
    // EDGE 3: Deve retornar uma lista com todos os asilos
    // =========================================================

    @Test
    @DisplayName("AACI-112: Deve retornar lista de todos os asilos")
    void listarAsilosComSucesso() {
        // GIVEN: Dois asilos cadastrados no banco
        Asilo asilo1 = new Asilo();
        Asilo asilo2 = new Asilo();
        when(asiloRepository.findAll())
                .thenReturn(List.of(asilo1, asilo2));

        // WHEN
        List<Asilo> result = asiloService.listarTodos();

        // THEN
        assertEquals(2, result.size());
        verify(asiloRepository, times(1)).findAll();
    }

    // =========================================================
    // EDGE 4: Deve atualizar um asilo com sucesso
    // =========================================================

    @Test
    @DisplayName("AACI-112: Deve atualizar um asilo com êxito")
    void atualizarAsiloComExito() {
        // GIVEN: Asilo existente, ativo e com o mesmo CNPJ (não troca CNPJ)
        Asilo asiloExistente = new Asilo();
        asiloExistente.setId(1L);
        asiloExistente.setCnpj("12.345.678/0001-99");
        asiloExistente.setAtivo(true);

        var dto = new AsiloCreateDTO("Nome Atualizado",
                "12.345.678/0001-99",
                "Rua Nova, 200"
        );

        when(asiloRepository.findById(1L))
                .thenReturn(Optional.of(asiloExistente));
        when(asiloRepository.save(any(Asilo.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        // WHEN & THEN
        assertDoesNotThrow(() -> asiloService.atualizarAsilo(1L, dto));
        verify(asiloRepository, times(1)).save(any(Asilo.class));
    }

    // =========================================================
    // EDGE 5: Deve lançar exceção ao tentar atualizar um asilo inativo
    // =========================================================
    @Test
    @DisplayName("AACI-112: Deve lançar RegraNegocioException ao tentar atualizar asilo inativo")
    void deveLancarExcecaoAoAtualizarAsiloInativo() {
        // GIVEN: Asilo existe mas está inativo
        Asilo asiloInativo = new Asilo();
        asiloInativo.setId(1L);
        asiloInativo.setCnpj("12.345.678/0001-99");
        asiloInativo.setAtivo(false);

        var dto = new AsiloCreateDTO(
                "Qualquer Nome",
                "12.345.678/0001-99",
                "Qualquer Endereço"
        );

        when(asiloRepository.findById(1L))
                .thenReturn(Optional.of(asiloInativo));

        // WHEN & THEN: Tenta atualizar e já captura a exceção
        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> asiloService.atualizarAsilo(1L, dto)
        );

        // Verifica a mensagem correta que o seu Service lança
        assertEquals("Não é possível editar um asilo que está inativo.", exception.getMessage());

        verify(asiloRepository, never()).save(any(Asilo.class));
    }

    // =========================================================
    // EDGE 6: Deve falhar ao atualizar com cnpj de outro asilo
    // =========================================================

    @Test
    @DisplayName("AACI-112: Deve lançar RecursoDuplicadoException ao atualizar com CNPJ de outro asilo")
    void deveFalharAoAtualizarComCnpjDeOutroAsilo() {
        // GIVEN: Asilo ativo com CNPJ "AAA", tentando mudar para "BBB" que já pertence a outro asilo
        Asilo asiloExistente = new Asilo();
        asiloExistente.setId(1L);
        asiloExistente.setCnpj("11.111.111/0001-11");
        asiloExistente.setAtivo(true);

        // DTO com CNPJ diferente que já está em uso por outro asilo
        var dto = new AsiloCreateDTO(
                "Nome",
                "22.222.222/0002-22",
                "Rua Y, 10"
        );

        when(asiloRepository.findById(1L))
                .thenReturn(Optional.of(asiloExistente));
        when(asiloRepository.existsByCnpj("22.222.222/0002-22"))
                .thenReturn(true);

        // WHEN & THEN
        assertThrows(RecursoDuplicadoException.class,
                () -> asiloService.atualizarAsilo(1L, dto));
        verify(asiloRepository, never()).save(any(Asilo.class));

    }

    // =========================================================
    // EDGE 7: Deve lançar exceção quando tentar atualizar um asilo inexistente
    // =========================================================

    @Test
    @DisplayName("AACI-112: Deve lançar RecursoNaoEncontradoException ao atualizar asilo inexistente")
    void deveFalharAoAtualizarAsiloInexistente() {
        // GIVEN: Nenhum asilo com ID 999 existe
        when(asiloRepository.findById(999L))
                .thenReturn(Optional.empty());

        var dto = new AsiloCreateDTO(
                "Nome",
                "12.345.678/0001-99",
                "Rua Z, 5"
        );

        // WHEN & THEN
        assertThrows(RecursoNaoEncontradoException.class, () -> asiloService.atualizarAsilo(999L, dto));
    }

    // =========================================================
    // EDGE 7: Deve inativar um asilo com sucesso
    // =========================================================

    @Test
    @DisplayName("AACI-112: Deve inativar asilo com sucesso")
    void inativarAsiloComSucesso() {
        // GIVEN: Asilo existe e está ativo
        Asilo asiloAtivo = new Asilo();
        asiloAtivo.setId(1L);
        asiloAtivo.setAtivo(true);

        when(asiloRepository.findById(1L)).thenReturn(Optional.of(asiloAtivo));

        // WHEN
        assertDoesNotThrow(() -> asiloService.inativarAsilo(1L));

        // THEN: O asilo deve ter sido salvo como ativo = false
        verify(asiloRepository, times(1)).save(argThat(a -> !a.isAtivo()));
    }

    // =========================================================
    // EDGE 8: Deve lançar exceção ao tentar inativar um asilo inativo
    // =========================================================

    @Test
    @DisplayName("AACI-112: Deve lançar exceção ao inativar asilo já inativo")
    void deveFalharAoInativarAsiloJaInativo() {
        // GIVEN: Asilo já inativo
        Asilo asiloInativo = new Asilo();
        asiloInativo.setId(1L);
        asiloInativo.setAtivo(false);

        when(asiloRepository.findById(1L)).thenReturn(Optional.of(asiloInativo));

        // WHEN & THEN
        assertThrows(RegraNegocioException.class,
                () -> asiloService.inativarAsilo(1L));

        verify(asiloRepository, never()).save(any(Asilo.class));
    }

    // =========================================================
    // EDGE 9: Deve lançar exceção ao inativar um que não existe no sistema
    // =========================================================

    @Test
    @DisplayName("AACI-112: Deve lançar RecursoNaoEncontradoException ao inativar asilo inexistente")
    void deveFalharAoInativarAsiloInexistente() {
        // GIVEN
        when(asiloRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(RecursoNaoEncontradoException.class, () -> asiloService.inativarAsilo(99L));
    }

}
