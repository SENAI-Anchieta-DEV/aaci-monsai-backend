package com.senai.monsai.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO utilizado para cadastro de um idoso")
public record IdosoCreateDTO(

        @Schema(description = "Nome completo do idoso", example = "José da Silva", required = true)
        String nome,

        @Schema(description = "CPF do idoso", example = "123.456.789-00", required = true)
        String cpf,

        @Schema(description = "Email do idoso ou responsável", example = "jose@email.com")
        String email,

        @Schema(description = "Serial da pulseira de monitoramento", example = "PULSEIRA-12345")
        String serialPulseira

) {}