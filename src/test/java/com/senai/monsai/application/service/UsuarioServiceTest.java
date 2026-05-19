package com.senai.monsai.application.service;

import com.senai.monsai.application.dto.UsuarioCreateDTO;
import com.senai.monsai.domain.entity.Asilo;
import com.senai.monsai.domain.entity.Idoso;
import com.senai.monsai.domain.entity.Usuario;
import com.senai.monsai.domain.enums.TipoUsuario;
import com.senai.monsai.domain.exception.AsiloNaoEncontradoException;
import com.senai.monsai.domain.exception.RecursoDuplicadoException;
import com.senai.monsai.domain.exception.RegraNegocioException;
import com.senai.monsai.domain.repository.AsiloRepository;
import com.senai.monsai.domain.repository.IdosoRepository;
import com.senai.monsai.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private IdosoRepository idosoRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AsiloRepository asiloRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    // =========================================================
    // EDGE CASE 1: Deve salvar usuário com sucesso
    // =========================================================
    @Test
    @DisplayName("AACI-114: Salvar usuário com sucesso")
    void deveSalvarUsuarioComSucesso() {
        Long asiloId = 1L;
        var dto = new UsuarioCreateDTO("André Mendes", "andre@email.com", "senha123", "123.456.789-10", TipoUsuario.GESTOR, asiloId);

        when(asiloRepository.findById(asiloId)).thenReturn(Optional.of(new Asilo()));
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCpf(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hash_senha");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArguments()[0]);

        Usuario salvo = usuarioService.criarUsuario(dto);

        assertNotNull(salvo);
        assertEquals("André Mendes", salvo.getNome());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    // =========================================================
    // EDGE CASE 2: Deve falhar se o asilo não existir
    // =========================================================
    @Test
    @DisplayName("AACI-114: Falha no cadastro por asilo inexistente")
    void deveFalharSeAsiloNaoExistir() {
        Long asiloInexistente = 99L;
        var dto = new UsuarioCreateDTO("André", "andre@e.com", "123", "123", TipoUsuario.CUIDADOR, asiloInexistente);
        String msgEsperada = "Asilo com ID " + asiloInexistente + " não foi encontrado.";

        when(asiloRepository.findById(asiloInexistente)).thenReturn(Optional.empty());

        AsiloNaoEncontradoException exception = assertThrows(
                AsiloNaoEncontradoException.class,
                () -> usuarioService.criarUsuario(dto)
        );

        // Verifica se a exceção contém o texto esperado
        assertEquals(msgEsperada, exception.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    // =========================================================
    // EDGE CASE 3: Deve lançar exceção por e-mail duplicado
    // =========================================================
    @Test
    @DisplayName("AACI-114: Cadastro barrado por E-mail duplicado")
    void deveFalharParaEmailDuplicado() {
        var dto = new UsuarioCreateDTO("Carlos", "carlos@email.com", "123", "999", TipoUsuario.CUIDADOR, 1L);
        String msgEsperada = "Já existe um usuário cadastrado com este e-mail.";

        when(asiloRepository.findById(1L)).thenReturn(Optional.of(new Asilo()));
        when(usuarioRepository.existsByEmail("carlos@email.com")).thenReturn(true);

        RecursoDuplicadoException exception = assertThrows(
                RecursoDuplicadoException.class,
                () -> usuarioService.criarUsuario(dto)
        );

        assertEquals(msgEsperada, exception.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    // =========================================================
    // EDGE CASE 4: Deve lançar exceção por CPF duplicado
    // =========================================================
    @Test
    @DisplayName("AACI-114: Cadastro barrado por CPF duplicado")
    void deveFalharParaCpfDuplicado() {
        var dto = new UsuarioCreateDTO("Maria", "m@email.com", "123", "123.456", TipoUsuario.GESTOR, 1L);
        String msgEsperada = "Já existe um usuário cadastrado com este CPF.";

        when(asiloRepository.findById(1L)).thenReturn(Optional.of(new Asilo()));
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCpf("123.456")).thenReturn(true);

        RecursoDuplicadoException exception = assertThrows(
                RecursoDuplicadoException.class,
                () -> usuarioService.criarUsuario(dto)
        );

        assertEquals(msgEsperada, exception.getMessage());
    }

    // =========================================================
    // EDGE CASE 5: Deve vincular idoso com sucesso
    // =========================================================
    @Test
    @DisplayName("AACI-114: Vinculação de idoso a cuidador com sucesso")
    void deveVincularIdosoComSucesso() {
        Asilo asilo = new Asilo();
        asilo.setId(1L);

        Usuario usuario = new Usuario();
        usuario.setAtivo(true);
        usuario.setAsilo(asilo);
        usuario.setIdosos(new ArrayList<>());

        Idoso idoso = new Idoso();
        idoso.setAtivo(true);
        idoso.setAsilo(asilo);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(idosoRepository.findById(2L)).thenReturn(Optional.of(idoso));

        assertDoesNotThrow(() -> usuarioService.vincularIdoso(1L, 2L));
        assertTrue(usuario.getIdosos().contains(idoso));
        verify(usuarioRepository, times(1)).save(usuario);
    }

    // =========================================================
    // EDGE CASE 6: Deve falhar ao vincular idoso de asilo diferente (Cross-Tenant)
    // =========================================================
    @Test
    @DisplayName("AACI-114: Bloqueio de vínculo Cross-Tenant (Asilos diferentes)")
    void deveFalharVincularIdosoAsiloDiferente() {
        Asilo asilo1 = new Asilo(); asilo1.setId(1L);
        Asilo asilo2 = new Asilo(); asilo2.setId(2L);

        Usuario usuario = new Usuario();
        usuario.setAtivo(true);
        usuario.setAsilo(asilo1);

        Idoso idoso = new Idoso();
        idoso.setAtivo(true);
        idoso.setAsilo(asilo2);

        String msgEsperada = "Violação de segurança: O usuário e o idoso não pertencem ao mesmo asilo.";

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(idosoRepository.findById(2L)).thenReturn(Optional.of(idoso));

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> usuarioService.vincularIdoso(1L, 2L)
        );

        assertEquals(msgEsperada, exception.getMessage());
    }
}