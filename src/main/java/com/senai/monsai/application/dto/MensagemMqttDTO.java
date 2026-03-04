package com.senai.monsai.application.dto;

import java.time.LocalDateTime;

public record MensagemMqttDTO(
        String topico,
        String conteudo,
        LocalDateTime dataRecebimento
) {
}
