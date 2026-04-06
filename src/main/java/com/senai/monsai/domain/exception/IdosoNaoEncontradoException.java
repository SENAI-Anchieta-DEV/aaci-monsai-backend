package com.senai.monsai.domain.exception;

public class IdosoNaoEncontradoException extends RecursoNaoEncontradoException {
    public IdosoNaoEncontradoException() {
        super("Idoso não encontrado");
    }
}
