package com.senai.monsai.domain.exception;

public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException() {
        super("Usuário não encontrado.");
    }
}