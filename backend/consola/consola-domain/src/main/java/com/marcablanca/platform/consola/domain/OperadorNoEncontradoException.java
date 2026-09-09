package com.marcablanca.platform.consola.domain;

import java.util.UUID;

/** No existe un operador con el id indicado. */
public class OperadorNoEncontradoException extends RuntimeException {

    public OperadorNoEncontradoException(UUID id) {
        super("No existe el operador " + id + ".");
    }
}
