package com.senai.monsai.ui_interface.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.senai.monsai.application.dto.TelemetriaDTO;
import com.senai.monsai.domain.entity.Asilo;
import com.senai.monsai.domain.entity.Dispositivo;
import com.senai.monsai.domain.entity.Idoso;
import com.senai.monsai.domain.entity.Usuario;
import com.senai.monsai.domain.enums.StatusDispositivo;
import com.senai.monsai.domain.enums.TipoUsuario;
import com.senai.monsai.domain.repository.AsiloRepository;
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
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private DispositivoRepository dispositivoRepository;
    @Autowired private IdosoRepository idosoRepository;
    @Autowired private AsiloRepository asiloRepository;
    @Autowired private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private String tokenGestor;
    private String dispositivoIdExistente;
    private Long idosoIdExistente;

    @BeforeEach
    void setup() {
        // 1. Limpeza rigorosa para evitar conflitos entre testes
        idosoRepository.deleteAll();
        usuarioRepository.deleteAll();
        asiloRepository.deleteAll();
        dispositivoRepository.deleteAll();

        // 2. Criar e SALVAR o Asilo primeiro
        Asilo asilo = new Asilo();
        asilo.setNome("Asilo de Teste MQTT");
        asilo.setCnpj("12.345.678/0001-00");
        asilo.setAtivo(true);
        asilo = asiloRepository.saveAndFlush(asilo); // Salva para gerar o ID

        // 3. Instanciar o dispositivo (Sem salvar ainda, o Idoso fará isso via Cascade)
        Dispositivo dispositivo = Dispositivo.builder()
                .serial("SN-TEST-123")
                .statusDispositivo(StatusDispositivo.ATIVO)
                .nivelBateria(100)
                .ultimoContato(LocalDateTime.now())
                .build();

        // 3. Instanciar o Idoso e vincular ao dispositivo
        Idoso idoso = Idoso.builder()
                .nome("João da Silva")
                .cpf("123.456.789-00")
                .email("joao@email.com")
                .ativo(true)
                .asilo(asilo)
                .dispositivo(dispositivo)
                .build();

        // 4. Salvar o Idoso (Isso salva o dispositivo automaticamente por causa do CascadeType.ALL)
        idoso = idosoRepository.save(idoso);

        // 5. Capturar os IDs reais gerados pelo banco de dados
        this.idosoIdExistente = idoso.getId();
        this.dispositivoIdExistente = idoso.getDispositivo().getId();

        // 6. Criar usuário para autenticação
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
        // Preparação do DTO de Telemetria usando os IDs dinâmicos do Setup
        var aceleracao = new TelemetriaDTO.AceleracaoDTO(1.2, 0.5, -9.8);
        var movimento = new TelemetriaDTO.MovimentoDTO(aceleracao, false);
        var sinais = new TelemetriaDTO.SinalVitalDTO("SV-001", 80, 36.5, movimento);
        var local = new TelemetriaDTO.LocalizacaoDTO(-23.55, -46.63, 5.0);
        var statusDisp = new TelemetriaDTO.StatusDispositivoDTO(
                "ST-001", "2026-03-11T10:00:00", 85, StatusDispositivo.ATIVO
        );

        TelemetriaDTO dto = new TelemetriaDTO(
                this.idosoIdExistente, // ID do idoso correto no banco (Long)
                "SN-TEST-123",        // CORREÇÃO: Passando o SERIAL string esperado pelo Service, e não o UUID da PK
                "2026-03-11T10:00:00",
                sinais,
                local,
                statusDisp
        );

        // Execução e Verificação
        mockMvc.perform(post("/api/mqtt/simular-sensor")
                        .header("Authorization", "Bearer " + tokenGestor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk()); // Agora o status esperado 200 vai bater!
    }

    @Test
    @DisplayName("Deve retornar 400 se faltar campo obrigatório ou ID inexistente")
    void deveValidarCamposObrigatoriosAninhados() throws Exception {
        // Enviando um payload com ID inexistente para testar a falha
        TelemetriaDTO dtoInvalido = new TelemetriaDTO(
                999L,
                "ID-INEXISTENTE",
                "2026-03-11T10:00:00",
                null, null, null
        );

        mockMvc.perform(post("/api/mqtt/simular-sensor")
                        .header("Authorization", "Bearer " + tokenGestor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoInvalido)))
                .andExpect(status().isBadRequest());
    }
}