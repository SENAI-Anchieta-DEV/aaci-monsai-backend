package com.senai.monsai.domain.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Medicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double valor;
    private LocalDateTime dataHora;

    @ManyToOne
    private Sensor sensor;

}

