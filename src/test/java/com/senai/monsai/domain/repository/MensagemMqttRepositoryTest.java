package com.senai.monsai.domain.repository;

import com.senai.monsai.domain.entity.Dispositivo;
import com.senai.monsai.domain.entity.MensagemMqtt;
import com.senai.monsai.domain.enums.StatusDispositivo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class MensagemMqttRepositoryTest {

    @Autowired
    private MensagemMqttRepository mensagemMqttRepository;

    @Autowired
    private DispositivoRepository dispositivoRepository;

    private Dispositivo dispositivoSalvo;

    @BeforeEach
    void setup() {
        // MensagemMqtt precisa de um Dispositivo persistido devido à FK
        Dispositivo dispositivo = Dispositivo.builder()
                .serial("SN-12345")
                .statusDispositivo(StatusDispositivo.ATIVO)
                .nivelBateria(90)
                .ultimoContato(LocalDateTime.now())
                .build();

        this.dispositivoSalvo = dispositivoRepository.save(dispositivo);
    }

    @Test
    @DisplayName("Deve persistir uma mensagem de telemetria com sucesso")
    void deveSalvarMensagem() {
        // Arrange
        MensagemMqtt mensagem = new MensagemMqtt();
        mensagem.setDispositivo(dispositivoSalvo);
        mensagem.setFrequenciaCardiaca(80);
        mensagem.setTemperatura(36.5);
        mensagem.setQuedaDetectada(false);
        mensagem.setLatitude(-23.55);
        mensagem.setLongitude(-46.63);
        mensagem.setDataHoraEvento(LocalDateTime.now());
        mensagem.setDataRecebimento(LocalDateTime.now());

        // Act
        MensagemMqtt salva = mensagemMqttRepository.save(mensagem);

        // Assert
        assertThat(salva.getId()).isNotNull();
        assertThat(salva.getDispositivo().getSerial()).isEqualTo("SN-12345");
        assertThat(salva.getFrequenciaCardiaca()).isEqualTo(80);
    }

    @Test
    @DisplayName("Deve buscar uma mensagem pelo ID")
    void deveBuscarPorId() {
        // Arrange
        MensagemMqtt mensagem = new MensagemMqtt();
        mensagem.setDispositivo(dispositivoSalvo);
        mensagem.setDataHoraEvento(LocalDateTime.now());
        mensagem.setDataRecebimento(LocalDateTime.now());

        MensagemMqtt salva = mensagemMqttRepository.save(mensagem);

        Long idGerado = salva.getId();

        // O findById espera um Long se o seu repositório for JpaRepository<MensagemMqtt, Long>
        Optional<MensagemMqtt> encontrada = mensagemMqttRepository.findById(idGerado);

        // Assert
        assertThat(encontrada).isPresent();
        // Comparando Long com Long (1L == 1L)
        assertThat(encontrada.get().getId()).isEqualTo(idGerado);
    }
}