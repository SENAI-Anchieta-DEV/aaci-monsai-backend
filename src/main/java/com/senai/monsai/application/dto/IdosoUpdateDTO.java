package com.senai.monsai.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record IdosoUpdateDTO(
        @NotBlank(message = "O nome é obrigatório.")
        String nome,

        @NotBlank(message = "O CPF é obrigatório.")
        String cpf,

        @Email(message = "Formato de e-mail inválido.")
        String email
) {}