package com.senai.monsai.ui_interface.controller;

import com.senai.monsai.application.dto.TelemetriaDTO;
import com.senai.monsai.application.service.TelemetriaService;
import com.senai.monsai.infrastructure.config.MqttGateway;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/telemetria")
@CrossOrigin(origins = "*")
public class TelemetriaController {
    @Autowired
    private TelemetriaService telemetriaService;

    @Autowired
    private MqttGateway mqttGateway;

    @PostMapping
    @Operation(summary = "Registra telemetria",
            description = "Envia dados de telemetria para processamento em tempo real")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Telemetria Padrão",
                            value = """
            {
              "idoso_id": 1,
              "pulseira_id": "b99d5543-7f2a-4632-8401-2292f392237d",
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

        return ResponseEntity.ok("Dados de telemetria recebidos com sucesso!");
    }

    // Variável temporária para guardar o último objeto DTO que chegou
    private static TelemetriaDTO ultimaTelemetria = null;

    // AACI-182: Endpoint que o React vai chamar para atualizar a tela
    @GetMapping("/ultima")
    public TelemetriaDTO getUltimaTelemetria() {
        return ultimaTelemetria;
    }

    // Este método deve ser chamado pela sua classe que escuta o MQTT
    public static void atualizarDados(TelemetriaDTO novoDto) {
        ultimaTelemetria = novoDto;
    }

    @PostMapping("/comando-led")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> enviarComando() {
        System.out.println("🚀 [JAVA]: Disparando comando LIGAR_LED via Gateway...");

        // Esta linha é a que faz a mágica e manda para o Broker!
        mqttGateway.sendToMqtt("LIGAR_LED", "monsai/comandos");

        return ResponseEntity.ok("Comando enviado!");
    }
}
