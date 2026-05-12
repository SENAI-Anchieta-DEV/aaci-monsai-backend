package com.senai.monsai.ui_interface.controller;

import com.senai.monsai.application.dto.LoginRequestDTO;
import com.senai.monsai.application.dto.LoginResponseDTO;
import com.senai.monsai.application.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    // =========================================================
    // EDGE CASE 1: Deve realizar login com sucesso (200 OK)
    // =========================================================
    @Test
    @DisplayName("AACI-114: Login bem-sucedido retorna 200 OK e Token")
    void deveRealizarLoginComSucesso() {
        // GIVEN
        LoginRequestDTO dto = new LoginRequestDTO("admin@monsai.com", "senha123");
        LoginResponseDTO mockResponse = new LoginResponseDTO("jwt_token_exemplo", "SUPER_ADMIN");

        when(authService.autenticar(any(LoginRequestDTO.class))).thenReturn(mockResponse);

        // WHEN
        ResponseEntity<LoginResponseDTO> response = authController.login(dto);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody(), "O corpo da resposta de login não deve ser nulo");
        assertEquals("jwt_token_exemplo", response.getBody().token());
        verify(authService, times(1)).autenticar(dto);
    }

    // =========================================================
    // EDGE CASE 2: Deve falhar ao logar com credenciais inválidas
    // =========================================================
    @Test
    @DisplayName("AACI-114: Login com falha lança exceção de credenciais")
    void deveFalharNoLoginComCredenciaisInvalidas() {
        // GIVEN
        LoginRequestDTO dto = new LoginRequestDTO("erro@email.com", "errada");
        String mensagemErro = "E-mail ou senha incorretos";

        when(authService.autenticar(any(LoginRequestDTO.class)))
                .thenThrow(new BadCredentialsException(mensagemErro));

        // WHEN & THEN
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authController.login(dto)
        );

        assertEquals(mensagemErro, exception.getMessage());
    }
}