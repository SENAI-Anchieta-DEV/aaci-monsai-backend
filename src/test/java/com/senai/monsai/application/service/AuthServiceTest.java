package com.senai.monsai.application.service;

import com.senai.monsai.application.dto.LoginRequestDTO;  // ajuste o nome do DTO se diferente
import com.senai.monsai.domain.entity.Usuario;
import com.senai.monsai.domain.enums.TipoUsuario;

import com.senai.monsai.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para AuthService (AACI-112).
 *
 * Cobre os cenários de autenticação (login) com credenciais válidas e inválidas,
 * mockando o UsuarioRepository e o PasswordEncoder para isolar a lógica do service.
 */
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    // =========================================================
    // EDGE 1: Login com credenciais válidas
    // =========================================================

    @Test
    @DisplayName("AACI-112: Deve autenticar usuário com credenciais válidas")
    void deveAutenticarUsuarioComCredenciaisValidas() {
        // GIVEN: Usuário cadastrado no banco com senha em hash
        var dto = new LoginRequestDTO("andre@email.com", "senha123");

        Usuario usuario = new Usuario();
        usuario.setEmail("andre@email.com");
        usuario.setSenha("$2a$10$hash_simulado");
        usuario.setTipo(TipoUsuario.GESTOR);

        // O repositório encontra o usuário pelo e-mail
        when(usuarioRepository.findByEmail("andre@email.com"))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches("senha123", "$2a$10$hash_simulado"))
                .thenReturn(true);

        // WHEN & THEN: O login deve funcionar sem lançar exception
        // Se o seu AuthService retorna um token ou DTO, você pode capturar e fazer asserts
        assertDoesNotThrow(() -> authService.autenticar(dto));
    }

    // =========================================================
    // EDGE 2: Login com senha incorreta
    // =========================================================

    @Test
    @DisplayName("AACI-112: Deve lançar exception ao logar com senha incorreta")
    void deveFalharComSenhaInvalidas() {
        // GIVEN:
        var dto = new LoginRequestDTO(
                "andre@email.com",
                "senhaErrada"
        );

        Usuario usuario = new Usuario();
        usuario.setEmail("andre@email.com");
        usuario.setSenha("$2a$10$hash_simulado");

        when(usuarioRepository.findByEmail("andre@email.com"))
                .thenReturn(Optional.of(usuario));

        // Password Encoder rejeita a senha
        when(passwordEncoder.matches("senhaErrada", "$2a$10$hash_simulado"))
                .thenReturn(false);

        // WHEN & THEN: O service deve lançar exception de credenciais inválidas
        assertThrows(BadCredentialsException.class, () -> authService.autenticar(dto));
    }

    // =========================================================
    // EDGE 3: Login com email não cadastrado
    // =========================================================

    @Test
    @DisplayName("AACI-112: Deve lançar exception ao logar com o email inexistente")
    void deveFalharComEmailInvalido() {
        // GIVEN: Nenhum usuário com esse e-mail existe no banco
        var dto = new LoginRequestDTO(
                "emailnaoexiste@email.com",
                "senha123"
        );

        when(usuarioRepository.findByEmail("emailnaoexiste@email.com"))
                .thenReturn(Optional.empty());

        // WHEN & THEN: O service deve lançar exception de credenciais inválidas
        assertThrows(BadCredentialsException.class, () -> authService.autenticar(dto));

        // O PasswordEncoder não vai ser chamado se o e-mail já não existe
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }


}
