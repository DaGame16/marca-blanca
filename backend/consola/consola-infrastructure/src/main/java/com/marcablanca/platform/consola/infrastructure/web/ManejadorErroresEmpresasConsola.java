package com.marcablanca.platform.consola.infrastructure.web;

import com.marcablanca.platform.aprovisionamiento.domain.EmpresaNoEditableException;
import com.marcablanca.platform.aprovisionamiento.domain.EmpresaNoEncontradaException;
import com.marcablanca.platform.aprovisionamiento.domain.EmpresaNoReactivableException;
import com.marcablanca.platform.aprovisionamiento.domain.EmpresaNoSuspendibleException;
import com.marcablanca.platform.consola.domain.CredencialesDeOperadorInvalidasException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Codigos HTTP para las acciones de administracion de empresas de la consola. */
@RestControllerAdvice(assignableTypes = ConsolaEmpresasController.class)
public class ManejadorErroresEmpresasConsola {

    public record ErrorConsola(int codigo, String mensaje) {
    }

    @ExceptionHandler(CredencialesDeOperadorInvalidasException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorConsola credenciales(CredencialesDeOperadorInvalidasException e) {
        return new ErrorConsola(401, e.getMessage());
    }

    @ExceptionHandler(EmpresaNoEncontradaException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorConsola noEncontrada(EmpresaNoEncontradaException e) {
        return new ErrorConsola(404, e.getMessage());
    }

    @ExceptionHandler({
            EmpresaNoSuspendibleException.class,
            EmpresaNoReactivableException.class,
            EmpresaNoEditableException.class
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorConsola transicionInvalida(RuntimeException e) {
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
