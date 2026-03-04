package com.senai.monsai.domain.exception;



public class AsiloNaoEncontradoException extends RuntimeException {
    public AsiloNaoEncontradoException(Long idAsilo) {
        super("Asilo com ID " + idAsilo + " não foi encontrado.");
    }
}
