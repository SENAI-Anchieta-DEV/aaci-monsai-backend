package com.senai.monsai.ui_interface.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.senai.monsai.application.dto.TelemetriaDTO;
import com.senai.monsai.application.service.TelemetriaService;
import com.senai.monsai.domain.entity.Usuario;
import com.senai.monsai.domain.enums.StatusDispositivo;
import com.senai.monsai.domain.enums.TipoUsuario;
import com.senai.monsai.domain.repository.UsuarioRepository;
import com.senai.monsai.infrastructure.config.MqttGateway;
import com.senai.monsai.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TelemetriaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @MockitoBean
    private TelemetriaService telemetriaService;

    @MockitoBean
    private MqttGateway mqttGateway;

    @Autowired
    private JwtService jwtService;

    private String tokenGestor;

    @BeforeEach
    void setup() {
        usuarioRepository.deleteAll();

        Usuario gestor = Usuario.builder()
                .nome("Admin Teste")
                .email("admin@teste.com")
                .senha("123456")
                .tipo(TipoUsuario.GESTOR)
                .ativo(true)
                .build();
        usuarioRepository.save(gestor);

        tokenGestor = jwtService.generateToken("admin@teste.com", "ROLE_GESTOR");

        // LINHA REMOVIDA: TelemetriaController.atualizarDados(null);
    }

    @Test
    @DisplayName("POST /api/telemetria - Deve retornar 201 ao receber dados válidos")
    void deveReceberTelemetriaComSucesso() throws Exception {
        TelemetriaDTO dto = criarDtoMock();

        doNothing().when(telemetriaService).processarTelemetria(any(TelemetriaDTO.class));

        mockMvc.perform(post("/api/telemetria")
                        .header("Authorization", "Bearer " + tokenGestor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Dados de telemetria processados e analisados com sucesso!"));

        verify(telemetriaService).processarTelemetria(any(TelemetriaDTO.class));
    }

    @Test
    @DisplayName("GET /api/telemetria/alertas-mock - Deve retornar lista de alertas mockados")
    void deveRetornarAlertasDoIdoso() throws Exception {
        // ROTA CORRIGIDA para /api/telemetria/alertas-mock
        mockMvc.perform(get("/api/telemetria/alertas-mock")
                        .header("Authorization", "Bearer " + tokenGestor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("⚠️ ANOMALIA CARDÍACA: BPM registrado em 125"))
                .andExpect(jsonPath("$[1]").value("🔋 BATERIA FRACA: Dispositivo com apenas 10%"));
    }

    @Test
    @DisplayName("GET /api/telemetria/ultima - Deve retornar 204 quando não houver dados")
    void deveRetornarNoContentQuandoSemDados() throws Exception {
        // Moca o comportamento do service retornando um map vazio ou nula para forçar o 204
        when(telemetriaService.getUltimasTelemetrias()).thenReturn(Collections.emptyMap());

        mockMvc.perform(get("/api/telemetria/ultima")
                        .header("Authorization", "Bearer " + tokenGestor))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/telemetria/comando-led - Deve chamar o Gateway MQTT e retornar 200")
    void deveEnviarComandoLedComSucesso() throws Exception {
        mockMvc.perform(post("/api/telemetria/comando-led")
                        .header("Authorization", "Bearer " + tokenGestor))
                .andExpect(status().isOk())
                .andExpect(content().string("Comando de ativação enviado para o Broker MQTT!"));

        verify(mqttGateway).sendToMqtt("LIGAR_LED", "monsai/comandos");
    }

    private TelemetriaDTO criarDtoMock() {
        var aceleracao = new TelemetriaDTO.AceleracaoDTO(0.1, 0.0, 9.8);
        var movimento = new TelemetriaDTO.MovimentoDTO(aceleracao, false);
        var sinais = new TelemetriaDTO.SinalVitalDTO("SV-001", 75, 36.5, movimento);
        var local = new TelemetriaDTO.LocalizacaoDTO(-23.55, -46.63, 5.0);
        var statusDisp = new TelemetriaDTO.StatusDispositivoDTO(
                "ST-001", "2026-03-10T10:59:00", 85, StatusDispositivo.ATIVO
        );

        return new TelemetriaDTO(
                1L,
                UUID.randomUUID().toString(),
                "2026-03-11T10:00:00",
                sinais,
                local,
                statusDisp
        );
    }
}