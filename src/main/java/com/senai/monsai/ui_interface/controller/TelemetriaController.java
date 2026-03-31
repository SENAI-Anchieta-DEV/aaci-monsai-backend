package com.senai.monsai.ui_interface.controller;

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
        // Se a pulseira for do idoso errado, o Service lançará uma SecurityException
        // O seu GlobalExceptionHandler deve capturar isso e devolver um erro 403 (Forbidden) ou 400 (Bad Request)
        telemetriaService.processarTelemetria(dto);

        // Atualiza a variável em memória para o React (apenas para testes locais)
        atualizarDados(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body("Dados de telemetria processados e analisados com sucesso!");
    }


    // ==========================================
    // 2. BUSCAR ALERTAS (Para o Frontend/React)
    // ==========================================
    @GetMapping("/alertas/{idosoId}")
    @Operation(summary = "Buscar alertas de um Idoso", description = "Retorna os alertas ativos gerados por anomalias nos sinais vitais")
    public ResponseEntity<List<String>> buscarAlertasIdoso(@PathVariable Long idosoId) {
        // No mundo real, você chamaria: telemetriaService.buscarAlertasPorIdoso(idosoId);
        // Retornando uma simulação caso o Frontend já queira testar a tela:
        List<String> alertasMock = List.of(
                "⚠️ ANOMALIA CARDÍACA: BPM registrado em 125",
                "🔋 BATERIA FRACA: Dispositivo com apenas 10%"
        );
        return ResponseEntity.ok(alertasMock);
    }


    // ==========================================
    // 3. RETORNAR ÚLTIMA LEITURA (Para o Frontend/React)
    // ==========================================
    private static TelemetriaDTO ultimaTelemetria = null;

    @GetMapping("/ultima")
    @Operation(summary = "Obter última telemetria", description = "Endpoint de polling temporário para o React atualizar a tela")
    public ResponseEntity<TelemetriaDTO> getUltimaTelemetria() {
        if (ultimaTelemetria == null) {
            return ResponseEntity.noContent().build(); // 204 se não tiver dados ainda
        }
        return ResponseEntity.ok(ultimaTelemetria);
    }

    public static void atualizarDados(TelemetriaDTO novoDto) {
        ultimaTelemetria = novoDto;
    }


    // ==========================================
    // 4. ENVIAR COMANDO PARA A PULSEIRA (MQTT)
    // ==========================================
    @PostMapping("/comando-led")
    @Operation(summary = "Acionar LED", description = "Envia um comando via MQTT para ligar o LED da pulseira (ex: localizar paciente)")
    public ResponseEntity<String> enviarComando() {
        System.out.println("🚀 [JAVA]: Disparando comando LIGAR_LED via Gateway...");
        mqttGateway.sendToMqtt("LIGAR_LED", "monsai/comandos");
        return ResponseEntity.ok("Comando de ativação enviado para o Broker MQTT!");
    }
}