package com.senai.monsai.domain.repository;

import com.senai.monsai.domain.entity.Dispositivo;
import com.senai.monsai.domain.enums.StatusDispositivo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@ActiveProfiles("test")
class DispositivoRepositoryTest {

    @Autowired
    private DispositivoRepository dispositivoRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("1. Deve persistir dispositivo com sucesso")
    void deveSalvarDispositivo() {
        // ARRANGE: Note que NÃO passamos o .id()
        Dispositivo dispositivo = Dispositivo.builder()
                .serial("SN-POWER-99")
                .nivelBateria(100)
                .build();

        // ACT
        Dispositivo salvo = dispositivoRepository.save(dispositivo);

        // ASSERT
        assertThat(salvo.getId()).isNotNull(); // O ID foi gerado automaticamente
        assertThat(salvo.getSerial()).isEqualTo("SN-POWER-99");
    }
    @Test
    @DisplayName("2. Deve buscar dispositivo pelo ID gerado")
    void deveBuscarPorId() {
        // Arrange - Criamos SEM ID
        Dispositivo dispositivo = Dispositivo.builder()
                .serial("SERIAL-01")
                .build();
        // O persist retorna a entidade já com o ID preenchido pelo banco
        Dispositivo salvo = entityManager.persistFlushFind(dispositivo);

        // Act - Usamos o ID que o banco criou
        Optional<Dispositivo> encontrado = dispositivoRepository.findById(salvo.getId());

        // Assert
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getSerial()).isEqualTo("SERIAL-01");
    }

    @Test
    @DisplayName("3. Deve falhar ao salvar dispositivo com serial duplicado")
    void deveFalharSerialDuplicado() {
        // Arrange
        String serialRepetido = "ABC-123";
        Dispositivo d1 = Dispositivo.builder().serial(serialRepetido).build();
        entityManager.persistAndFlush(d1);

        // Tentamos criar outro objeto com o mesmo serial
        Dispositivo d2 = Dispositivo.builder().serial(serialRepetido).build();

        // Act & Assert
        assertThrows(DataIntegrityViolationException.class, () -> {
            dispositivoRepository.saveAndFlush(d2);
        });
    }

    @Test
    @DisplayName("4. Deve atualizar o nível da bateria usando ID dinâmico")
    void deveAtualizarBateria() {
        // Arrange
        Dispositivo dispositivo = Dispositivo.builder()
                .serial("SN-BATT")
                .nivelBateria(50)
                .build();
        Dispositivo salvo = dispositivoRepository.save(dispositivo);

        // Act
        salvo.setNivelBateria(25);
        Dispositivo atualizado = dispositivoRepository.saveAndFlush(salvo);

        // Assert
        assertThat(atualizado.getNivelBateria()).isEqualTo(25);
    }

    @Test
    @DisplayName("5. Deve deletar dispositivo e confirmar ausência")
    void deveDeletarDispositivo() {
        // Arrange
        Dispositivo dispositivo = Dispositivo.builder().serial("SN-DEL").build();
        Dispositivo salvo = entityManager.persistAndFlush(dispositivo);
        String idGerado = salvo.getId();

        // Act
        dispositivoRepository.deleteById(idGerado);
        entityManager.flush(); // Garante que o delete foi pro banco
        entityManager.clear(); // Limpa o cache para forçar a busca no banco

        // Assert
        Optional<Dispositivo> deletado = dispositivoRepository.findById(idGerado);
        assertThat(deletado).isEmpty();
    }

    @Test
    @DisplayName("6. Deve atualizar status do dispositivo para ATIVO ao reconectar")
    void deveAtualizarStatusParaAtivo() {
        // Arrange
        Dispositivo dispositivo = Dispositivo.builder()
                .serial("SN-OFFLINE-01")
                .statusDispositivo(StatusDispositivo.INATIVO)
                .nivelBateria(40)
                .build();
        Dispositivo salvo = dispositivoRepository.save(dispositivo);

        // Act — simula reconexão
        salvo.setStatusDispositivo(StatusDispositivo.ATIVO);
        salvo.setUltimoContato(LocalDateTime.now());
        Dispositivo atualizado = dispositivoRepository.saveAndFlush(salvo);

        // Assert
        assertThat(atualizado.getStatusDispositivo()).isEqualTo(StatusDispositivo.ATIVO);
        assertThat(atualizado.getUltimoContato()).isNotNull();
    }

    @Test
    @DisplayName("7. Deve registrar status ALERTA no dispositivo ao detectar anormalidade")
    void deveRegistrarStatusAlerta() {
        // Arrange
        Dispositivo dispositivo = Dispositivo.builder()
                .serial("SN-ALERTA-01")
                .statusDispositivo(StatusDispositivo.ATIVO)
                .nivelBateria(80)
                .build();
        Dispositivo salvo = dispositivoRepository.save(dispositivo);

        // Act — sistema detecta queda e sinaliza alerta
        salvo.setStatusDispositivo(StatusDispositivo.ALERTA);
        Dispositivo emAlerta = dispositivoRepository.saveAndFlush(salvo);

        // Assert
        assertThat(emAlerta.getStatusDispositivo()).isEqualTo(StatusDispositivo.ALERTA);
    }
    @Test
    @DisplayName("8. Deve persistir e recuperar o campo ultimoContato corretamente")
    void devePersistirUltimoContato() {
        // Arrange
        LocalDateTime agora = LocalDateTime.now().withNano(0); // sem nanos para comparação precisa
        Dispositivo dispositivo = Dispositivo.builder()
                .serial("SN-CONTATO-01")
                .ultimoContato(agora)
                .nivelBateria(60)
                .build();

        // Act
        Dispositivo salvo = dispositivoRepository.saveAndFlush(dispositivo);
        entityManager.clear();
        Optional<Dispositivo> encontrado = dispositivoRepository.findById(salvo.getId());

        // Assert
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getUltimoContato()).isEqualTo(agora);
    }
    @Test
    @DisplayName("9. Deve listar todos os dispositivos cadastrados")
    void deveListarTodosOsDispositivos() {
        // Arrange
        Dispositivo d1 = Dispositivo.builder().serial("SN-LIST-01").build();
        Dispositivo d2 = Dispositivo.builder().serial("SN-LIST-02").build();
        Dispositivo d3 = Dispositivo.builder().serial("SN-LIST-03").build();

        entityManager.persist(d1);
        entityManager.persist(d2);
        entityManager.persist(d3);
        entityManager.flush();

        // Act
        List<Dispositivo> todos = dispositivoRepository.findAll();

        // Assert
        assertThat(todos).hasSizeGreaterThanOrEqualTo(3);
        assertThat(todos).extracting(Dispositivo::getSerial)
                .contains("SN-LIST-01", "SN-LIST-02", "SN-LIST-03");
    }

    @Test
    @DisplayName("10. Deve lançar exceção ao tentar salvar dispositivo sem serial")
    void deveFalharAoSalvarDispositivoSemSerial() {
        // Arrange
        Dispositivo semSerial = Dispositivo.builder()
                .nivelBateria(50)
                .statusDispositivo(StatusDispositivo.INATIVO)
                .build();
        // serial = null, coluna nullable = false

        // Act & Assert
        assertThrows(DataIntegrityViolationException.class, () -> {
            dispositivoRepository.saveAndFlush(semSerial);
        });
    }
}