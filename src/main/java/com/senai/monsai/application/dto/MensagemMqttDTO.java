package com.senai.monsai.application.dto;

import java.time.LocalDateTime;

public record MensagemMqttDTO(
        String dispositivoId,
        String tipo,
        Double valor,
        String timestamp // Receba como String para evitar erro de formatação
) {
}
