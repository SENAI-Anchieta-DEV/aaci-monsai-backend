package com.senai.monsai.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record IdosoUpdateDTO(
        @Pattern(regexp = "^(?!\\s*$).+", message = "O nome não pode estar em branco se for enviado.")
        String nome,

        @Pattern(regexp = "^(?!\\s*$).+", message = "O CPF não pode estar em branco se for enviado.")
        String cpf,

        @Email(message = "Formato de e-mail inválido.")
        String email,

        Long asiloId,
        String dispositivoId
) {}