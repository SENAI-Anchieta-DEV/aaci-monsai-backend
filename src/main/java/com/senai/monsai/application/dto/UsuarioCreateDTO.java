package com.senai.monsai.application.dto;

import com.senai.monsai.domain.enums.TipoUsuario;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class UsuarioCreateDTO{
        private String nome;
        private String email;
        private String senha;
        private String cpf;
        @Enumerated(EnumType.STRING)
        private TipoUsuario tipoUsuario;
        private Long asiloId;
}
