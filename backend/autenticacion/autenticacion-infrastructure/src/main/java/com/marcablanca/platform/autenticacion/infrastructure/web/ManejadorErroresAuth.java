package com.marcablanca.platform.autenticacion.infrastructure.web;

import com.marcablanca.platform.autenticacion.domain.TokenDeRefrescoInvalidoException;
import com.marcablanca.platform.usuarios.domain.CredencialesInvalidasException;
import com.marcablanca.platform.usuarios.domain.UsuarioNoDisponibleException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ManejadorErroresAuth {

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponse> manejarCredencialesInvalidas(
            CredencialesInvalidasException ex, HttpServletRequest request) {
        return construirRespuesta(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(UsuarioNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> manejarUsuarioNoDisponible(
            UsuarioNoDisponibleException ex, HttpServletRequest request) {
        return construirRespuesta(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(TokenDeRefrescoInvalidoException.class)
    public ResponseEntity<ErrorResponse> manejarTokenDeRefrescoInvalido(
            TokenDeRefrescoInvalidoException ex, HttpServletRequest request) {
        return construirRespuesta(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    private ResponseEntity<ErrorResponse> construirRespuesta(
            HttpStatus estado, String mensaje, HttpServletRequest request) {
        ErrorResponse cuerpo = new ErrorResponse(estado.value(), mensaje, Instant.now(), request.getRequestURI());
        return ResponseEntity.status(estado).body(cuerpo);
    }
}
