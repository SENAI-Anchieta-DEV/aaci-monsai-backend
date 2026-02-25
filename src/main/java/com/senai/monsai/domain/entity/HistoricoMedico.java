package com.senai.monsai.domain.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class HistoricoMedico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String descricao;
    private LocalDateTime dataRegistro;
    @ManyToOne
    private Idoso idoso;

}

