package com.senai.monsai.application.service;

import com.senai.monsai.application.dto.IdosoCreateDTO;
import com.senai.monsai.application.dto.IdosoUpdateDTO;
import com.senai.monsai.domain.entity.Asilo;
import com.senai.monsai.domain.entity.Dispositivo;
import com.senai.monsai.domain.entity.Idoso;
import com.senai.monsai.domain.entity.Usuario;
import com.senai.monsai.domain.exception.IdosoNaoEncontradoException;
import com.senai.monsai.domain.exception.RecursoDuplicadoException;
import com.senai.monsai.domain.exception.RecursoNaoEncontradoException;
import com.senai.monsai.domain.exception.RegraNegocioException;
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

import java.util.ArrayList;
import java.util.List;
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

    private Usuario usuarioGestor;
    private Asilo asiloPadrao;

    @BeforeEach
    void setupSecurity() {
        // Mock do SecurityContext para simular o usuário logado
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("admin@email.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Instâncias comuns para reuso nos testes
        asiloPadrao = new Asilo();
        asiloPadrao.setId(1L);

        usuarioGestor = new Usuario();
        usuarioGestor.setAsilo(asiloPadrao);
    }

    // =========================================================
    // EDGE CASE 1: Deve cadastrar idoso com sucesso
    // =========================================================
    @Test
    @DisplayName("AACI-114: Cadastro de idoso com asilo e serial")
    void deveCriarIdosoComSucesso() {
        IdosoCreateDTO dto = new IdosoCreateDTO("João", "111.222.333-44", "joao@email.com", "MON-313", 1L);

        when(usuarioRepository.findByEmail("admin@email.com")).thenReturn(Optional.of(usuarioGestor));
        when(asiloRepository.findById(1L)).thenReturn(Optional.of(asiloPadrao));
        when(idosoRepository.existsByCpf(dto.cpf())).thenReturn(false);
        when(pulseiraRepository.save(any(Dispositivo.class))).thenAnswer(i -> i.getArguments()[0]);
        when(idosoRepository.save(any(Idoso.class))).thenAnswer(i -> i.getArguments()[0]);

        Idoso resultado = idosoService.criarIdoso(dto);

        assertNotNull(resultado);
        assertEquals("João", resultado.getNome());
        verify(pulseiraRepository, times(1)).save(any(Dispositivo.class));
        verify(idosoRepository, times(1)).save(any(Idoso.class));
    }

    // =========================================================
    // EDGE CASE 2: Deve falhar se o ID do asilo não existir
    // =========================================================
    @Test
    @DisplayName("AACI-114: Falha quando asiloId não existe")
    void deveFalharParaAsiloInexistente() {
        IdosoCreateDTO dto = new IdosoCreateDTO("João", "111", "j@e.com", "MON-1", 99L);
        String mensagemEsperada = "O Asilo informado no asiloId não existe.";

        when(usuarioRepository.findByEmail("admin@email.com")).thenReturn(Optional.of(usuarioGestor));
        when(asiloRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> idosoService.criarIdoso(dto)
        );

        assertEquals(mensagemEsperada, exception.getMessage());
    }

    // =========================================================
    // EDGE CASE 3: Deve lançar exception ao tentar cadastrar idoso com CPF duplicado
    // =========================================================
    @Test
    @DisplayName("AACI-114: Cadastro de idoso barrado por CPF duplicado")
    void deveFalharAoCriarIdosoComCpfDuplicado() {
        IdosoCreateDTO dto = new IdosoCreateDTO("Ana", "111.222.333-44", "ana@email.com", "MON-999", 1L);
        String mensagemEsperada = "Já existe um idoso cadastrado com este CPF.";

        when(usuarioRepository.findByEmail("admin@email.com")).thenReturn(Optional.of(usuarioGestor));
        when(asiloRepository.findById(1L)).thenReturn(Optional.of(asiloPadrao));
        when(idosoRepository.existsByCpf(dto.cpf())).thenReturn(true);

        RecursoDuplicadoException exception = assertThrows(
                RecursoDuplicadoException.class,
                () -> idosoService.criarIdoso(dto)
        );

        assertEquals(mensagemEsperada, exception.getMessage());
        verify(idosoRepository, never()).save(any());
    }

    // =========================================================
    // EDGE CASE 4: Deve falhar quando Gestor tentar criar Idoso em asilo diferente do dele
    // =========================================================
    @Test
    @DisplayName("AACI-114: Violação de Cross-Tenant no cadastro")
    void deveFalharAoCadastrarEmOutroAsilo() {
        IdosoCreateDTO dto = new IdosoCreateDTO("Pedro", "999", "p@email.com", "MON-5", 2L); // asiloDestino diferente (2L)
        Asilo asiloDiferente = new Asilo();
        asiloDiferente.setId(2L);
        String mensagemEsperada = "Violação de segurança: Você não pode cadastrar um idoso em outro asilo.";

        when(usuarioRepository.findByEmail("admin@email.com")).thenReturn(Optional.of(usuarioGestor));
        when(asiloRepository.findById(2L)).thenReturn(Optional.of(asiloDiferente));

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> idosoService.criarIdoso(dto)
        );

        assertEquals(mensagemEsperada, exception.getMessage());
    }

    // =========================================================
    // EDGE CASE 5: Deve listar idosos apenas do Asilo do Gestor logado
    // =========================================================
    @Test
    @DisplayName("AACI-114: Listagem filtrada por tenant (Asilo) do Gestor")
    void deveListarApenasDoAsiloDoUsuario() {
        Idoso idoso = new Idoso();
        idoso.setAsilo(asiloPadrao);

        when(usuarioRepository.findByEmail("admin@email.com")).thenReturn(Optional.of(usuarioGestor));
        when(idosoRepository.findByAsiloId(1L)).thenReturn(List.of(idoso));

        List<Idoso> resultado = idosoService.listarTodos();

        assertEquals(1, resultado.size());
        verify(idosoRepository, times(1)).findByAsiloId(1L);
        verify(idosoRepository, never()).findAll();
    }

    // =========================================================
    // EDGE CASE 6: Deve inativar Idoso desvinculando dispositivo e cuidadores
    // =========================================================
    @Test
    @DisplayName("AACI-114: Inativação de idoso com limpeza de vínculos")
    void deveInativarIdosoComSucesso() {
        Long idosoId = 10L;
        Idoso idosoExistente = new Idoso();
        idosoExistente.setId(idosoId);
        idosoExistente.setAtivo(true);
        idosoExistente.setAsilo(asiloPadrao); // Mesmo asilo do gestor
        idosoExistente.setDispositivo(new Dispositivo());

        Usuario cuidador = new Usuario();
        cuidador.setIdosos(new ArrayList<>(List.of(idosoExistente))); // Arrays mutáveis
        idosoExistente.setUsuarios(new ArrayList<>(List.of(cuidador)));

        when(usuarioRepository.findByEmail("admin@email.com")).thenReturn(Optional.of(usuarioGestor));
        when(idosoRepository.findById(idosoId)).thenReturn(Optional.of(idosoExistente));

        idosoService.inativarIdoso(idosoId);

        assertFalse(idosoExistente.isAtivo());
        assertNull(idosoExistente.getDispositivo());
        assertTrue(cuidador.getIdosos().isEmpty()); // O Idoso foi removido do cuidador
        verify(idosoRepository, times(1)).save(idosoExistente);
        verify(usuarioRepository, times(1)).save(cuidador);
    }
}