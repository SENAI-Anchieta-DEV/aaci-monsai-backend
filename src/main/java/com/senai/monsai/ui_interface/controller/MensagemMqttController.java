package com.senai.monsai.ui_interface.controller;

import com.senai.monsai.application.dto.MensagemMqttDTO;
import com.senai.monsai.application.service.MensagemMqttService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mqtt")
@RequiredArgsConstructor
public class MensagemMqttController {

    private final MensagemMqttService mqttService;

    @PostMapping("/enviar")
    public ResponseEntity<String> enviarMensagem(@RequestBody MensagemMqttDTO dto) {
        // Chamaremos um método de envio no Service
        mqttService.publicar(dto.topico(), dto.conteudo());
        return ResponseEntity.ok("Comando enviado com sucesso para o tópico: " + dto.topico());
    }
}
