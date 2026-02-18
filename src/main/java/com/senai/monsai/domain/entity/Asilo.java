package com.senai.monsai.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Asilo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long asilo_id;
    private String nome;
    private String cnpj;
    private String endereco;
    @OneToMany(mappedBy = "asilo")
    private List<Idoso> idosos;
}
