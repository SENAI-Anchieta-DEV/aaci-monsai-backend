package com.senai.monsai.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta retornada após autenticação bem-sucedida")
public record LoginResponseDTO(

        @Schema(description = "Token JWT utilizado para autenticação", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token,

        @Schema(description = "Tipo de perfil do usuário autenticado", example = "ADMIN")
        String tipoPerfil

) {}