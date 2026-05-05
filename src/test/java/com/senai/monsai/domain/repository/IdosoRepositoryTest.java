package com.senai.monsai.domain.repository;

import com.senai.monsai.domain.entity.Asilo;
import com.senai.monsai.domain.entity.Dispositivo;
import com.senai.monsai.domain.entity.Idoso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@ActiveProfiles("test")
class IdosoRepositoryTest {

    @Autowired
    private IdosoRepository idosoRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Asilo asiloSalvo;

    @BeforeEach
    void setup() {
        // Criando o asilo necessário para o vínculo do Idoso
        Asilo asilo = new Asilo();
        asilo.setNome("Recanto do Sol");
        asilo.setCnpj("12345678000199");
        this.asiloSalvo = entityManager.persist(asilo);
    }

    @Test
    @DisplayName("1. Deve confirmar que CPF já existe")
    void deveConfirmarExistenciaCpf() {
        // Arrange
        Idoso idoso = Idoso.builder()
                .nome("Seu João")
                .cpf("12345678900")
                .asilo(asiloSalvo)
                .build();
        entityManager.persist(idoso);

        // Act
        boolean existe = idosoRepository.existsByCpf("12345678900");
        boolean naoExiste = idosoRepository.existsByCpf("00000000000");

        // Assert
        assertThat(existe).isTrue();
        assertThat(naoExiste).isFalse();
    }

    @Test
    @DisplayName("2. Deve buscar todos os idosos de um asilo específico")
    void deveBuscarPorAsiloId() {
        // Arrange
        Idoso i1 = Idoso.builder().nome("Idoso A").cpf("111").asilo(asiloSalvo).build();
        Idoso i2 = Idoso.builder().nome("Idoso B").cpf("222").asilo(asiloSalvo).build();

        Asilo outroAsilo = new Asilo();
        outroAsilo.setNome("Outro Lugar");
        outroAsilo.setCnpj("99999999000199");
        entityManager.persist(outroAsilo);

        Idoso i3 = Idoso.builder().nome("Idoso C").cpf("333").asilo(outroAsilo).build();

        entityManager.persist(i1);
        entityManager.persist(i2);
        entityManager.persist(i3);

        // Act
        List<Idoso> resultado = idosoRepository.findByAsiloId(asiloSalvo.getId());

        // Assert
        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(Idoso::getNome).containsExactlyInAnyOrder("Idoso A", "Idoso B");
        assertThat(resultado).extracting(Idoso::getNome).doesNotContain("Idoso C");
    }
    @Test
    @DisplayName("3. Deve lançar exceção ao salvar CPF duplicado")
    void deveFalharAoSalvarCpfDuplicado() {
        Idoso idoso1 = Idoso.builder().nome("João").cpf("123").asilo(asiloSalvo).build();
        entityManager.persistAndFlush(idoso1);

        Idoso idoso2 = Idoso.builder().nome("Maria").cpf("123").asilo(asiloSalvo).build();
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> {
            idosoRepository.saveAndFlush(idoso2);
        });
    }

    @Test
    @DisplayName("4. Deve impedir salvar idoso sem vínculo com asilo")
    void deveFalharAoSalvarSemAsilo() {
        // Arrange
        Idoso semAsilo = Idoso.builder()
                .nome("Sem Teto")
                .cpf("999")
                .asilo(null)
                .build();

        // Act & Assert
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> {
            idosoRepository.saveAndFlush(semAsilo);
        });
    }

    @Test
    @DisplayName("5. Deve retornar lista vazia para asilo que não possui idosos")
    void deveRetornarVazioParaAsiloSemIdosos() {
        Asilo novoAsilo = new Asilo();
        novoAsilo.setNome("Asilo Vazio");
        novoAsilo.setCnpj("00000000000000");
        entityManager.persist(novoAsilo);
        List<Long> idosos = idosoRepository.findByAsiloId(novoAsilo.getId())
                .stream().map(Idoso::getId).toList();
        assertThat(idosos).isEmpty();
    }

    @Test
    @DisplayName("6. Deve garantir que idosos não 'vazam' entre asilos diferentes")
    void deveGarantirIsolamentoEntreAsilos() {
        Asilo asiloA = asiloSalvo;
        Asilo asiloB = new Asilo();
        asiloB.setNome("Asilo B");
        asiloB.setCnpj("11111111111111");
        entityManager.persist(asiloB);

        Idoso idosoA = Idoso.builder().nome("Idoso A").cpf("A1").asilo(asiloA).build();
        Idoso idosoB = Idoso.builder().nome("Idoso B").cpf("B1").asilo(asiloB).build();
        entityManager.persist(idosoA);
        entityManager.persist(idosoB);
        List<Idoso> resultadoA = idosoRepository.findByAsiloId(asiloA.getId());
        assertThat(resultadoA).hasSize(1);
        assertThat(resultadoA.get(0).getNome()).isEqualTo("Idoso A");
        assertThat(resultadoA).extracting(Idoso::getNome).doesNotContain("Idoso B");
    }

    @Test
    @DisplayName("7. Deve permitir deletar um idoso sem afetar o asilo")
    void deveDeletarIdosoEManterAsilo() {
        Idoso idoso = Idoso.builder().nome("Para Deletar").cpf("DEL").asilo(asiloSalvo).build();
        idoso = entityManager.persistAndFlush(idoso);
        Long idIdoso = idoso.getId();
        Long idAsilo = asiloSalvo.getId();

        idosoRepository.deleteById(idIdoso);
        entityManager.flush();
        entityManager.clear();

        assertThat(idosoRepository.findById(idIdoso)).isEmpty();
        assertThat(entityManager.find(Asilo.class, idAsilo)).isNotNull();
    }
    @Test
    @DisplayName("8. Deve permitir vários idosos no mesmo asilo")
    void devePermitirVariosIdososNoMesmoAsilo() {
        Idoso i1 = Idoso.builder().nome("Idoso 1").cpf("CPF1").asilo(asiloSalvo).build();
        Idoso i2 = Idoso.builder().nome("Idoso 2").cpf("CPF2").asilo(asiloSalvo).build();

        idosoRepository.saveAndFlush(i1);
        idosoRepository.saveAndFlush(i2);

        assertThat(idosoRepository.findByAsiloId(asiloSalvo.getId())).hasSize(2);
    }

    @Test
    @DisplayName("9. Deve encontrar idoso por CPF")
    void deveBuscarPorCpf() {
        String cpf = "123.456.789-00";
        Idoso idoso = Idoso.builder().nome("Teste").cpf(cpf).asilo(asiloSalvo).build();
        entityManager.persist(idoso);
        boolean existe = idosoRepository.existsByCpf(cpf);

        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("10. Deve persistir idoso com dispositivo (OneToOne)")
    void deveSalvarIdosoComDispositivo() {
        Dispositivo disp = Dispositivo.builder().serial("XYZ-999").build();
        // Não precisamos salvar o dispositivo antes se o cascade for ALL

        Idoso idoso = Idoso.builder()
                .nome("Idoso Tech")
                .cpf("CPF_TECH")
                .asilo(asiloSalvo)
                .dispositivo(disp)
                .build();

        Idoso salvo = idosoRepository.save(idoso);

        assertThat(salvo.getDispositivo()).isNotNull();
        assertThat(salvo.getDispositivo().getSerial()).isEqualTo("XYZ-999");
    }

    @Test
    @DisplayName("11. Deve atualizar status ativo do idoso")
    void deveAtualizarStatusAtivo() {
        Idoso idoso = Idoso.builder().nome("Ativo").cpf("CPF_A").asilo(asiloSalvo).ativo(true).build();
        idoso = idosoRepository.save(idoso);

        idoso.setAtivo(false);
        Idoso atualizado = idosoRepository.saveAndFlush(idoso);

        assertThat(atualizado.isAtivo()).isFalse();
    }

    @Test
    @DisplayName("12. Deve falhar ao buscar idoso por ID de asilo inexistente")
    void deveRetornarVazioParaAsiloInexistente() {
        // ID 999L provavelmente não existe
        List<Idoso> resultado = idosoRepository.findByAsiloId(999L);
        assertThat(resultado).isEmpty();
    }
}