package com.senai.monsai.ui_interface.controller;

import com.senai.monsai.application.dto.IdosoCreateDTO;
import com.senai.monsai.application.dto.IdosoUpdateDTO;
import com.senai.monsai.application.service.IdosoService;
import com.senai.monsai.domain.entity.Idoso;
import com.senai.monsai.domain.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IdosoControllerTest {

    @Mock
    private IdosoService idosoService;

    @InjectMocks
    private IdosoController idosoController;

    // =========================================================
    // EDGE CASE 1: Deve criar idoso com sucesso (201 Created)
    // =========================================================
    @Test
    @DisplayName("AACI-114: Cadastro de idoso retorna 201 e objeto criado")
    void deveCriarIdosoComSucesso() {
        IdosoCreateDTO dto = new IdosoCreateDTO("Maria", "123", "m@email.com", "MON-99", 1L);
        Idoso idosoCriado = new Idoso();
        idosoCriado.setNome("Maria");

        when(idosoService.criarIdoso(any(IdosoCreateDTO.class))).thenReturn(idosoCriado);

        ResponseEntity<Idoso> response = idosoController.criarIdoso(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody(), "O corpo da resposta não deve ser nulo");
        assertEquals("Maria", response.getBody().getNome());
        verify(idosoService, times(1)).criarIdoso(dto);
    }

    // =========================================================
    // EDGE CASE 2: Deve listar idosos com sucesso (200 OK)
    // =========================================================
    @Test
    @DisplayName("AACI-114: Listagem de idosos retorna 200 OK")
    void deveListarIdososComSucesso() {
        when(idosoService.listarTodos()).thenReturn(List.of(new Idoso(), new Idoso()));

        ResponseEntity<List<Idoso>> response = idosoController.listarIdosos();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody(), "A lista não deve ser nula"); // CORREÇÃO AQUI
        assertEquals(2, response.getBody().size());
        verify(idosoService, times(1)).listarTodos();
    }

    // =========================================================
    // EDGE CASE 3: Deve buscar idoso por serial (200 OK)
    // =========================================================
    @Test
    @DisplayName("AACI-114: Busca de idoso por serial do dispositivo")
    void deveBuscarPorSerialComSucesso() {
        String serial = "MON-313";
        Idoso idoso = new Idoso();
        idoso.setNome("José");

        when(idosoService.buscarPorSerial(serial)).thenReturn(idoso);

        ResponseEntity<Idoso> response = idosoController.buscarPorSerial(serial);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody(), "O corpo da resposta não deve ser nulo");
        assertEquals("José", response.getBody().getNome());
    }

    // =========================================================
    // EDGE CASE 4: Deve lançar Exceção ao buscar serial inexistente
    // =========================================================
    @Test
    @DisplayName("AACI-114: Busca por serial inexistente lança exceção")
    void deveFalharAoBuscarSerialInexistente() {
        String serial = "MON-000";
        String mensagemErro = "Idoso com a pulseira " + serial + " não encontrado.";

        when(idosoService.buscarPorSerial(serial))
                .thenThrow(new RecursoNaoEncontradoException(mensagemErro));

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> idosoController.buscarPorSerial(serial)
        );

        assertEquals(mensagemErro, exception.getMessage());
    }

    // =========================================================
    // EDGE CASE 5: Deve atualizar idoso com sucesso (200 OK)
    // =========================================================
    @Test
    @DisplayName("AACI-114: Atualização de idoso retorna 200 OK")
    void deveAtualizarIdosoComSucesso() {
        Long idIdoso = 1L;
        IdosoUpdateDTO dto = new IdosoUpdateDTO("Maria Atualizada", "123", "novo@email.com");
        Idoso idosoAtualizado = new Idoso();
        idosoAtualizado.setNome(dto.nome());

        when(idosoService.atualizarIdoso(eq(idIdoso), any(IdosoUpdateDTO.class))).thenReturn(idosoAtualizado);

        ResponseEntity<Idoso> response = idosoController.atualizarIdoso(idIdoso, dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody(), "O corpo da resposta não deve ser nulo");
        assertEquals("Maria Atualizada", response.getBody().getNome());
    }

    // =========================================================
    // EDGE CASE 6: Deve inativar idoso com sucesso (204 No Content)
    // =========================================================
    @Test
    @DisplayName("AACI-114: Inativação de idoso retorna 204 No Content")
    void deveInativarIdosoComSucesso() {
        Long idIdoso = 1L;

        ResponseEntity<Void> response = idosoController.inativarIdoso(idIdoso);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(idosoService, times(1)).inativarIdoso(idIdoso);
    }
}