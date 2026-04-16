package com.senai.monsai.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaixaReferencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relacionamento 1:1 - Cada idoso tem sua própria regra
    @OneToOne
    @JoinColumn(name = "idoso_id")
    private Idoso idoso;

    private Integer minBpm;
    private Integer maxBpm;
    private Double minTemp;
    private Double maxTemp;
}