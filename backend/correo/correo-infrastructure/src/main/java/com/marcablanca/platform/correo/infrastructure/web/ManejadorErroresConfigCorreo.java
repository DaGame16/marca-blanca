package com.marcablanca.platform.correo.infrastructure.web;

import com.marcablanca.platform.correo.domain.ConfiguracionCorreoNoEncontradaException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
class ManejadorErroresConfigCorreo {

    @ExceptionHandler(ConfiguracionCorreoNoEncontradaException.class)
    public ResponseEntity<ErrorResponseConfigCorreo> manejarNoEncontrada(ConfiguracionCorreoNoEncontradaException ex,
                                                                          HttpServletRequest r) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), r);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseConfigCorreo> manejarEstadoInvalido(IllegalStateException ex,
                                                                            HttpServletRequest r) {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), r);
    }

    private ResponseEntity<ErrorResponseConfigCorreo> construir(HttpStatus estado, String mensaje,
                                                                  HttpServletRequest r) {
        return ResponseEntity.status(estado)
                .body(new ErrorResponseConfigCorreo(estado.value(), mensaje, Instant.now(), r.getRequestURI()));
    }
}
