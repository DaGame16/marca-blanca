package com.marcablanca.platform.identidadvisual.infrastructure.web;

import com.marcablanca.platform.identidadvisual.domain.EmpresaNoEncontradaException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
class ManejadorErroresIdentidadVisual {

    @ExceptionHandler(EmpresaNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> manejarEmpresaNoEncontrada(EmpresaNoEncontradaException ex, HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> manejarArgumentoInvalido(IllegalArgumentException ex, HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus estado, String mensaje, HttpServletRequest request) {
        return ResponseEntity.status(estado)
                .body(new ErrorResponse(estado.value(), mensaje, Instant.now(), request.getRequestURI()));
    }
}
