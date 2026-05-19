package com.senai.monsai.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.support.GenericMessage;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MensagemMqttServiceTest {

    @Mock
    private TelemetriaService telemetriaService;

    @InjectMocks
    private MensagemMqttService mensagemMqttService;

    // =========================================================
    // EDGE CASE 1: Receber JSON Mqtt Válido com sucesso
    // =========================================================
    @Test
    @DisplayName("AACI-114: Mapeamento de Payload MQTT válido")
    void deveMapearEProcessarJsonVálido() {
        // GIVEN: Um JSON simulando a pulseira IoT
        String payloadJson = "{"
                + "\"idoso_id\": 1,"
                + "\"pulseira_id\": \"MON-313\","
                + "\"data_hora\": \"2026-03-10T11:00:00\","
                + "\"sinal_vital\": {"
                + "  \"sinal_vital_id\": \"SV-001\","
                + "  \"frequencia_cardiaca_bpm\": 75,"
                + "  \"temperatura_c\": 36.5,"
                + "  \"movimento\": {"
                + "    \"aceleracao\": {\"x\": 0.1, \"y\": 0.0, \"z\": 9.8},"
                + "    \"queda_detectada\": false"
                + "  }"
                + "},"
                + "\"localizacao\": {\"latitude\": -23.55, \"longitude\": -46.63, \"precisao_metro\": 5.0},"
                + "\"status_do_dispositivo\": {"
                + "  \"status_id\": \"ST-001\","
                + "  \"ultimo_contato\": \"2026-03-10T10:59:00\","
                + "  \"nivel_bateria\": 85,"
                + "  \"status_pulseira\": \"ATIVO\""
                + "}"
                + "}";

        GenericMessage<String> message = new GenericMessage<>(payloadJson);

        // WHEN & THEN
        assertDoesNotThrow(() -> mensagemMqttService.escutarHardware(message));
        verify(telemetriaService, times(1)).processarTelemetria(any());
    }

    // =========================================================
    // EDGE CASE 2: Receber JSON Mal Formatado
    // =========================================================
    @Test
    @DisplayName("AACI-114: Json mal formatado é capturado pelo Catch sem quebrar o listener")
    void deveCapturarExceptionComJsonInvalido() {
        String payloadQuebrado = "{ \"idoso_id\": 1, ERRO SINTAXE }";
        GenericMessage<String> message = new GenericMessage<>(payloadQuebrado);

        // O método tem um try-catch. Não deve lançar a exceção para fora.
        assertDoesNotThrow(() -> mensagemMqttService.escutarHardware(message));

        // E o service interno nunca deve ser chamado
        verify(telemetriaService, never()).processarTelemetria(any());
    }
}