package com.marcablanca.platform.correo.infrastructure.web;

import com.marcablanca.platform.correo.domain.ConfiguracionCorreoNoEncontradaException;
import com.marcablanca.platform.correo.domain.EnvioDeCorreoFallidoException;
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

    /** La validacion automatica (correo de prueba al crear/editar) no logro conectarse/autenticarse. */
    @ExceptionHandler(EnvioDeCorreoFallidoException.class)
    public ResponseEntity<ErrorResponseConfigCorreo> manejarEnvioFallido(EnvioDeCorreoFallidoException ex,
                                                                          HttpServletRequest r) {
        String detalle = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
        return construir(HttpStatus.BAD_GATEWAY, "No se pudo verificar el envio con esos datos: " + detalle, r);
    }

    private ResponseEntity<ErrorResponseConfigCorreo> construir(HttpStatus estado, String mensaje,
                                                                  HttpServletRequest r) {
        return ResponseEntity.status(estado)
                .body(new ErrorResponseConfigCorreo(estado.value(), mensaje, Instant.now(), r.getRequestURI()));
    }
}
