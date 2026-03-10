package com.senai.monsai.application.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record MensagemMqttDTO(
        // AACI 127 e 128 - Definindo campos, tipos e limites do payload
        @NotNull(message = "ID do dispositivo é obrigatório") String dispositivoId,
        @NotNull(message = "tipo é obrigatório") String tipo,
        @NotNull(message = "valor é obrigatório") Double valor,
        String timestamp // ISO-8601
) {
}
