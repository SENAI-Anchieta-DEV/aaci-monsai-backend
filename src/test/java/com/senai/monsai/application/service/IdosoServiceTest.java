package com.senai.monsai.application.service;

import com.senai.monsai.application.dto.IdosoCreateDTO;
import com.senai.monsai.application.dto.IdosoUpdateDTO;
import com.senai.monsai.domain.entity.Asilo;
import com.senai.monsai.domain.entity.Dispositivo;
import com.senai.monsai.domain.entity.Idoso;
import com.senai.monsai.domain.entity.Usuario;
import com.senai.monsai.domain.exception.RecursoDuplicadoException;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Habilita o Mockito, SEM banco de dados!
class IdosoServiceTest {

    @InjectMocks
    private IdosoService idosoService; // O Serviço REAL que vamos testar

    // Abaixo são os "Dublês" (Mocks). Eles fingem ser o banco de dados.
    @Mock
    private IdosoRepository idosoRepository;
    @Mock
    private PulseiraRepository pulseiraRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private AsiloRepository asiloRepository;

    // Mocks para simular o usuário logado no Spring Security
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @BeforeEach
    void setupSecurity() {
        // Ensinando o mock a fingir que existe um usuário logado chamado "admin@teste.com"
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin@teste.com");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("1. Deve criar um idoso com sucesso")
    void deveCriarIdosoComSucesso() {
        // Arrange
        Long asiloId = 1L;
        IdosoCreateDTO dto = new IdosoCreateDTO("Maria", "999", "maria@email", "SN-999", asiloId);

        Asilo asiloMock = new Asilo();
        asiloMock.setId(asiloId);

        Usuario usuarioLogado = new Usuario();
        usuarioLogado.setAsilo(asiloMock);

        Dispositivo dispositivoSalvo = new Dispositivo();
        dispositivoSalvo.setSerial("SN-999");

        Idoso idosoSalvo = new Idoso();
        idosoSalvo.setNome("Maria");
        idosoSalvo.setCpf("999");

        // Ensinando o Mockito
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(usuarioLogado));
        when(asiloRepository.findById(asiloId)).thenReturn(Optional.of(asiloMock));
        when(idosoRepository.existsByCpf("999")).thenReturn(false); // CPF liberado!
        when(pulseiraRepository.save(any(Dispositivo.class))).thenReturn(dispositivoSalvo);
        when(idosoRepository.save(any(Idoso.class))).thenReturn(idosoSalvo);

        // Act
        Idoso resultado = idosoService.criarIdoso(dto);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNome()).isEqualTo("Maria");
        // Verifica se o save foi chamado exatamente 1 vez
        verify(idosoRepository, times(1)).save(any(Idoso.class));
    }
    @Test
    @DisplayName("2. Deve falhar se usuário tentar cadastrar idoso em outro asilo")
    void deveFalharCriacaoAsiloDiferente() {
        // Arrange
        Long asiloUsuario = 1L;
        Long asiloDestinoInvasao = 2L; // O usuário vai tentar salvar no asilo 2!
        IdosoCreateDTO dto = new IdosoCreateDTO("Invasor", "000", "inv@email", "SN-000", asiloDestinoInvasao);

        Asilo asiloDoUser = new Asilo(); asiloDoUser.setId(asiloUsuario);
        Asilo asiloAlvo = new Asilo(); asiloAlvo.setId(asiloDestinoInvasao);

        Usuario usuarioLogado = new Usuario();
        usuarioLogado.setAsilo(asiloDoUser);

        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(usuarioLogado));
        when(asiloRepository.findById(asiloDestinoInvasao)).thenReturn(Optional.of(asiloAlvo));

        // Act & Assert
        RegraNegocioException exception = assertThrows(RegraNegocioException.class, () -> {
            idosoService.criarIdoso(dto);
        });

        assertThat(exception.getMessage()).contains("Você não pode cadastrar um idoso em outro asilo");
        verify(idosoRepository, never()).save(any());
    }
    @Test
    @DisplayName("3. Deve listar TODOS os idosos se o usuário for SuperAdmin (sem asilo)")
    void deveListarTodosParaSuperAdmin() {
        // Arrange
        Usuario superAdmin = new Usuario();
        superAdmin.setAsilo(null); // SuperAdmin não tem asilo fixo

        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(superAdmin));
        when(idosoRepository.findAll()).thenReturn(List.of(new Idoso(), new Idoso()));

        // Act
        List<Idoso> resultado = idosoService.listarTodos();

        // Assert
        assertThat(resultado).hasSize(2);
        verify(idosoRepository, times(1)).findAll();
        verify(idosoRepository, never()).findByAsiloId(anyLong()); // Garante que não usou a busca restrita
    }
    @Test
    @DisplayName("4. Deve falhar ao tentar atualizar um idoso que está inativo")
    void deveFalharAoAtualizarIdosoInativo() {
        // Arrange
        Long idIdoso = 99L;
        IdosoUpdateDTO updateDto = new IdosoUpdateDTO("Nome Novo", "111", "novo@email");

        Usuario usuarioLogado = new Usuario();

        Idoso idosoInativo = new Idoso();
        idosoInativo.setId(idIdoso);
        idosoInativo.setAtivo(false); // IDOSO INATIVO!

        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(usuarioLogado));
        when(idosoRepository.findById(idIdoso)).thenReturn(Optional.of(idosoInativo));

        // Act & Assert
        RegraNegocioException exception = assertThrows(RegraNegocioException.class, () -> {
            idosoService.atualizarIdoso(idIdoso, updateDto);
        });

        assertThat(exception.getMessage()).contains("Não é possível editar os dados de um idoso inativo");
        verify(idosoRepository, never()).save(any());
    }
    @Test
    @DisplayName("5. Deve inativar idoso com sucesso e remover o dispositivo")
    void deveInativarIdosoComSucesso() {
        // Arrange
        Long asiloId = 1L;
        Long idIdoso = 50L;

        Asilo asilo = new Asilo(); asilo.setId(asiloId);

        Usuario usuarioLogado = new Usuario();
        usuarioLogado.setAsilo(asilo);

        Idoso idosoParaInativar = new Idoso();
        idosoParaInativar.setId(idIdoso);
        idosoParaInativar.setAsilo(asilo); // Mesmo asilo do usuário (Permitido)
        idosoParaInativar.setAtivo(true);
        idosoParaInativar.setDispositivo(new Dispositivo()); // Tem uma pulseira

        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(usuarioLogado));
        when(idosoRepository.findById(idIdoso)).thenReturn(Optional.of(idosoParaInativar));

        // Act
        idosoService.inativarIdoso(idIdoso);

        // Assert
        assertThat(idosoParaInativar.isAtivo()).isFalse(); // Ficou inativo
        assertThat(idosoParaInativar.getDispositivo()).isNull(); // Perdeu a pulseira
        verify(idosoRepository, times(1)).save(idosoParaInativar);
    }
}