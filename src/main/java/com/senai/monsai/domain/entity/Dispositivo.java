package com.senai.monsai.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.senai.monsai.domain.enums.StatusDispositivo;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Dispositivo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "dispositivo_id")
    private String id;

    @Column(nullable = false, unique = true)
    private String serial; // Identificação física/técnica

    @OneToOne(mappedBy = "dispositivo")
    @JsonIgnore
    private Idoso idoso;

    // Status em tempo real (extraído do status_do_dispositivo do JSON)
    private Integer nivelBateria;

    @Enumerated(EnumType.STRING)
    private StatusDispositivo statusDispositivo;

    private LocalDateTime ultimoContato;

    @OneToMany(mappedBy = "dispositivo", cascade = CascadeType.ALL)
    private List<Sensor> sensores;

}
