package com.senai.monsai.domain.entity;

import com.senai.monsai.domain.enums.TipoUsuario;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@SuperBuilder
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long usuario_id;
    private String nome;
    private String cpf;
    private String email;
    private String senha;
    @Enumerated(EnumType.STRING)
    private TipoUsuario tipo;
}
