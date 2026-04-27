package com.senai.monsai.application.service;

import com.senai.monsai.application.dto.UsuarioCreateDTO;
import com.senai.monsai.domain.entity.Asilo;
import com.senai.monsai.domain.entity.Usuario;
import com.senai.monsai.domain.enums.TipoUsuario;
import com.senai.monsai.domain.exception.RecursoDuplicadoException;
import com.senai.monsai.domain.exception.RecursoNaoEncontradoException;
import com.senai.monsai.domain.repository.AsiloRepository;
import com.senai.monsai.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AsiloRepository asiloRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    @DisplayName("AACI-114: Deve salvar usuário com sucesso")
    void salvarUsuario() {
        // GIVEN: DTO com todos os campos obrigatórios
        Long asiloId = 1L;

        var dto = new UsuarioCreateDTO(
                "André Mendes",
                "andre@email.com",
                "senha123",
                "123.456.789-10",
                TipoUsuario.GESTOR,
                asiloId
        );

        when(asiloRepository.findById(asiloId))
                .thenReturn(Optional.of(new Asilo()));

        when(usuarioRepository.existsByEmail(anyString()))
                .thenReturn(false);

        when(usuarioRepository.existsByCpf(anyString()))
                .thenReturn(false);

        when(passwordEncoder.encode(anyString()))
                .thenReturn("hash_senha");

        // WHEN
        assertDoesNotThrow(() -> usuarioService.criarUsuario(dto));

        // THEN
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    // =========================================================
    // EDGE CASE 1: Asilo não existe
    // =========================================================

    @Test
    @DisplayName("AACI-115: Deve falhar se o asilo do usuário não existir")
    void falhaSeAsiloNaoExistir() {
        // GIVEN
        Long asiloInexistente = 99L;
        var dto = new UsuarioCreateDTO(
                "André",
                "andre@email.com",
                "123",
                "123.456.789-10",
                TipoUsuario.CUIDADOR,
                asiloInexistente
        );

        when(asiloRepository.findById(asiloInexistente)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(RecursoNaoEncontradoException.class, () -> usuarioService.criarUsuario(dto));
        verify(usuarioRepository, never()).save(any());
    }

    // =========================================================
    // EDGE CASE 2: E-mail já cadastrado
    // =========================================================

    @Test
    @DisplayName("AACI-112: Deve lançar uma exceção ao tentar cadastrar e-mail já cadastrado")
    void deveFalharParaEmailDuplicado() {
        // GIVEN: DTO com email que já existe no DB
        var dto = new UsuarioCreateDTO(
                "Carlos Oliveira",
                "carlos@email.com",
                "senha123",
                "999.000.888.21",
                TipoUsuario.CUIDADOR,
                1L
        );

        // O asilo existe normalmente — o erro deve ocorrer ANTES de qualquer save
        when(asiloRepository.findById(1L))
                .thenReturn(Optional.of(new Asilo()));

        // Simulação de email como se já estivesse no banco
        when(usuarioRepository.existsByEmail("carlos@email.com"))
                .thenReturn(true);

        // WHEN & THEN: O service deve lançar a exception de duplicidade
        assertThrows(RecursoDuplicadoException.class,
                () -> usuarioService.criarUsuario(dto));

        verify(usuarioRepository, never()).save(any());
    }

    // =========================================================
    // EDGE CASE 2: CPF já cadastrado
    // =========================================================

    @Test
    @DisplayName("AACI-112: Deve lançar uma exceção ao tentar cadastrar um CPF já existente")
    void deveFalharParaCpfDuplicado() {
        // GIVEN: DTO com CPF que já existe no banco
        var dto = new UsuarioCreateDTO(
                "Maria Souza",
                "maria@email.com",
                "senha456",
                "123.456.789-10",   // CPF já cadastrado
                TipoUsuario.GESTOR,
                1L
        );

        when(asiloRepository.findById(1L))
                .thenReturn(Optional.of(new Asilo()));

        // E-mail ainda não existe — o erro deve vir do CPF
        when(usuarioRepository.existsByCpf(anyString()))
                .thenReturn(false);

        when(usuarioRepository.existsByCpf("123.456.789-10"))
                .thenReturn(true);

        //WHEN & THEN
        assertThrows(RecursoDuplicadoException.class,
                () -> usuarioService.criarUsuario(dto));

        verify(usuarioRepository, never()).save(any());
    }
}
