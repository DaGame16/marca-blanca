package com.marcablanca.platform.aprovisionamiento.infrastructure.web;

import com.marcablanca.platform.aprovisionamiento.domain.EmpresaNoActivableException;
import com.marcablanca.platform.aprovisionamiento.domain.EmpresaNoEncontradaException;
import com.marcablanca.platform.aprovisionamiento.domain.EmpresaNoModificableException;
import com.marcablanca.platform.aprovisionamiento.domain.EmpresaYaExisteException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

/** Acotado a estos controllers: el handler de IllegalArgumentException es amplio y no debe pisar a otros modulos. */
@RestControllerAdvice(assignableTypes = {
        AltaEmpresaController.class,
        RegistroModulosController.class,
        RegistroPersonalizacionController.class })
class ManejadorErroresAprovisionamiento {

    @ExceptionHandler(EmpresaYaExisteException.class)
    ResponseEntity<ErrorResponse> yaExiste(EmpresaYaExisteException ex, HttpServletRequest req) {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(EmpresaNoEncontradaException.class)
    ResponseEntity<ErrorResponse> noEncontrada(EmpresaNoEncontradaException ex, HttpServletRequest req) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(EmpresaNoModificableException.class)
    ResponseEntity<ErrorResponse> noModificable(EmpresaNoModificableException ex, HttpServletRequest req) {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(EmpresaNoActivableException.class)
    ResponseEntity<ErrorResponse> noActivable(EmpresaNoActivableException ex, HttpServletRequest req) {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ErrorResponse> datosInvalidos(IllegalArgumentException ex, HttpServletRequest req) {
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return construir(HttpStatus.BAD_REQUEST, detalle, req);
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus estado, String mensaje, HttpServletRequest req) {
        return ResponseEntity.status(estado)
                .body(new ErrorResponse(estado.value(), mensaje, Instant.now(), req.getRequestURI()));
    }
}