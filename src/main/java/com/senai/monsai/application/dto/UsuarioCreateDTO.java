package com.senai.monsai.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
// Importe o seu Enum TipoUsuario aqui se necessário

public record UsuarioCreateDTO(
        @NotBlank(message = "O nome é obrigatório.")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Formato de e-mail inválido.")
        String email,

        @NotBlank(message = "A senha é obrigatória.")
        String senha,

        @NotBlank(message = "O CPF é obrigatório.")
        String cpf,

        @NotNull(message = "O tipo de usuário é obrigatório.")
        String tipoUsuario, // Se for um Enum no seu projeto, mude o tipo aqui

        @NotNull(message = "O ID do asilo é obrigatório.")
        Long asiloId
) {}