package com.senai.monsai.ui_interface.controller;

import com.senai.monsai.application.dto.MensagemMqttDTO;
import com.senai.monsai.application.dto.TelemetriaDTO;
import com.senai.monsai.application.service.MensagemMqttService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mqtt")
@RequiredArgsConstructor
@Tag(name = "Simulador MQTT", description = "Simulador de dispositivos IoT")
public class MensagemMqttController {
/*
    private final MensagemMqttService mqttService;

    @PostMapping("/simular-sensor")
    @Operation(summary = "Simular recebimento de telemetria")
    public ResponseEntity<String> dispararSimulacao(@RequestBody TelemetriaDTO dto) {
        mqttService.publicarTelemetria(dto);
        return ResponseEntity.ok("Simulação disparada com sucesso.");
    }
*/}
