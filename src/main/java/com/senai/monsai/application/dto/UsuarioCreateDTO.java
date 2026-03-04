package com.senai.monsai.application.dto;

import com.senai.monsai.domain.enums.TipoUsuario;
import lombok.Data;

@Data
public class UsuarioCreateDTO{
        private String nome;
        private String email;
        private String senha;
        private TipoUsuario tipoUsuario;
        private Long asiloId;
}
