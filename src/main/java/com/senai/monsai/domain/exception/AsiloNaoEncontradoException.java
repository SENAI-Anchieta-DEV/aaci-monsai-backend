package com.senai.monsai.domain.exception;

public class AsiloNaoEncontradoException extends RecursoNaoEncontradoException {

    public AsiloNaoEncontradoException(Long idAsilo) {
        super("Asilo com ID " + idAsilo + " não foi encontrado.");
    }
}