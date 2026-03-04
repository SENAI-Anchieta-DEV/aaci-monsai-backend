package com.senai.monsai.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Idoso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idoso_id;

    private String nome;
    private String cpf;
    private String email;
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne
    @JoinColumn(name = "asilo_id")
    private Asilo asilo;
    @OneToOne
    @JoinColumn(name = "pulseira_id")
    private Pulseira pulseira;
    @ManyToMany(mappedBy = "idosos")
    @JsonIgnore
    private List<Usuario> usuarios = new ArrayList<>();
    @Builder.Default
    private boolean ativo = true;
}