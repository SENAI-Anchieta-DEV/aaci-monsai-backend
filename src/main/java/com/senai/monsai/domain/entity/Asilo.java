package com.senai.monsai.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data

public class Asilo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "asilo_id")
    private Long id;

    private String nome;

    @Column(unique = true, nullable = false)
    private String cnpj;

    private String endereco;

    @JsonIgnore
    @OneToMany(mappedBy = "asilo")
    private List<Idoso> idosos;

    private boolean ativo = true;
}