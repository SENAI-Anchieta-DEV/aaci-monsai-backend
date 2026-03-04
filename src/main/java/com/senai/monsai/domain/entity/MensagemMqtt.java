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
public class MensagemMqtt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String topico;

    @Column(columnDefinition = "TEXT")
    private String conteudo;

    private LocalDateTime dataRecebimento;
}
