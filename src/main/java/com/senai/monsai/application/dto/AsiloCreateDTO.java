package com.senai.monsai.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO utilizado para criação de um asilo")
public record AsiloCreateDTO(

        @Schema(description = "Nome do asilo", example = "Lar São Vicente")
        String nome,

        @Schema(description = "CNPJ do asilo", example = "12.345.678/0001-99")
        String cnpj,

        @Schema(description = "Endereço completo do asilo", example = "Rua das Flores, 120 - São Paulo")
        String endereco

) {}