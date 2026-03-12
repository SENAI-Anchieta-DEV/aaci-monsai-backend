package com.senai.monsai.application.dto;

import com.senai.monsai.domain.enums.TipoUsuario;

public record UsuarioDTO(
        Long usuarioId,
        String nome,
        String email,
        String cpf,
        TipoUsuario tipo,
        Long asiloId,
        boolean ativo
) {}