package com.senai.monsai.domain.repository;

import com.senai.monsai.domain.entity.Dispositivo;
import com.senai.monsai.domain.entity.MensagemMqtt;
import com.senai.monsai.domain.enums.StatusDispositivo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    @DisplayName("1. Deve persistir uma mensagem de telemetria com sucesso")
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
    @DisplayName("2. Deve buscar uma mensagem pelo ID")
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

    @Test
    @DisplayName("3. Deve lançar exceção ao salvar mensagem sem dispositivo associado")
    void deveFalharAoSalvarMensagemSemDispositivo() {
        // Arrange — dispositivo = null, coluna FK nullable = false
        MensagemMqtt semDispositivo = new MensagemMqtt();
        semDispositivo.setFrequenciaCardiaca(75);
        semDispositivo.setTemperatura(36.5);
        semDispositivo.setDataHoraEvento(LocalDateTime.now());
        semDispositivo.setDataRecebimento(LocalDateTime.now());
        // dispositivo propositalmente nulo

        // Act & Assert
        assertThrows(DataIntegrityViolationException.class, () -> {
            mensagemMqttRepository.saveAndFlush(semDispositivo);
        });
    }

    @Test
    @DisplayName("4. Deve persistir mensagem com queda detectada = true para alerta crítico")
    void deveSalvarMensagemComQuedaDetectada() {
        // Arrange
        MensagemMqtt quedaMsg = new MensagemMqtt();
        quedaMsg.setDispositivo(dispositivoSalvo);
        quedaMsg.setQuedaDetectada(true);
        quedaMsg.setFrequenciaCardiaca(110);
        quedaMsg.setTemperatura(37.1);
        quedaMsg.setDataHoraEvento(LocalDateTime.now());
        quedaMsg.setDataRecebimento(LocalDateTime.now());

        // Act
        MensagemMqtt salva = mensagemMqttRepository.saveAndFlush(quedaMsg);

        // Assert
        assertThat(salva.getId()).isNotNull();
        assertThat(salva.getQuedaDetectada()).isTrue();
    }

    @Test
    @DisplayName("5. Deve persistir coordenadas de localização (lat/lng) na mensagem MQTT")
    void deveSalvarCoordenadasDeLocalizacao() {
        // Arrange — coordenadas reais de São Paulo
        double latEsperada = -23.550520;
        double lngEsperada = -46.633308;

        MensagemMqtt localizacao = new MensagemMqtt();
        localizacao.setDispositivo(dispositivoSalvo);
        localizacao.setLatitude(latEsperada);
        localizacao.setLongitude(lngEsperada);
        localizacao.setDataHoraEvento(LocalDateTime.now());
        localizacao.setDataRecebimento(LocalDateTime.now());

        // Act
        MensagemMqtt salva = mensagemMqttRepository.saveAndFlush(localizacao);

        // Assert
        assertThat(salva.getLatitude()).isEqualTo(latEsperada);
        assertThat(salva.getLongitude()).isEqualTo(lngEsperada);
    }

    @Test
    @DisplayName("6. Deve salvar múltiplas mensagens do mesmo dispositivo sem conflito")
    void deveSalvarMultiplasMensagensDoMesmoDispositivo() {
        // Arrange — 3 mensagens sequenciais do mesmo sensor
        for (int i = 1; i <= 3; i++) {
            MensagemMqtt msg = new MensagemMqtt();
            msg.setDispositivo(dispositivoSalvo);
            msg.setFrequenciaCardiaca(70 + i);
            msg.setTemperatura(36.0 + (i * 0.1));
            msg.setDataHoraEvento(LocalDateTime.now().plusSeconds(i));
            msg.setDataRecebimento(LocalDateTime.now().plusSeconds(i));
            mensagemMqttRepository.save(msg);
        }
        mensagemMqttRepository.flush();

        // Act
        List<MensagemMqtt> todas = mensagemMqttRepository.findAll();

        // Assert — deve haver ao menos as 3 mensagens persistidas
        assertThat(todas).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("7. Deve persistir dataHoraEvento e dataRecebimento para auditoria de latência")
    void deveSalvarTimestampsParaAuditoriaDeLatencia() {
        // Arrange
        LocalDateTime momentoSensor   = LocalDateTime.of(2025, 1, 10, 10, 0, 0);
        LocalDateTime momentoBackend  = LocalDateTime.of(2025, 1, 10, 10, 0, 0, 500_000_000); // +500ms

        MensagemMqtt msg = new MensagemMqtt();
        msg.setDispositivo(dispositivoSalvo);
        msg.setDataHoraEvento(momentoSensor);
        msg.setDataRecebimento(momentoBackend);
        msg.setFrequenciaCardiaca(72);

        // Act
        MensagemMqtt salva = mensagemMqttRepository.saveAndFlush(msg);

        // Assert
        assertThat(salva.getDataHoraEvento()).isEqualTo(momentoSensor);
        assertThat(salva.getDataRecebimento()).isEqualTo(momentoBackend);
        // O backend recebeu DEPOIS do sensor → dataRecebimento > dataHoraEvento
        assertThat(salva.getDataRecebimento()).isAfter(salva.getDataHoraEvento());
    }


}