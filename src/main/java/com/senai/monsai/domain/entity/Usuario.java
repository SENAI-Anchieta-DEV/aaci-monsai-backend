package com.senai.monsai.domain.entity;

import com.senai.monsai.domain.enums.TipoUsuario;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_id") // O banco continua vendo usuario_id
    private Long id; // Mas no Java chamamos só de "id"

    private String nome;

    private String cpf;

    @Column(unique = true)
    private String email;

    private String senha;

    @Enumerated(EnumType.STRING)
    private TipoUsuario tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asilo_id")
    private Asilo asilo;

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "usuario_idoso",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "idoso_id")
    )
    private List<Idoso> idosos = new ArrayList<>();

    @Builder.Default
    private boolean ativo = true;
}