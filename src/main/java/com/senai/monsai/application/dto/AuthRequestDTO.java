package com.senai.monsai.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO utilizado para autenticação do usuário")
public record AuthRequestDTO(

        @Schema(description = "Email do usuário", example = "usuario@email.com", required = true)
        String email,

        @Schema(description = "Senha do usuário", example = "123456", required = true)
        String senha

) {}