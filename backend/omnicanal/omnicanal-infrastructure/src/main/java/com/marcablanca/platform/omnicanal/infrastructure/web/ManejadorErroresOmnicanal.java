package com.marcablanca.platform.omnicanal.infrastructure.web;

import com.marcablanca.platform.omnicanal.domain.CasoNoEncontradoException;
import com.marcablanca.platform.omnicanal.domain.ConversacionNoEncontradaException;
import com.marcablanca.platform.omnicanal.domain.WebhookSecretoInvalidoException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
class ManejadorErroresOmnicanal {

    @ExceptionHandler(WebhookSecretoInvalidoException.class)
    public ResponseEntity<ErrorResponse> manejarSecretoInvalido(WebhookSecretoInvalidoException ex, HttpServletRequest r) {
        return construir(HttpStatus.UNAUTHORIZED, ex.getMessage(), r);
    }

    @ExceptionHandler({ConversacionNoEncontradaException.class, CasoNoEncontradoException.class})
    public ResponseEntity<ErrorResponse> manejarNoEncontrado(RuntimeException ex, HttpServletRequest r) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), r);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> manejarNoDisponible(IllegalStateException ex, HttpServletRequest r) {
        return construir(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), r);
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus estado, String mensaje, HttpServletRequest r) {
        return ResponseEntity.status(estado).body(new ErrorResponse(estado.value(), mensaje, Instant.now(), r.getRequestURI()));
    }
}
