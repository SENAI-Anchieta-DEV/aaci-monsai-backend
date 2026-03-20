package com.senai.monsai.application.dto;
import com.senai.monsai.domain.enums.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UsuarioCreateDTO(
        @NotBlank(message = "O nome não pode estar em branco.")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "O formato do e-mail é inválido.")
        String email,

        @NotBlank(message = "A senha é obrigatória.")
        String senha,

        @NotBlank(message = "O CPF é obrigatório.")
        String cpf,

        @NotNull(message = "O tipo de usuário é obrigatório.")
        TipoUsuario tipoUsuario,

        @NotNull(message = "O ID do asilo é obrigatório.")
        Long asiloId
) {}