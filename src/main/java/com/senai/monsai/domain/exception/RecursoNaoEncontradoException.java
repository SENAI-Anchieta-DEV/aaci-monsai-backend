package com.senai.monsai.domain.exception;

public class RecursoNaoEncontradoException extends RuntimeException {

    // Construtor vazio (padrão)
    public RecursoNaoEncontradoException() {
        super("Recurso não encontrado.");
    }

    // Construtor que aceita a mensagem customizada (Precisamos desse!)
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}