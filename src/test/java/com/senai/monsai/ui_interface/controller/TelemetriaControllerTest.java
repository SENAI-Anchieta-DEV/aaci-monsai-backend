package com.senai.monsai.ui_interface.controller;

import com.senai.monsai.application.dto.AlertaDTO;
import com.senai.monsai.application.dto.TelemetriaDTO;
import com.senai.monsai.application.service.TelemetriaService;
import com.senai.monsai.domain.enums.StatusDispositivo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TelemetriaControllerTest {

    @Mock
    private TelemetriaService telemetriaService;

    @InjectMocks
    private TelemetriaController telemetriaController;

    private TelemetriaDTO montarDtoSimples() {
        var aceleracao = new TelemetriaDTO.AceleracaoDTO(
                0.0,
                0.0,
                9.8
        );

        var movimento = new TelemetriaDTO.MovimentoDTO(aceleracao, false);

        var sinalVital = new TelemetriaDTO.SinalVitalDTO(
                "SV-1",
                80,
                36.5,
                movimento
        );

        var local = new TelemetriaDTO.LocalizacaoDTO(
                -23.0,
                -46.0,
                5.0);

        var status = new TelemetriaDTO.StatusDispositivoDTO(
                "ST-1",
                "now",
                100,
                StatusDispositivo.ATIVO
        );

        return new TelemetriaDTO(
                1L,
                "MON-313",
                "now",
                sinalVital,
                local,
                status
        );
    }

    // =========================================================
    // EDGE CASE 1: Receber telemetria com sucesso
    // =========================================================
    @Test
    @DisplayName("AACI-114: Endpoint de telemetria retorna 201 Created")
    void deveReceberTelemetriaComSucesso() {
        TelemetriaDTO dto = montarDtoSimples();
        doNothing().when(telemetriaService).processarTelemetria(any(TelemetriaDTO.class));

        ResponseEntity<String> response = telemetriaController.receberTelemetria(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody(), "A mensagem de resposta não deve ser nula");
        assertEquals("Dados de telemetria processados e analisados com sucesso!", response.getBody());
        verify(telemetriaService, times(1)).processarTelemetria(dto);
    }

    // =========================================================
    // EDGE CASE 2: Buscar a última leitura de telemetria
    // =========================================================
    @Test
    @DisplayName("AACI-114: Retorna mapa com as últimas telemetrias")
    void deveRetornarUltimasTelemetrias() {
        TelemetriaDTO dto = montarDtoSimples();
        when(telemetriaService.getUltimasTelemetrias()).thenReturn(Map.of("MON-313", dto));

        ResponseEntity<?> response = telemetriaController.getUltimaTelemetria();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody(), "O mapa de telemetrias não deve ser nulo");
        // Verifica se a resposta é um mapa e se contém o dispositivo simulado
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertTrue(body.containsKey("MON-313"));
    }

    // =========================================================
    // EDGE CASE 3: Buscar alertas da memória
    // =========================================================
    @Test
    @DisplayName("AACI-114: Retorna lista de alertas da memória do Service")
    void deveRetornarAlertasEmMemoria() {
        // O controller chama uma variável estática ALERTA_CACHE do Service,
        // mas aqui focamos na chamada do Controller.
        ResponseEntity<List<AlertaDTO>> response = telemetriaController.getAlertasEmMemoria();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody(), "A lista de alertas não pode ser nula");
    }

    // =========================================================
    // EDGE CASE 4: Buscar alertas mock
    // =========================================================
    @Test
    @DisplayName("AACI-114: Retorna a lista mockada de testes antigos")
    void deveRetornarAlertasMock() {
        ResponseEntity<List<String>> response = telemetriaController.buscarAlertasMock();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody(), "A lista não pode ser nula");
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().get(0).contains("ANOMALIA CARDÍACA"));
    }
}