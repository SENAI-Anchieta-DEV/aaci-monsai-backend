package com.senai.monsai.application.service;

import com.senai.monsai.application.dto.LoginRequestDTO;
import com.senai.monsai.application.dto.LoginResponseDTO;
import com.senai.monsai.domain.entity.Usuario;
import com.senai.monsai.domain.enums.TipoUsuario; // Ajuste o import do seu enum se necessário
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("1. Deve autenticar com sucesso e retornar o Token JWT")
    void deveAutenticarComSucesso() {
        // Arrange
        LoginRequestDTO requestDTO = new LoginRequestDTO("admin@monsai.com", "senha123");

        Usuario usuarioMock = new Usuario();
        usuarioMock.setEmail("admin@monsai.com");
        usuarioMock.setAtivo(true);
        usuarioMock.setTipo(TipoUsuario.GESTOR); // Assumindo que você tem um enum de TipoUsuario

        String tokenEsperado = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.teste.123";

        // Quando buscar o usuário, retorna o mock ativo
        when(usuarioRepository.findByEmail(requestDTO.email())).thenReturn(Optional.of(usuarioMock));
        // Quando pedir o token, retorna nossa string de mentira
        when(jwtService.generateToken(usuarioMock.getEmail(), usuarioMock.getTipo().name()))
                .thenReturn(tokenEsperado);

        // Act
        LoginResponseDTO response = authService.autenticar(requestDTO);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo(tokenEsperado);
        assertThat(response.tipoPerfil()).isEqualTo("GESTOR"); // Ajuste conforme o nome do campo no seu DTO

        // Verifica se o AuthenticationManager foi chamado para validar a senha
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("2. Deve falhar se a senha estiver incorreta (AuthenticationManager lança erro)")
    void deveFalharSeSenhaIncorreta() {
        // Arrange
        LoginRequestDTO requestDTO = new LoginRequestDTO("admin@monsai.com", "senha-errada");

        // Simulamos o Spring Security rejeitando a senha
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authService.autenticar(requestDTO);
        });

        // Como a senha falhou, ele NUNCA deve ir no banco buscar o usuário nem gerar token
        verify(usuarioRepository, never()).findByEmail(anyString());
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("3. Deve falhar ao tentar logar com um usuário que não existe no banco")
    void deveFalharSeUsuarioNaoExistirNoBanco() {
        // Arrange
        LoginRequestDTO requestDTO = new LoginRequestDTO("fantasma@monsai.com", "senha123");

        // O AuthManager deixa passar, mas o banco de dados não acha o e-mail
        when(usuarioRepository.findByEmail(requestDTO.email())).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            authService.autenticar(requestDTO);
        });

        assertThat(exception.getMessage()).isEqualTo("Usuário não encontrado.");
        verify(jwtService, never()).generateToken(anyString(), anyString()); // Nunca gera token
    }

    @Test
    @DisplayName("4. Deve bloquear o login se o usuário estiver inativo (soft delete)")
    void deveFalharSeUsuarioEstiverInativo() {
        // Arrange
        LoginRequestDTO requestDTO = new LoginRequestDTO("demitido@monsai.com", "senha123");

        Usuario usuarioInativo = new Usuario();
        usuarioInativo.setEmail("demitido@monsai.com");
        usuarioInativo.setAtivo(false); // ATENÇÃO: Usuário inativo!

        when(usuarioRepository.findByEmail(requestDTO.email())).thenReturn(Optional.of(usuarioInativo));

        // Act & Assert
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authService.autenticar(requestDTO);
        });

        assertThat(exception.getMessage()).isEqualTo("Usuário inativo.");
        verify(jwtService, never()).generateToken(anyString(), anyString()); // Barrado antes de gerar o token
    }
}