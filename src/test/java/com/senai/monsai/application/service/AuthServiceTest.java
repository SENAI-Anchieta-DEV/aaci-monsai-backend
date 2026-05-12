package com.senai.monsai.application.service;

import com.senai.monsai.application.dto.LoginRequestDTO;
import com.senai.monsai.application.dto.LoginResponseDTO;
import com.senai.monsai.domain.entity.Usuario;
import com.senai.monsai.domain.enums.TipoUsuario;
import com.senai.monsai.domain.repository.UsuarioRepository;
import com.senai.monsai.infrastructure.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    // =========================================================
    // EDGE CASE 1: Login com credenciais válidas
    // =========================================================
    @Test
    @DisplayName("AACI-114: Autenticação com credenciais válidas e geração de token")
    void deveAutenticarUsuarioComSucesso() {
        // GIVEN
        LoginRequestDTO dto = new LoginRequestDTO("andre@email.com", "senha123");
        Usuario usuario = new Usuario();
        usuario.setEmail("andre@email.com");
        usuario.setTipo(TipoUsuario.GESTOR);

        // Mock do processo de autenticação do Spring Security
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);

        when(usuarioRepository.findByEmail("andre@email.com")).thenReturn(Optional.of(usuario));
        when(jwtService.generateToken(anyString(), anyString())).thenReturn("token_valido");

        // WHEN
        LoginResponseDTO response = authService.autenticar(dto);

        // THEN
        assertNotNull(response);
        assertEquals("token_valido", response.token());
        assertEquals("GESTOR", response.tipoPerfil());
        verify(authenticationManager, times(1)).authenticate(any());
    }

    // =========================================================
    // EDGE CASE 2: Login com senha incorreta (AuthenticationManager lança exceção)
    // =========================================================
    @Test
    @DisplayName("AACI-114: Falha na autenticação por senha incorreta")
    void deveFalharComSenhaIncorreta() {
        // GIVEN
        LoginRequestDTO dto = new LoginRequestDTO("andre@email.com", "senhaErrada");
        String mensagemErro = "Credenciais inválidas";

        // Simulação do AuthenticationManager lançando a exceção padrão do Spring
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException(mensagemErro));

        // WHEN & THEN
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.autenticar(dto)
        );

        assertEquals(mensagemErro, exception.getMessage());
        verify(usuarioRepository, never()).findByEmail(anyString());
    }

    // =========================================================
    // EDGE CASE 3: Login com email não cadastrado
    // =========================================================
    @Test
    @DisplayName("AACI-114: Falha na autenticação por e-mail inexistente")
    void deveFalharComEmailInexistente() {
        // GIVEN
        LoginRequestDTO dto = new LoginRequestDTO("fantasma@email.com", "123");
        String mensagemErro = "Usuário não encontrado.";

        // A autenticação passa (cenário hipotético onde o manager não valida o banco antes)
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(usuarioRepository.findByEmail("fantasma@email.com")).thenReturn(Optional.empty());

        // WHEN & THEN
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> authService.autenticar(dto)
        );

        assertEquals(mensagemErro, exception.getMessage());
    }
}