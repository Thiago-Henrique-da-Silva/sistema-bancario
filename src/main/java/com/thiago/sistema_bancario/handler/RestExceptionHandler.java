package com.thiago.sistema_bancario.handler;

import com.thiago.sistema_bancario.exception.BusinessException;
import com.thiago.sistema_bancario.exception.DataAlreadyRegisteredException;
import com.thiago.sistema_bancario.exception.InvalidInputExeption;
import com.thiago.sistema_bancario.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request){

        List<InvalidField> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> new InvalidField(error.getField(), error.getDefaultMessage()))
                .toList();

        ProblemDetail problemDetail = ex.getBody();
        problemDetail.setTitle("Campos inválidos");
        problemDetail.setDetail("Um ou mais campos estão inválidos.");
        problemDetail.setProperty("erros", errors);

        return handleExceptionInternal(ex, problemDetail, headers, status, request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    protected ProblemDetail resourceNotFoundExceptionHandler(ResourceNotFoundException ex){
        log.warn("recurso nao encontrado: {}", ex.getMessage());

        return createProblemDetail(HttpStatus.NOT_FOUND,"Recurso não encontrado.",ex.getMessage());
    }

    @ExceptionHandler(DataAlreadyRegisteredException.class)
    protected ProblemDetail dataAlreadyRegisteredExceptionHandler(DataAlreadyRegisteredException ex){
        log.warn("recurso ja cadastrado: {}", ex.getMessage());

        return createProblemDetail(HttpStatus.CONFLICT,"Recurso ja cadastrado.",ex.getMessage());
    }

    @ExceptionHandler(InvalidInputExeption.class)
    protected ProblemDetail invalidInputExceptionHandler(InvalidInputExeption ex){
        log.warn("Valor invalido: {}", ex.getMessage());

        return createProblemDetail(HttpStatus.BAD_REQUEST,"Valor invalido.",ex.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    protected ProblemDetail businessExceptionHandler(BusinessException ex){
        log.warn("Regra de negocio violada: {}", ex.getMessage());

        return createProblemDetail(HttpStatus.UNPROCESSABLE_ENTITY, "Regra de negocio violada.",ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex){
        log.error("Violação de integridade no banco.", ex);

        return createProblemDetail(HttpStatus.CONFLICT, "Conflito de dados.",
                "Os dados informados entram em conflito com um registro existente.");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception ex){
        log.error("erro inesperado", ex);

        return createProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado.",
                "erro inesperado, tente novamente mais tarde.");
    }

    private ProblemDetail createProblemDetail(HttpStatus status, String title, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}
