package com.senai.monsai.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO utilizado para atualização da senha do usuário")
public record AtualizarSenhaDTO(

        @Schema(description = "Nova senha do usuário", example = "NovaSenha123", required = true)
        String novaSenha

) {}