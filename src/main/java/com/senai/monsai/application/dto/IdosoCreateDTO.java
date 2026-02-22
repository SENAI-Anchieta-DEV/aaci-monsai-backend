package com.senai.monsai.application.dto;

import lombok.Data;

@Data
public class IdosoCreateDTO {
    private String nome;
    private String cpf;
    private String email;
    private String serialPulseira; // O ID físico da pulseira IoT
}
