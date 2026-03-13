package com.senai.monsai.ui_interface.exception;

import com.senai.monsai.domain.exception.RecursoNaoEncontradoException;
import com.senai.monsai.domain.exception.RegraNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // ======================================================= //documentado cada erro
    // 404 NOT FOUND - Recurso não encontrado (Idoso, Usuário, etc)
    // =======================================================
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroPadrao> handleRecursoNaoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest request) {
        ErroPadrao erro = new ErroPadrao(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    // =======================================================
    // 400 BAD REQUEST - Regras de Negócio violadas
    // =======================================================
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroPadrao> handleRegraNegocio(RegraNegocioException ex, HttpServletRequest request) {
        ErroPadrao erro = new ErroPadrao(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    // =======================================================
    // 409 CONFLICT - Conflito no banco (E-mail/CPF duplicado)
    // =======================================================
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroPadrao> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        ErroPadrao erro = new ErroPadrao(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Erro de conflito de dados. Possivelmente um registro duplicado (ex: E-mail ou CPF já em uso).",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    // =======================================================
    // 400 BAD REQUEST - Erros de validação do DTO (@NotBlank, @Email)
    // =======================================================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroPadrao> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String mensagemDeErro = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

        ErroPadrao erro = new ErroPadrao(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação: " + mensagemDeErro,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    // =======================================================
    // 400 BAD REQUEST - JSON mal formatado na requisição
    // =======================================================
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroPadrao> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        ErroPadrao erro = new ErroPadrao(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Corpo da requisição (JSON) ausente ou mal formatado.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    // =======================================================
    // 400 BAD REQUEST - Parâmetro de URL com tipo errado (ex: texto no lugar de ID)
    // =======================================================
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroPadrao> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        ErroPadrao erro = new ErroPadrao(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Parâmetro de URL inválido. Verifique os dados enviados.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    // =======================================================
    // 401 UNAUTHORIZED - E-mail ou Senha incorretos no Login
    // =======================================================
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErroPadrao> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        ErroPadrao erro = new ErroPadrao(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Credenciais inválidas. Verifique seu e-mail e senha e tente novamente.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(erro);
    }

    // =======================================================
    // 403 FORBIDDEN - Usuário sem permissão (Spring Security)
    // =======================================================
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroPadrao> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        ErroPadrao erro = new ErroPadrao(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                "Acesso negado. Você não tem permissão para executar esta ação.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(erro);
    }

    // =======================================================
    // 500 INTERNAL SERVER ERROR - O "Catch-all" genérico
    // =======================================================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroPadrao> handleGenericException(Exception ex, HttpServletRequest request) {
        ex.printStackTrace();

        ErroPadrao erro = new ErroPadrao(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ocorreu um erro interno inesperado no servidor.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
    }
}