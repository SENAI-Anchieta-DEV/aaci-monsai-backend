package com.senai.monsai.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaFaixa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long idosoId;
    private String usuarioNome; // Quem alterou
    private LocalDateTime dataAlteracao;
    private String campoAlterado; // Ex: "BPM Máximo"
    private String valorAntigo;
    private String valorNovo;
}