package com.senai.monsai.application.dto;

public record LoginResponseDTO(
        String token,
        String tipoPerfil,
        Long usuarioId,
        String nome,
        String cpf,
        Long asiloId
) {}