package com.senai.monsai.ui_interface.controller;

import com.senai.monsai.application.dto.AtualizarSenhaDTO;
import com.senai.monsai.application.dto.UsuarioCreateDTO;
import com.senai.monsai.application.service.UsuarioService;
import com.senai.monsai.domain.entity.Idoso;
import com.senai.monsai.domain.entity.Usuario;
import com.senai.monsai.domain.enums.TipoUsuario;
import com.senai.monsai.domain.exception.RecursoDuplicadoException;
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
public class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    // =========================================================
    // EDGE CASE 1: Deve criar usuário com sucesso (201 Created)
    // =========================================================
    @Test
    @DisplayName("AACI-114: Cadastro de usuário retorna 201 Created")
    void deveCriarUsuarioComSucesso() {
        UsuarioCreateDTO dto = new UsuarioCreateDTO("André", "a@email.com", "123", "123", TipoUsuario.CUIDADOR, 1L);
        Usuario usuarioCriado = new Usuario();
        usuarioCriado.setNome("André");

        when(usuarioService.criarUsuario(any(UsuarioCreateDTO.class))).thenReturn(usuarioCriado);

        ResponseEntity<Usuario> response = usuarioController.criarUsuario(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody(), "O corpo da resposta não deve ser nulo");
        assertEquals("André", response.getBody().getNome());
        verify(usuarioService, times(1)).criarUsuario(dto);
    }

    // =========================================================
    // EDGE CASE 2: Deve listar todos os usuários com sucesso (200 OK)
    // =========================================================
    @Test
    @DisplayName("AACI-114: Listagem de usuários retorna 200 OK")
    void deveListarUsuariosComSucesso() {
        when(usuarioService.listarTodos()).thenReturn(List.of(new Usuario(), new Usuario()));

        ResponseEntity<List<Usuario>> response = usuarioController.listarUsuarios();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody(), "O corpo da resposta não deve ser nulo");
        assertEquals(2, response.getBody().size());
        verify(usuarioService, times(1)).listarTodos();
    }

    // =========================================================
    // EDGE CASE 3: Deve lançar Exceção ao tentar cadastrar e-mail duplicado
    // =========================================================
    @Test
    @DisplayName("AACI-114: Cadastro barrado no Controller por E-mail duplicado")
    void deveFalharParaEmailDuplicado() {
        UsuarioCreateDTO dto = new UsuarioCreateDTO("Carlos", "carlos@email.com", "123", "999", TipoUsuario.CUIDADOR, 1L);
        String mensagemEsperada = "Já existe um usuário cadastrado com este e-mail.";

        when(usuarioService.criarUsuario(dto)).thenThrow(new RecursoDuplicadoException(mensagemEsperada));

        RecursoDuplicadoException exception = assertThrows(
                RecursoDuplicadoException.class,
                () -> usuarioController.criarUsuario(dto)
        );

        assertEquals(mensagemEsperada, exception.getMessage());
    }

    // =========================================================
    // EDGE CASE 4: Deve atualizar senha com sucesso (204 No Content)
    // =========================================================
    @Test
    @DisplayName("AACI-114: Atualização de senha retorna 204 No Content")
    void deveAtualizarSenhaComSucesso() {
        Long idUsuario = 1L;
        AtualizarSenhaDTO dto = new AtualizarSenhaDTO("novaSenha456");

        ResponseEntity<Void> response = usuarioController.atualizarSenha(idUsuario, dto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(usuarioService, times(1)).atualizarSenha(idUsuario, dto);
    }

    // =========================================================
    // EDGE CASE 5: Deve vincular idoso com sucesso (204 No Content)
    // =========================================================
    @Test
    @DisplayName("AACI-114: Vinculação de idoso ao usuário retorna 204 No Content")
    void deveVincularIdosoComSucesso() {
        Long idUsuario = 1L;
        Long idIdoso = 2L;

        ResponseEntity<Void> response = usuarioController.vincularIdoso(idUsuario, idIdoso);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(usuarioService, times(1)).vincularIdoso(idUsuario, idIdoso);
    }

    // =========================================================
    // EDGE CASE 6: Deve listar idosos vinculados ao usuário com sucesso (200 OK)
    // =========================================================
    @Test
    @DisplayName("AACI-114: Listagem de idosos vinculados retorna 200 OK")
    void deveListarIdososVinculadosComSucesso() {
        Long idUsuario = 1L;
        when(usuarioService.listarIdososVinculados(idUsuario)).thenReturn(List.of(new Idoso()));

        ResponseEntity<List<Idoso>> response = usuarioController.listarIdososVinculados(idUsuario);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody(), "A lista de idosos vinculados não deve ser nula");
        assertEquals(1, response.getBody().size());
        verify(usuarioService, times(1)).listarIdososVinculados(idUsuario);
    }
}