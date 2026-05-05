package com.senai.monsai.domain.repository;

import com.senai.monsai.domain.entity.Dispositivo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

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
}