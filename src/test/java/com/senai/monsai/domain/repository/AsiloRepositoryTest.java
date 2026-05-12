package com.senai.monsai.domain.repository;

import com.senai.monsai.domain.entity.Asilo;
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
class AsiloRepositoryTest {

    @Autowired
    private AsiloRepository asiloRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("1. Deve salvar asilo com todos os campos preenchidos")
    void deveSalvarAsiloCompleto() {
        Asilo asilo = new Asilo();
        asilo.setNome("Lar de Teste");
        asilo.setCnpj("12.345.678/0001-00");
        asilo.setEndereco("Rua das Flores, 123");
        asilo.setAtivo(true);

        Asilo salvo = asiloRepository.save(asilo);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getNome()).isEqualTo("Lar de Teste");
    }

    @Test
    @DisplayName("2. Deve falhar ao salvar asilos com CNPJs idênticos")
    void deveFalharCnpjDuplicado() {
        String cnpj = "11.111.111/0001-11";

        Asilo asilo1 = new Asilo();
        asilo1.setNome("Asilo A");
        asilo1.setCnpj(cnpj);
        entityManager.persistAndFlush(asilo1);

        Asilo asilo2 = new Asilo();
        asilo2.setNome("Asilo B");
        asilo2.setCnpj(cnpj);

        // O flush força o banco a validar a constraint de unique
        assertThrows(DataIntegrityViolationException.class, () -> {
            asiloRepository.saveAndFlush(asilo2);
        });
    }

    @Test
    @DisplayName("3. Deve encontrar asilo pelo CNPJ via método exists")
    void deveVerificarExistenciaCnpj() {
        Asilo asilo = new Asilo();
        asilo.setNome("Asilo Existe");
        asilo.setCnpj("22.222.222/0001-22");
        entityManager.persist(asilo);

        boolean existe = asiloRepository.existsByCnpj("22.222.222/0001-22");
        boolean naoExiste = asiloRepository.existsByCnpj("00.000.000/0000-00");

        assertThat(existe).isTrue();
        assertThat(naoExiste).isFalse();
    }

    @Test
    @DisplayName("4. Deve desativar um asilo (Soft Delete)")
    void deveAlterarStatusAtivo() {
        Asilo asilo = new Asilo();
        asilo.setNome("Asilo Ativo");
        asilo.setCnpj("33.333.333/0001-33");
        asilo.setAtivo(true);
        Asilo salvo = asiloRepository.save(asilo);

        salvo.setAtivo(false);
        Asilo atualizado = asiloRepository.saveAndFlush(salvo);

        assertThat(atualizado.isAtivo()).isFalse();
    }

    @Test
    @DisplayName("5. Deve retornar vazio ao buscar por ID inexistente")
    void deveRetornarVazioParaIdFicticio() {
        Optional<Asilo> resultado = asiloRepository.findById(9999L);
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("6. Deve atualizar nome e endereço do asilo com sucesso")
    void deveAtualizarDadosCadastraisDoAsilo() {
        // Arrange
        Asilo asilo = new Asilo();
        asilo.setNome("Nome Antigo");
        asilo.setCnpj("44.444.444/0001-44");
        asilo.setEndereco("Rua Velha, 1");
        Asilo salvo = asiloRepository.saveAndFlush(asilo);

        // Act
        salvo.setNome("Nome Atualizado");
        salvo.setEndereco("Av. Nova, 200");
        Asilo atualizado = asiloRepository.saveAndFlush(salvo);

        // Assert
        assertThat(atualizado.getNome()).isEqualTo("Nome Atualizado");
        assertThat(atualizado.getEndereco()).isEqualTo("Av. Nova, 200");
    }

    @Test
    @DisplayName("7. Deve lançar exceção ao tentar salvar asilo sem CNPJ")
    void deveFalharAoSalvarAsiloSemCnpj() {
        // Arrange
        Asilo semCnpj = new Asilo();
        semCnpj.setNome("Asilo Sem CNPJ");
        // CNPJ não preenchido — coluna nullable = false

        // Act & Assert
        assertThrows(DataIntegrityViolationException.class, () -> {
            asiloRepository.saveAndFlush(semCnpj);
        });
    }

    @Test
    @DisplayName("8. Deve listar todos os asilos cadastrados no sistema")
    void deveListarTodosOsAsilos() {
        // Arrange
        Asilo a1 = new Asilo();
        a1.setNome("Asilo Alpha");
        a1.setCnpj("55.555.555/0001-55");

        Asilo a2 = new Asilo();
        a2.setNome("Asilo Beta");
        a2.setCnpj("66.666.666/0001-66");

        entityManager.persist(a1);
        entityManager.persist(a2);
        entityManager.flush();

        // Act
        List<Asilo> todos = asiloRepository.findAll();

        // Assert — ao menos os 2 que acabamos de inserir existem
        assertThat(todos).hasSizeGreaterThanOrEqualTo(2);
        assertThat(todos).extracting(Asilo::getNome)
                .contains("Asilo Alpha", "Asilo Beta");
    }

    @Test
    @DisplayName("9. Deve deletar um asilo e confirmar sua ausência")
    void deveDeletarAsiloEConfirmarAusencia() {
        // Arrange
        Asilo asilo = new Asilo();
        asilo.setNome("Asilo Para Deletar");
        asilo.setCnpj("77.777.777/0001-77");
        Asilo salvo = entityManager.persistAndFlush(asilo);
        Long id = salvo.getId();

        // Act
        asiloRepository.deleteById(id);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Asilo> deletado = asiloRepository.findById(id);
        assertThat(deletado).isEmpty();
    }

    @Test
    @DisplayName("10. Deve reativar um asilo que estava desativado")
    void deveReativarAsiloDesativado() {
        // Arrange
        Asilo asilo = new Asilo();
        asilo.setNome("Asilo Inativo");
        asilo.setCnpj("88.888.888/0001-88");
        asilo.setAtivo(false);
        Asilo salvo = asiloRepository.saveAndFlush(asilo);

        // Act
        salvo.setAtivo(true);
        Asilo reativado = asiloRepository.saveAndFlush(salvo);

        // Assert
        assertThat(reativado.isAtivo()).isTrue();
    }
}