package com.senai.monsai.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

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
    @ManyToOne
    @JoinColumn(name = "asilo_id")
    private Asilo asilo;
    @OneToOne
    @JoinColumn(name = "dispositivo_id")
    private Dispositivo dispositivo;
}