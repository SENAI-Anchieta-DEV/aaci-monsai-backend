package com.senai.monsai.application.service;

import com.senai.monsai.application.dto.IdosoCreateDTO;
import com.senai.monsai.domain.entity.Asilo;
import com.senai.monsai.domain.entity.Idoso;
import com.senai.monsai.domain.entity.Usuario;
import com.senai.monsai.domain.exception.RecursoDuplicadoException;
import com.senai.monsai.domain.exception.RecursoNaoEncontradoException;
import com.senai.monsai.domain.repository.AsiloRepository;
import com.senai.monsai.domain.repository.IdosoRepository;
import com.senai.monsai.domain.repository.PulseiraRepository;
import com.senai.monsai.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IdosoServiceTest {

    @Mock private IdosoRepository idosoRepository;
    @Mock private AsiloRepository asiloRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PulseiraRepository pulseiraRepository;

    @InjectMocks
    private IdosoService idosoService;

    @BeforeEach
    void setupSecurity() {
        // Mock do SecurityContext para simular o usuário logado "admin@email.com"
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("admin@email.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    // =========================================================
    // EDGE CASE 1: Deve cadastrar idoso com sucesso
    // =========================================================

    @Test
    @DisplayName("AACI-114: Cadastro de idoso com asilo e serial")
    void deveCriarIdosoComSucesso() {
        // GIVEN: Dados do DTO fornecido por você
        var dto = new IdosoCreateDTO("João", "111.222.333-44", "joao@email.com", "MON-313", 1L);

        Usuario usuarioLogado = new Usuario();
        usuarioLogado.setAsilo(new Asilo()); // Usuário pertence a um asilo

        when(usuarioRepository.findByEmail("admin@email.com")).thenReturn(Optional.of(usuarioLogado));
        when(asiloRepository.findById(1L)).thenReturn(Optional.of(new Asilo()));
        when(idosoRepository.existsByCpf(anyString())).thenReturn(false);

        // WHEN
        idosoService.criarIdoso(dto);

        // THEN
        verify(pulseiraRepository, times(1)).save(any());
        verify(idosoRepository, times(1)).save(any(Idoso.class));
    }

    // =========================================================
    // EDGE CASE 2: Deve falhar se o ID do asilo não existir
    // =========================================================


    @Test
    @DisplayName("AACI-115: Falha quando asiloId não existe")
    void deveFalharParaAsiloInexistente() {
        var dto = new IdosoCreateDTO("João", "111", "j@e.com", "MON-1", 99L);

        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(new Usuario()));
        when(asiloRepository.findById(99L)).thenReturn(Optional.empty());

        // O erro lançado deve ser o que você definiu no código: "O Asilo informado no asiloId não existe."
        assertThrows(RecursoNaoEncontradoException.class, () -> idosoService.criarIdoso(dto));
    }

    // =========================================================
    // EDGE CASE 3: CPF de idoso já cadastrado
    // =========================================================

    @Test
    @DisplayName("AACI-112: Deve lançar exception ao tentar cadastrar um idoso com CPF duplicado")
    void deveFalharAoCriarIdosoComCpfDuplicado() {
        // GIVEN: DTO com CPF que já existe no DB
        var dto = new IdosoCreateDTO(
                "Ana Luiza",
                "111.222.333-44",
                "familiarana@email.com",
                "MON-999",
                1L
        );

        // Usuário logado existe e possui um asilo associado
        Usuario usuarioLogado = new Usuario();
        usuarioLogado.setAsilo(new Asilo());
        when(usuarioRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(usuarioLogado));

        // O asilo informado no DTO existe
        when(asiloRepository.findById(1L))
                .thenReturn(Optional.of(new Asilo()));

        // Simulação do CPF já cadastrado
        when(idosoRepository.existsByCpf("111.222.333-44"))
                .thenReturn(true);

        // WHEN & THEN: O service deve barrar o cadastro com exception
        // Ajuste a exception abaixo conforme a implementação real do service
        assertThrows(RecursoDuplicadoException.class,
                () -> idosoService.criarIdoso(dto));

        verify(idosoRepository, never()).save(any());
        verify(pulseiraRepository, never()).save(any());
    }

    // =========================================================
    // EDGE CASE 4: Usuário logado sem asilo vinculado
    // =========================================================

    @Test
    @DisplayName("AACI-112: Deve lançar exception quando um usuário logado não possui asilo")
    void deveFalharQuandoUsuarioLogadoNaoTiverAsilo() {
        // GIVEN: DTO válido em aparência
        var dto = new IdosoCreateDTO(
                "José Martins",
                "555.666.777-88",
                "familiarjose@email.com",
                "MON-358",
                1L
        );

        // O usuário logado existe, mas não possui asilo associado
        Usuario usuarioSemAsilo = new Usuario();

        when(usuarioRepository.findByEmail("admin@monsai.com"))
                .thenReturn(Optional.of(usuarioSemAsilo));

        // WHEN & THEN: O service deve detectar a divergência e lançar exceção
        assertThrows(RecursoNaoEncontradoException.class,
                () -> idosoService.criarIdoso(dto));

        verify(idosoRepository, never()).save(any());
    }
}