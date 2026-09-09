package com.marcablanca.platform.consola.infrastructure.web;

import com.marcablanca.platform.consola.domain.CredencialesDeOperadorInvalidasException;
import com.marcablanca.platform.consola.domain.OperadorInactivoException;
import com.marcablanca.platform.consola.domain.OperadorNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduce las excepciones de la consola de autenticacion a codigos HTTP. */
@RestControllerAdvice(assignableTypes = ConsolaAuthController.class)
public class ManejadorErroresConsola {

    public record ErrorConsola(int codigo, String mensaje) {
    }

    @ExceptionHandler(CredencialesDeOperadorInvalidasException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorConsola credencialesInvalidas(CredencialesDeOperadorInvalidasException e) {
        return new ErrorConsola(401, e.getMessage());
    }

    @ExceptionHandler(OperadorInactivoException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorConsola inactivo(OperadorInactivoException e) {
        return new ErrorConsola(403, e.getMessage());
    }

    @ExceptionHandler(OperadorNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorConsola noEncontrado(OperadorNoEncontradoException e) {
        return new ErrorConsola(404, e.getMessage());
    }

    @ExceptionHandler({ IllegalArgumentException.class, MethodArgumentNotValidException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorConsola datosInvalidos(Exception e) {
        String mensaje = e instanceof IllegalArgumentException ? e.getMessage()
                : "Datos invalidos para la operacion.";
        return new ErrorConsola(400, mensaje);
    }
}
