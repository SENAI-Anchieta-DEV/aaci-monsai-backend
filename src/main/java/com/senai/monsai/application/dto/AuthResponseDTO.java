package com.senai.monsai.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta retornada após autenticação bem sucedida")
public record AuthResponseDTO(

        @Schema(description = "Token JWT utilizado para autenticação nas requisições", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token,

        @Schema(description = "Tipo de perfil do usuário autenticado", example = "ADMIN")
        String tipoPerfil

) {}