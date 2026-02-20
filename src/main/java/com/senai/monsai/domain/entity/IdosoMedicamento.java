package com.senai.monsai.domain.entity;

import jakarta.persistence.*;

@Entity
public class IdosoMedicamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Idoso idoso;

    @ManyToOne
    private Medicamento medicamento;

    private String dosagem;
    private String horario;
}
