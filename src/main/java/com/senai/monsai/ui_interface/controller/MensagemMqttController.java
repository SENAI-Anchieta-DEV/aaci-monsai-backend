package com.senai.monsai.ui_interface.controller;

import com.senai.monsai.application.dto.TelemetriaDTO;
import com.senai.monsai.application.service.TelemetriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid; // <-- Faltava importar e usar o @Valid
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mqtt")
@RequiredArgsConstructor
@Tag(name = "Simulador MQTT", description = "Endpoints para simular o envio de dados de dispositivos IoT")
public class MensagemMqttController {

    private final TelemetriaService telemetriaService;

    @PostMapping("/simular-sensor")
    @Operation(summary = "Simular recebimento de telemetria", description = "Simula uma pulseira enviando dados via MQTT")
    public ResponseEntity<String> dispararSimulacao(@Valid @RequestBody TelemetriaDTO dto) { // <-- @Valid adicionado
        telemetriaService.processarTelemetria(dto);
        return ResponseEntity.ok("Simulação processada com sucesso. Verifique os alertas e o banco de dados.");
    }
}