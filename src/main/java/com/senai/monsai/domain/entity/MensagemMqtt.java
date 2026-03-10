package com.senai.monsai.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "mensagens_telemetria")
public class MensagemMqtt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relacionamento: Muitas mensagens pertencem a UM dispositivo
    @ManyToOne
    @JoinColumn(name = "dispositivo_id", nullable = false)
    private Dispositivo dispositivo;

    // Sinais Vitais (O que o médico quer ver)
    private Integer frequenciaCardiaca;
    private Double temperatura;
    private Boolean quedaDetectada;

    // Localização
    private Double latitude;
    private Double longitude;

    // Timestamps
    private LocalDateTime dataHoraEvento; // Quando a pulseira gerou o dado
    private LocalDateTime dataRecebimento; // Quando o seu Java salvou no banco
}
