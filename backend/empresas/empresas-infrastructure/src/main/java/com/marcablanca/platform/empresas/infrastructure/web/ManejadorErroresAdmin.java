package com.marcablanca.platform.empresas.infrastructure.web;

import com.marcablanca.platform.empresas.domain.EmpresaNoEncontradaException;
import com.marcablanca.platform.empresas.domain.ModuloNoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
class ManejadorErroresAdmin {

    @ExceptionHandler(ClaveAdminInvalidaException.class)
    public ResponseEntity<ErrorResponse> manejarClaveInvalida(ClaveAdminInvalidaException ex, HttpServletRequest request) {
        return construir(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(EmpresaNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> manejarEmpresaNoEncontrada(EmpresaNoEncontradaException ex, HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ModuloNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> manejarModuloNoEncontrado(ModuloNoEncontradoException ex, HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus estado, String mensaje, HttpServletRequest request) {
        return ResponseEntity.status(estado)
                .body(new ErrorResponse(estado.value(), mensaje, Instant.now(), request.getRequestURI()));
    }
}
