package com.senai.monsai.ui_interface.controller;

import com.senai.monsai.application.dto.MensagemMqttDTO;
import com.senai.monsai.application.service.MensagemMqttService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mqtt")
@RequiredArgsConstructor
@Tag(name = "Envio de Mensagem MQTT", description = "Envio de dados (mensagem e tópico) via MQTT")
public class MensagemMqttController {

    private final MensagemMqttService mqttService;

    @Operation(summary = "Enviar Mensagem MQTT", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/enviar")
    public ResponseEntity<String> enviarMensagem(@RequestBody MensagemMqttDTO dto) {
        // Chamaremos um método de envio no Service
        mqttService.publicar(dto.topico(), dto.conteudo());
        return ResponseEntity.ok("Comando enviado com sucesso para o tópico: " + dto.topico());
    }
}
