package com.restaurant.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolationException(org.springframework.dao.DataIntegrityViolationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "No se puede eliminar el registro porque está siendo referenciado por datos históricos (ej. ventas pasadas).");
        problem.setTitle("Data Integrity Violation");
        problem.setType(URI.create("https://api.restaurant.com/errors/conflict"));
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid Request");
        problem.setType(URI.create("https://api.restaurant.com/errors/bad-request"));
        return problem;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalStateException(IllegalStateException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("State Conflict");
        problem.setType(URI.create("https://api.restaurant.com/errors/conflict"));
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        if ("Invalid credentials".equals(ex.getMessage())) {
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos");
            problem.setTitle("Unauthorized");
            problem.setType(URI.create("https://api.restaurant.com/errors/unauthorized"));
            return problem;
        }
        
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problem.setTitle("Internal Server Error");
        problem.setType(URI.create("https://api.restaurant.com/errors/internal-error"));
        return problem;
    }

    @ExceptionHandler({org.springframework.transaction.CannotCreateTransactionException.class, org.springframework.dao.DataAccessResourceFailureException.class})
    public org.springframework.http.ResponseEntity<Object> handleDatabaseConnectionFailure(Exception ex) {
        return org.springframework.http.ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(java.util.Map.of(
                    "error", "Service Unavailable",
                    "message", "El sistema está experimentando problemas de conexión. Por favor intente más tarde."
                ));
    }
}
