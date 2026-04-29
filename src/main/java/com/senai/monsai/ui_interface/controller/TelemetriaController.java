package com.senai.monsai.ui_interface.controller;

import com.senai.monsai.application.dto.AlertaDTO;
import com.senai.monsai.application.dto.TelemetriaDTO;
import com.senai.monsai.application.service.TelemetriaService;
import com.senai.monsai.infrastructure.config.MqttGateway;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/telemetria")
@CrossOrigin(origins = "*") // Permite que o React faça requisições sem erro de CORS
@Tag(name = "Telemetria e IoT", description = "Recepção de dados das pulseiras e gestão de alertas")
public class TelemetriaController {

    @Autowired
    private TelemetriaService telemetriaService;

    @Autowired
    private MqttGateway mqttGateway;

    // ==========================================
    // 1. RECEBER DADOS DO DISPOSITIVO (MQTT/HTTP)
    // ==========================================
    @PostMapping
    @Operation(summary = "Registra telemetria", description = "Envia dados de telemetria para processamento em tempo real")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Telemetria Padrão",
                            value = """
                            {
                              "idoso_id": 1,
                              "pulseira_id": "MON-313",
                              "data_hora": "2026-03-10T11:00:00",
                              "sinal_vital": {
                                "sinal_vital_id": "SV-001",
                                "frequencia_cardiaca_bpm": 75,
                                "temperatura_c": 36.5,
                                "movimento": {
                                  "aceleracao": {"x": 0.1, "y": 0.0, "z": 9.8},
                                  "queda_detectada": false
                                }
                              },
                              "localizacao": {"latitude": -23.55, "longitude": -46.63, "precisao_metro": 5.0},
                              "status_do_dispositivo": {
                                "status_id": "ST-001",
                                "ultimo_contato": "2026-03-10T10:59:00",
                                "nivel_bateria": 85,
                                "status_pulseira": "ATIVO"
                              }
                            }
                            """
                    )
            )
    )
    public ResponseEntity<String> receberTelemetria(@Valid @RequestBody TelemetriaDTO dto) {
        telemetriaService.processarTelemetria(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body("Dados de telemetria processados e analisados com sucesso!");
    }


    // ==========================================
    // 2. BUSCAR ALERTAS (MOCK PARA TESTES ANTIGOS)
    // ==========================================
    @GetMapping("/alertas-mock")
    public ResponseEntity<List<String>> buscarAlertasMock() {
        List<String> alertasMock = List.of(
                "⚠️ ANOMALIA CARDÍACA: BPM registrado em 125",
                "🔋 BATERIA FRACA: Dispositivo com apenas 10%"
        );
        return ResponseEntity.ok(alertasMock);
    }


    // ==========================================
    // 3. RETORNAR ÚLTIMA LEITURA (Para o Monitoramento.jsx)
    // ==========================================
    @GetMapping("/ultima")
    public ResponseEntity<?> getUltimaTelemetria() {
        // O Controller pede os dados guardados lá no Service
        return ResponseEntity.ok(telemetriaService.getUltimasTelemetrias());
    }


    // ==========================================
    // 4. RETORNAR OS ALERTAS ATIVOS (Para o HistoricoAlertas.jsx)
    // ==========================================
    @GetMapping("/alertas-recentes")
    @Operation(summary = "Alertas em Tempo Real", description = "Retorna os alertas ativos no cache")
    public ResponseEntity<List<AlertaDTO>> getAlertasEmMemoria() {
        // Retorna a lista que o Service preencheu
        return ResponseEntity.ok(TelemetriaService.ALERTA_CACHE);
    }

    // ==========================================
    // 5. ENVIAR COMANDO PARA A PULSEIRA (LED/Buzzer)
    // ==========================================
    @PostMapping("/comando-led/{serial}")
    @Operation(summary = "Acionar LED da pulseira", description = "Envia um comando via MQTT para testar o LED do dispositivo")
    public ResponseEntity<Void> testarPulseira(@PathVariable String serial) {
        // Envia para o tópico que o ESP32 deve estar ouvindo
        // O payload pode ser apenas "L" (de LED)
        mqttGateway.sendToMqtt("L", "monsai/comandos/" + serial);

        return ResponseEntity.ok().build();
    }
}