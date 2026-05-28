package com.senai.monsai.domain.repository;

import com.senai.monsai.domain.entity.Asilo;
import com.senai.monsai.domain.entity.Usuario;
import com.senai.monsai.domain.enums.TipoUsuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@ActiveProfiles("test")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("1. Deve buscar um usuário por e-mail com sucesso")
    void findByEmailSucesso() {
        // Cenário
        Usuario usuario = criarUsuario("João", "joao@teste.com", "11122233344");
        entityManager.persist(usuario);

        // Ação
        Optional<Usuario> result = usuarioRepository.findByEmail("joao@teste.com");

        // Validação (Usando AssertJ para maior clareza)
        assertThat(result).isPresent();
        assertThat(result.get().getNome()).isEqualTo("João");
    }

    @Test
    @DisplayName("2. Deve retornar vazio ao buscar e-mail inexistente")
    void deveRetornarVazioParaEmailInexistente() {
        Optional<Usuario> resultado = usuarioRepository.findByEmail("naoexiste@gmail.com");

        assertThat(resultado).isNotPresent();
    }

    @Test
    @DisplayName("3. Não deve permitir salvar dois usuários com o mesmo e-mail")
    void erroEmailDuplicado() {
        // Primeiro usuário persistido
        Usuario u1 = criarUsuario("Primeiro", "duplicado@teste.com", "11111111111");
        entityManager.persist(u1);

        // Segundo usuário com mesmo e-mail
        Usuario u2 = criarUsuario("Segundo", "duplicado@teste.com", "22222222222");

        // Validação da exceção no saveAndFlush
        assertThrows(DataIntegrityViolationException.class, () -> {
            usuarioRepository.saveAndFlush(u2);
        });
    }

    @Test
    @DisplayName("4. Deve filtrar usuários por tipo")
    void deveBuscarUsuariosPorTipo() {
        Usuario u1 = criarUsuario("Gestor", "g1@teste.com", "33333333333");
        u1.setTipo(TipoUsuario.GESTOR);
        entityManager.persist(u1);

        List<Usuario> gestores = usuarioRepository.findByTipo(TipoUsuario.GESTOR);

        assertThat(gestores).hasSize(1)
                .extracting(Usuario::getNome)
                .contains("Gestor");
    }

    @Test
    @DisplayName("5. Deve encontrar usuários por asilo")
    void deveBuscarPorAsilo() {
        // Preparando o Asilo
        Asilo asilo = new Asilo();
        asilo.setNome("Asilo A");
        asilo.setCnpj("12345678000199");
        asilo = entityManager.persistAndFlush(asilo);

        // Preparando o Usuário vinculado
        Usuario u = criarUsuario("Funcionario", "func@teste.com", "44444444444");
        u.setAsilo(asilo);
        entityManager.persist(u);

        // Ação
        List<Usuario> resultado = usuarioRepository.findByAsiloId(asilo.getId());

        // Validação
        assertThat(resultado).isNotEmpty();
        assertThat(resultado.get(0).getAsilo().getNome()).isEqualTo("Asilo A");
    }

    @Test
    @DisplayName("6. Deve retornar true ao verificar CPF já cadastrado")
    void deveDetectarCpfJaCadastrado() {
        // Arrange
        Usuario usuario = criarUsuario("Titular", "titular@email.com", "99988877766");
        entityManager.persist(usuario);

        // Act
        boolean cpfExiste  = usuarioRepository.existsByCpf("99988877766");
        boolean cpfNaoExiste = usuarioRepository.existsByCpf("00000000000");

        // Assert
        assertThat(cpfExiste).isTrue();
        assertThat(cpfNaoExiste).isFalse();
    }

    @Test
    @DisplayName("7. Deve cadastrar e recuperar usuário com perfil FAMILIAR")
    void deveCadastrarFamiliar() {
        // Arrange
        Usuario familiar = criarUsuario("Ana Familiar", "ana@familia.com", "12312312312");
        familiar.setTipo(TipoUsuario.FAMILIAR);
        entityManager.persist(familiar);

        // Act
        List<Usuario> familiares = usuarioRepository.findByTipo(TipoUsuario.FAMILIAR);

        // Assert
        assertThat(familiares).hasSize(1);
        assertThat(familiares.get(0).getNome()).isEqualTo("Ana Familiar");
        assertThat(familiares.get(0).getTipo()).isEqualTo(TipoUsuario.FAMILIAR);
    }

    @Test
    @DisplayName("8. Deve listar apenas usuários do tipo GESTOR ativos no asilo")
    void deveListarGestoresPorTipo() {
        // Arrange — 1 GESTOR e 1 CUIDADOR no mesmo asilo
        Usuario gestor = criarUsuario("Carlos Gestor", "carlos@asilo.com", "55566677788");
        gestor.setTipo(TipoUsuario.GESTOR);
        entityManager.persist(gestor);

        Usuario cuidador = criarUsuario("Paula Cuidadora", "paula@asilo.com", "11122233344");
        cuidador.setTipo(TipoUsuario.CUIDADOR);
        entityManager.persist(cuidador);

        // Act
        List<Usuario> gestores  = usuarioRepository.findByTipo(TipoUsuario.GESTOR);
        List<Usuario> cuidadores = usuarioRepository.findByTipo(TipoUsuario.CUIDADOR);

        // Assert
        assertThat(gestores).hasSize(1);
        assertThat(cuidadores).hasSize(1);
        assertThat(gestores.get(0).getNome()).isEqualTo("Carlos Gestor");
    }

    @Test
    @DisplayName("9. Deve atualizar dados cadastrais do usuário com sucesso")
    void deveAtualizarDadosCadastraisDoUsuario() {
        // Arrange
        Usuario usuario = criarUsuario("Nome Antigo", "antigo@email.com", "77788899900");
        Usuario salvo = usuarioRepository.saveAndFlush(usuario);

        // Act — Gestor atualiza nome e email
        salvo.setNome("Nome Atualizado");
        salvo.setEmail("atualizado@email.com");
        Usuario atualizado = usuarioRepository.saveAndFlush(salvo);

        // Assert
        assertThat(atualizado.getNome()).isEqualTo("Nome Atualizado");
        assertThat(atualizado.getEmail()).isEqualTo("atualizado@email.com");
    }

    @Test
    @DisplayName("10. Deve desativar usuário e confirmar status ativo = false")
    void deveDesativarUsuario() {
        // Arrange
        Usuario usuario = criarUsuario("Usuário Ativo", "ativo@email.com", "33344455566");
        usuario.setAtivo(true);
        Usuario salvo = usuarioRepository.saveAndFlush(usuario);

        // Act — administrador desativa o usuario
        salvo.setAtivo(false);
        Usuario desativado = usuarioRepository.saveAndFlush(salvo);
        entityManager.clear();

        Usuario encontrado = usuarioRepository.findById(desativado.getId()).orElseThrow();

        // Assert
        assertThat(encontrado.isAtivo()).isFalse();
    }

    //DRY
    private Usuario criarUsuario(String nome, String email, String cpf) {
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(email);
        u.setCpf(cpf);
        u.setSenha("monsai123");
        u.setAtivo(true);
        return u;
    }

}