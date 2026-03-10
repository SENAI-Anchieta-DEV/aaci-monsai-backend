package com.senai.monsai.domain.entity;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id; // O pulseira_id do JSON

    private String serial; // Identificação física/técnica

    @OneToOne
    @JoinColumn(name = "idoso_id")
    private Idoso idoso; // O "dono" do sinal vital

    // Status em tempo real (extraído do status_do_dispositivo do JSON)
    private Integer nivelBateria;

    @Enumerated(EnumType.STRING)
    private StatusDispositivo statusDispositivo;

    private LocalDateTime ultimoContato;

    @OneToMany(mappedBy = "dispositivo", cascade = CascadeType.ALL)
    private List<Sensor> sensores;

}
