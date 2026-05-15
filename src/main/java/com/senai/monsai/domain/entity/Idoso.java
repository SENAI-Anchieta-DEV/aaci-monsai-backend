package com.senai.monsai.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.ArrayList;

@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Idoso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idoso_id")
    private Long id;

    private String nome;
    @Column(nullable = false, unique = true)
    private String cpf;
    private String email;

    @ManyToOne(optional = false)
    @JoinColumn(name = "asilo_id", nullable = false)
    @JsonIgnore
    private Asilo asilo;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "dispositivo_id")
    private Dispositivo dispositivo;

    @Builder.Default
    private boolean ativo = true;

    @JsonIgnore
    @ManyToMany(mappedBy = "idosos")
    private List<Usuario> usuarios = new ArrayList<>();
}