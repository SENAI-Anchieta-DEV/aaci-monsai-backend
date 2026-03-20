package com.senai.monsai.domain.exception;

public class IdosoNaoEncontradoException extends RuntimeException {
    public IdosoNaoEncontradoException() {
        super("Idoso não encontrado");
    }
}
