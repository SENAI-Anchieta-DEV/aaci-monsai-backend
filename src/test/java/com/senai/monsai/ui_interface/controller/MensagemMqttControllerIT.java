package com.senai.monsai.ui_interface.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.senai.monsai.application.dto.TelemetriaDTO;
import com.senai.monsai.domain.entity.Dispositivo;
import com.senai.monsai.domain.entity.Idoso;
import com.senai.monsai.domain.entity.Usuario;
import com.senai.monsai.domain.enums.StatusDispositivo;
import com.senai.monsai.domain.enums.TipoUsuario;
import com.senai.monsai.domain.repository.DispositivoRepository;
import com.senai.monsai.domain.repository.IdosoRepository;
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

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MensagemMqttControllerIT {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private DispositivoRepository dispositivoRepository;
    @Autowired private JwtService jwtService;

    private String tokenGestor;
    private String dispositivoIdExistente;

    @Autowired private IdosoRepository idosoRepository; // Adicione este repositório

    @BeforeEach
    void setup() {
        // 1. Limpeza (Atenção à ordem por causa das FKs)
        idosoRepository.deleteAll();
        dispositivoRepository.deleteAll();
        usuarioRepository.deleteAll();

        // 2. Criar e salvar o dispositivo
        Dispositivo dispositivo = Dispositivo.builder()
                .serial("SN-TEST-123")
                .statusDispositivo(StatusDispositivo.ATIVO)
                .nivelBateria(100)
                .ultimoContato(LocalDateTime.now())
                .build();
        dispositivo = dispositivoRepository.save(dispositivo);
        this.dispositivoIdExistente = dispositivo.getId();

        // 3. Criar e salvar o Idoso VINCULADO ao dispositivo
        // Ajuste os campos conforme sua entidade Idoso
        Idoso idoso = Idoso.builder()
                .nome("João da Silva")
                .cpf("123.456.789-00")
                .ativo(true)
                .dispositivo(dispositivo) // <--- O VÍNCULO QUE FALTAVA
                .build();
        idosoRepository.save(idoso);

        // 4. Setup do usuário para o token (como estava antes)
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
        // Montagem do DTO com o ID que realmente existe no banco
        var aceleracao = new TelemetriaDTO.AceleracaoDTO(1.2, 0.5, -9.8);
        var movimento = new TelemetriaDTO.MovimentoDTO(aceleracao, false);
        var sinais = new TelemetriaDTO.SinalVitalDTO("SV-001", 80, 36.5, movimento);
        var local = new TelemetriaDTO.LocalizacaoDTO(-23.55, -46.63, 5.0);
        var statusDisp = new TelemetriaDTO.StatusDispositivoDTO(
                "ST-001", "2026-03-11T10:00:00", 85, StatusDispositivo.ATIVO
        );

        TelemetriaDTO dto = new TelemetriaDTO(
                1L,
                this.dispositivoIdExistente, // <--- Agora o Service vai encontrar!
                "2026-03-11T10:00:00",
                sinais,
                local,
                statusDisp
        );

        mockMvc.perform(post("/api/mqtt/simular-sensor")
                        .header("Authorization", "Bearer " + tokenGestor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar 400 se faltar campo obrigatório")
    void deveValidarCamposObrigatoriosAninhados() throws Exception {
        // ID aleatório para forçar erro de validação/negócio se necessário
        TelemetriaDTO dtoInvalido = new TelemetriaDTO(1L, "ID-INEXISTENTE", "2026", null, null, null);

        mockMvc.perform(post("/api/mqtt/simular-sensor")
                        .header("Authorization", "Bearer " + tokenGestor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoInvalido)))
                .andExpect(status().isBadRequest());
    }
}