package com.senai.monsai.ui_interface.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.senai.monsai.application.dto.TelemetriaDTO;
import com.senai.monsai.domain.entity.Usuario;
import com.senai.monsai.domain.enums.StatusDispositivo;
import com.senai.monsai.domain.enums.TipoUsuario;
import com.senai.monsai.domain.repository.UsuarioRepository;
import com.senai.monsai.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MensagemMqttControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private JwtService jwtService;

    private String tokenGestor;

    @BeforeEach
    void setup() {
        usuarioRepository.deleteAll();
        Usuario gestor = Usuario.builder()
                .nome("Admin")
                .email("admin@teste.com")
                .tipo(TipoUsuario.GESTOR)
                .ativo(true)
                .build();
        usuarioRepository.save(gestor);
        tokenGestor = jwtService.generateToken(gestor.getEmail(), "ROLE_GESTOR");
    }

    @Test
    @DisplayName("Deve processar contrato de telemetria complexo com sucesso")
    void deveProcessarTelemetriaComplexa() throws Exception {
        // 1. Montagem do DTO aninhado (seguindo sua estrutura de records)
        var aceleracao = new TelemetriaDTO.AceleracaoDTO(1.2, 0.5, -9.8);
        var movimento = new TelemetriaDTO.MovimentoDTO(aceleracao, false);
        var sinais = new TelemetriaDTO.SinalVitalDTO("SV-001", 80, 36.5, movimento);
        var local = new TelemetriaDTO.LocalizacaoDTO(-23.55, -46.63, 5.0);
        var statusDisp = new TelemetriaDTO.StatusDispositivoDTO(
                "ST-001", "2026-03-11T10:00:00", 85, StatusDispositivo.ATIVO
        );

        TelemetriaDTO dto = new TelemetriaDTO(
                1L,
                UUID.randomUUID().toString(),
                "2026-03-11T10:00:00",
                sinais,
                local,
                statusDisp
        );

        // 2. Execução do MockMvc
        mockMvc.perform(post("/api/mqtt/simular-sensor")
                        .header("Authorization", "Bearer " + tokenGestor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar 400 se faltar campo obrigatório no contrato aninhado")
    void deveValidarCamposObrigatoriosAninhados() throws Exception {
        // Simulando erro: sinal_vital como null (marcado com @NotNull no DTO mestre)
        TelemetriaDTO dtoInvalido = new TelemetriaDTO(1L, "UUID", "2026", null, null, null);

        mockMvc.perform(post("/api/mqtt/simular-sensor")
                        .header("Authorization", "Bearer " + tokenGestor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoInvalido)))
                .andExpect(status().isBadRequest());
    }
}