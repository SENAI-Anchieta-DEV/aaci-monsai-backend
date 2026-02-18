package com.senai.monsai.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@SuperBuilder
public class Idoso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idoso_id;
    private String nome;
    private String cpf;
    private String email;
    @JoinColumn(name = "asilo_id")
    private Asilo asilo;

}
