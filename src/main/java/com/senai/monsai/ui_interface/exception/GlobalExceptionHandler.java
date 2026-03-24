package com.senai.monsai.ui_interface.exception;

import com.senai.monsai.domain.exception.RecursoDuplicadoException;
import com.senai.monsai.domain.exception.RecursoNaoEncontradoException;
import com.senai.monsai.domain.exception.RegraNegocioException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // substitui a classe erro padrão apos lembrar da existencia de uma classe
    // do spring que já fazia isso
    // =======================================================
    // 404 NOT FOUND - Recurso não encontrado (Idoso, Usuário, etc)
    // =======================================================
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail handleRecursoNaoEncontrado(RecursoNaoEncontradoException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        return problemDetail;
    }

    // =======================================================
    // 400 BAD REQUEST - Regras de Negócio violadas
    // =======================================================
    @ExceptionHandler(RegraNegocioException.class)
    public ProblemDetail handleRegraNegocio(RegraNegocioException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        return problemDetail;
    }

    // =======================================================
    // 409 CONFLICT - Violação de Constraint no Banco de Dados (E-mail ou Serial único)
    // =======================================================
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Conflito de dados: Já existe um registro no banco de dados com esta mesma informação (ex: e-mail ou serial de dispositivo).");
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        return problemDetail;
    }
    // =======================================================
    // 400 BAD REQUEST - Erros de validação do DTO (@NotBlank, @Email)
    // =======================================================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        String mensagemDeErro = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Erro de validação: " + mensagemDeErro);
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        return problemDetail;
    }

    // =======================================================
    // 400 BAD REQUEST - JSON mal formatado na requisição
    // =======================================================
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Corpo da requisição (JSON) ausente ou mal formatado.");
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        return problemDetail;
    }

    // =======================================================
    // 400 BAD REQUEST - Parâmetro de URL com tipo errado (ex: texto no lugar de ID)
    // =======================================================
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Parâmetro de URL inválido. Verifique os dados enviados.");
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        return problemDetail;
    }

    // =======================================================
    // 401 UNAUTHORIZED - E-mail ou Senha incorretos no Login
    // =======================================================
    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Credenciais inválidas. Verifique seu e-mail e senha e tente novamente.");
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        return problemDetail;
    }

    // =======================================================
    // 403 FORBIDDEN - Usuário sem permissão (Spring Security)
    // =======================================================
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Acesso negado. Você não tem permissão para executar esta ação.");
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        return problemDetail;
    }

    // =======================================================
    // 500 INTERNAL SERVER ERROR - O "Catch-all" genérico
    // =======================================================
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        ex.printStackTrace(); // Útil para você ver o erro real no terminal
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro interno inesperado no servidor.");
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        return problemDetail;
    }
}