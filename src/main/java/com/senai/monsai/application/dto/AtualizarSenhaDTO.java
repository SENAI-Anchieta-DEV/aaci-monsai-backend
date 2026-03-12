package com.senai.monsai.application.dto;

import jakarta.validation.constraints.NotBlank;

public record AtualizarSenhaDTO(
        @NotBlank(message = "A nova senha não pode ser vazia.")
        String novaSenha
) {}