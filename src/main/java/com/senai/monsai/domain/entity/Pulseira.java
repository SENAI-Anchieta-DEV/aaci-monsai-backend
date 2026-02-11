package com.senai.monsai.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;


@Data
@Entity
public class Pulseira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serial;

    @OneToOne
    private Idoso idoso;

    @OneToMany(mappedBy = "pulseira", cascade = CascadeType.ALL)
    private List<Sensor> sensores;

}
