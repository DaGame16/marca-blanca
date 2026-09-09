package com.marcablanca.platform.correo.domain;

import java.util.UUID;

public class ConfiguracionCorreoNoEncontradaException extends RuntimeException {
    public ConfiguracionCorreoNoEncontradaException(UUID id) {
        super("No existe una configuracion de correo con id: " + id);
    }
}
