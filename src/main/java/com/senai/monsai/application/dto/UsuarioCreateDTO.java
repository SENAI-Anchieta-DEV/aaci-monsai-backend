package com.senai.monsai.application.dto;

import com.senai.monsai.domain.enums.TipoUsuario;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO utilizado para criação de usuários no sistema")
public record UsuarioCreateDTO(

        @Schema(description = "Nome completo do usuário", example = "João da Silva", required = true)
        String nome,

        @Schema(description = "Email do usuário", example = "joao@email.com", required = true)
        String email,

        @Schema(description = "Senha do usuário", example = "123456", required = true)
        String senha,

        @Schema(description = "CPF do usuário", example = "123.456.789-00", required = true)
        String cpf,

        @Schema(description = "Tipo de usuário do sistema", example = "ADMIN")
        TipoUsuario tipoUsuario,

        @Schema(description = "ID do asilo associado ao usuário", example = "1")
        Long asiloId

) {}