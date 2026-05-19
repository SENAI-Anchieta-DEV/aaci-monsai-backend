package com.senai.monsai.ui_interface.controller;

import com.senai.monsai.application.dto.TelemetriaDTO;
import com.senai.monsai.application.service.TelemetriaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MensagemMqttControllerTest {

    @Mock
    private TelemetriaService telemetriaService;

    @InjectMocks
    private MensagemMqttController mensagemMqttController;

    // =========================================================
    // EDGE CASE 1: Simulação do envio de telemetria
    // =========================================================
    @Test
    @DisplayName("AACI-114: Endpoint de simulação Mqtt processa e retorna 200 OK")
    void deveSimularEnvioDeTelemetriaComSucesso() {
        // GIVEN (Mock básico de telemetria)
        TelemetriaDTO dto = mock(TelemetriaDTO.class);
        doNothing().when(telemetriaService).processarTelemetria(dto);

        // WHEN
        ResponseEntity<String> response = mensagemMqttController.dispararSimulacao(dto);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody(), "A mensagem de resposta não deve ser nula");
        assertEquals("Simulação processada com sucesso. Verifique os alertas e o banco de dados.", response.getBody());
        verify(telemetriaService, times(1)).processarTelemetria(dto);
    }
}