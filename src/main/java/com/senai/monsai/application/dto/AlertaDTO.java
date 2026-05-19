package com.senai.monsai.application.dto;

import java.util.List;

public record AlertaDTO(
        String id,
        Long idosoId,
        String idosoNome,
        List<String> motivos,
        String data,
        boolean visto
) {}
