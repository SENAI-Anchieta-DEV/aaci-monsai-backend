package com.senai.monsai.application.service;

import com.senai.monsai.application.dto.IdosoCreateDTO;
import com.senai.monsai.domain.entity.Asilo;
import com.senai.monsai.domain.entity.Idoso;
import com.senai.monsai.domain.entity.Usuario;
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

    @Test
    @DisplayName("AACI-115: Falha quando asiloId não existe")
    void deveFalharParaAsiloInexistente() {
        var dto = new IdosoCreateDTO("João", "111", "j@e.com", "MON-1", 99L);

        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(new Usuario()));
        when(asiloRepository.findById(99L)).thenReturn(Optional.empty());

        // O erro lançado deve ser o que você definiu no código: "O Asilo informado no asiloId não existe."
        assertThrows(RecursoNaoEncontradoException.class, () -> idosoService.criarIdoso(dto));
    }
}