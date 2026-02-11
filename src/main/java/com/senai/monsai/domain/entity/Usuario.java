package com.senai.monsai.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@SuperBuilder
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long usuario_id;
    private String nome;
    private String cpf;
    private String email;
    private String senha;
    private String tipo_perfil;
}
