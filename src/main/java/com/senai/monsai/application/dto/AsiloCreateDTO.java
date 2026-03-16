package com.senai.monsai.application.dto;

import jakarta.validation.constraints.NotBlank;

public record AsiloCreateDTO(
        @NotBlank(message = "O nome do asilo é obrigatório.")
        String nome,

        @NotBlank(message = "O CNPJ é obrigatório.")
        String cnpj,

        @NotBlank(message = "O CNPJ é obrigatório.")
        String endereco
) {}