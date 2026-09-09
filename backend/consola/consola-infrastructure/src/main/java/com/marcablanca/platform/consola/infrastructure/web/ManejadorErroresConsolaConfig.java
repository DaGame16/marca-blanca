package com.marcablanca.platform.consola.infrastructure.web;

import com.marcablanca.platform.consola.domain.CredencialesDeOperadorInvalidasException;
import com.marcablanca.platform.modulosempresa.domain.ModuloEnUsoException;
import com.marcablanca.platform.modulosempresa.domain.ModuloNoEncontradoException;
import com.marcablanca.platform.modulosempresa.domain.ModuloYaExisteException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Errores de los CRUD de configuracion de la consola (correo, catalogo de
 * modulos). Las excepciones del modulo `correo`
 * ({@code ConfiguracionCorreoNoEncontradaException}, {@code IllegalStateException})
 * ya las traduce el advice global de ese modulo.
 */
@RestControllerAdvice(assignableTypes = {
        ConsolaConfigCorreoController.class,
        ConsolaCatalogoModulosController.class
})
public class ManejadorErroresConsolaConfig {

    public record ErrorConsola(int codigo, String mensaje) {
    }

    @ExceptionHandler(CredencialesDeOperadorInvalidasException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorConsola credenciales(CredencialesDeOperadorInvalidasException e) {
        return new ErrorConsola(401, e.getMessage());
    }

    @ExceptionHandler(ModuloNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorConsola noEncontrado(ModuloNoEncontradoException e) {
        return new ErrorConsola(404, e.getMessage());
    }

    @ExceptionHandler({ ModuloYaExisteException.class, ModuloEnUsoException.class })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorConsola conflicto(RuntimeException e) {
        return new ErrorConsola(409, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorConsola invalido(IllegalArgumentException e) {
        return new ErrorConsola(400, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorConsola cuerpoInvalido(MethodArgumentNotValidException e) {
        return new ErrorConsola(400, "Datos invalidos para la operacion.");
    }
}
