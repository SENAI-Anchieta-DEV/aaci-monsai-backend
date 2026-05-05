package com.senai.monsai.domain.repository;

import com.senai.monsai.domain.entity.Asilo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

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
}