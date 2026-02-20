package com.senai.monsai.application.dto;

import com.senai.monsai.domain.enums.TipoUsuario;

public record UsuarioCreateDTO(
        String nome,
        String email,
        String senha,
        TipoUsuario tipo,
        Long asiloId
) {}
