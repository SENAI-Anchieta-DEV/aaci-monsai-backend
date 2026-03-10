package com.senai.monsai.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public record AtualizarSenhaDTO (

    @NotBlank(message = "A nova senha é obrigatória.")
    String novaSenha
){

}