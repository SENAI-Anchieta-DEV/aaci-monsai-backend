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
    @DisplayName("Deve buscar um usuário por e-mail com sucesso")
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
    @DisplayName("Deve retornar vazio ao buscar e-mail inexistente")
    void deveRetornarVazioParaEmailInexistente() {
        Optional<Usuario> resultado = usuarioRepository.findByEmail("naoexiste@gmail.com");

        assertThat(resultado).isNotPresent();
    }

    @Test
    @DisplayName("Não deve permitir salvar dois usuários com o mesmo e-mail")
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
    @DisplayName("Deve filtrar usuários por tipo")
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
    @DisplayName("Deve encontrar usuários por asilo")
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

    /**
     * Método auxiliar para evitar repetição de código (DRY)
     */
    private Usuario criarUsuario(String nome, String email, String cpf) {
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(email);
        u.setCpf(cpf);
        u.setSenha("123456");
        u.setAtivo(true);
        return u;
    }
}
