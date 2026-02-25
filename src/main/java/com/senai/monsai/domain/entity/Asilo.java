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
    private Long asilo_id;
    private String nome;
    private String cnpj;
    private String endereco;
    @JsonIgnore
    @OneToMany(mappedBy = "asilo")
    private List<Idoso> idosos;
    private boolean ativo;
}
