package com.senai.monsai.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Pulseira {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pulseira_id;
    @ManyToOne
    @JoinColumn(name = "idoso_id")
    private Idoso idoso;
    private String status;
}
