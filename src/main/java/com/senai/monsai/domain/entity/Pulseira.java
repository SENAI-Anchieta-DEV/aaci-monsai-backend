package com.senai.monsai.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    @OneToOne(mappedBy = "pulseira")
    private Idoso idoso;

    @OneToMany(mappedBy = "pulseira", cascade = CascadeType.ALL)
    private List<Sensor> sensores;

}
