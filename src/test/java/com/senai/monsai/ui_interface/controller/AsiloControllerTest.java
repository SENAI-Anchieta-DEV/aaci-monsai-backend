package com.senai.monsai.ui_interface.controller;

import com.senai.monsai.application.dto.AsiloCreateDTO;
import com.senai.monsai.application.service.AsiloService;
import com.senai.monsai.domain.entity.Asilo;
import com.senai.monsai.domain.exception.RecursoDuplicadoException;
import com.senai.monsai.domain.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AsiloControllerTest {

    @Mock
    private AsiloService asiloService;

    @InjectMocks
    private AsiloController asiloController;

    // =========================================================
    // EDGE CASE 1: Deve criar asilo com sucesso (Retornando objeto Asilo)
    // =========================================================
    @Test
    @DisplayName("AACI-114: Cadastro de asilo com asilo e serial")
    void deveCriarAsiloComSucesso() {
        // GIVEN
        AsiloCreateDTO dto = new AsiloCreateDTO("Asilo Anchieta", "12.345.678/0001-99", "Rua das Flores, 123");
        Asilo asiloSalvo = new Asilo();
        asiloSalvo.setId(1L);
        asiloSalvo.setNome(dto.nome());

        when(asiloService.criarAsilo(any(AsiloCreateDTO.class))).thenReturn(asiloSalvo);

        // WHEN
        ResponseEntity<Asilo> response = asiloController.criarAsilo(dto);

        // THEN
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Asilo Anchieta", response.getBody().getNome());
        verify(asiloService, times(1)).criarAsilo(dto);
    }

    // =========================================================
    // EDGE CASE 2: Deve lançar RecursoDuplicadoException ao criar CNPJ duplicado
    // =========================================================
    @Test
    @DisplayName("AACI-114: Falha no cadastro por CNPJ duplicado")
    void deveFalharAoCriarCnpjDuplicado() {
        // GIVEN
        AsiloCreateDTO dto = new AsiloCreateDTO("Asilo Duplicado", "11.222.333/0001-44", "Endereço X");
        String mensagemEsperada = "Já existe um asilo cadastrado com este CNPJ.";

        when(asiloService.criarAsilo(dto)).thenThrow(new RecursoDuplicadoException(mensagemEsperada));

        // WHEN & THEN
        RecursoDuplicadoException exception = assertThrows(
                RecursoDuplicadoException.class,
                () -> asiloController.criarAsilo(dto)
        );

        assertEquals(mensagemEsperada, exception.getMessage());
    }

    // =========================================================
    // EDGE CASE 3: Deve atualizar asilo com sucesso (Retornando objeto Asilo)
    // =========================================================
    @Test
    @DisplayName("AACI-114: Atualização de asilo com dados válidos")
    void deveAtualizarAsiloComSucesso() {
        // GIVEN
        Long asiloId = 1L;
        AsiloCreateDTO dto = new AsiloCreateDTO("Novo Nome", "12.345.678/0001-99", "Endereço Novo");
        Asilo asiloAtualizado = new Asilo();
        asiloAtualizado.setId(asiloId);
        asiloAtualizado.setNome(dto.nome());

        when(asiloService.atualizarAsilo(eq(asiloId), any(AsiloCreateDTO.class))).thenReturn(asiloAtualizado);

        // WHEN
        ResponseEntity<Asilo> response = asiloController.atualizarAsilo(asiloId, dto);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody(), "O corpo da resposta não deve ser nulo");
        assertEquals("Novo Nome", response.getBody().getNome());
    }

    // =========================================================
    // EDGE CASE 4: Deve lançar RecursoNaoEncontradoException ao atualizar ID inexistente
    // =========================================================
    @Test
    @DisplayName("AACI-114: Falha na atualização de asilo inexistente")
    void deveFalharAoAtualizarAsiloInexistente() {
        // GIVEN
        Long asiloId = 999L;
        AsiloCreateDTO dto = new AsiloCreateDTO("Nome", "00.000.000/0000-00", "Endereço");
        String mensagemEsperada = "Asilo não encontrado.";

        when(asiloService.atualizarAsilo(eq(asiloId), any(AsiloCreateDTO.class)))
                .thenThrow(new RecursoNaoEncontradoException(mensagemEsperada));

        // WHEN & THEN
        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> asiloController.atualizarAsilo(asiloId, dto)
        );

        assertEquals(mensagemEsperada, exception.getMessage());
    }

    // =========================================================
    // EDGE CASE 5: Deve inativar asilo com sucesso (204 No Content)
    // =========================================================
    @Test
    @DisplayName("AACI-114: Inativação de asilo existente")
    void deveInativarAsiloComSucesso() {
        // GIVEN
        Long asiloId = 1L;

        // WHEN
        ResponseEntity<Void> response = asiloController.inativarAsilo(asiloId);

        // THEN
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(asiloService, times(1)).inativarAsilo(asiloId);
    }
}