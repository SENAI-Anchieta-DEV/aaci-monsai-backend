package com.senai.monsai.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Asilo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long asilo_id;
    private String nome;
    private String cnpj;
    private String endereco;
}
