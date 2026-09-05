package com.marcablanca.platform.usuarios.domain;

public class CorreoYaRegistradoException extends RuntimeException {
    public CorreoYaRegistradoException(String correo) {
        super("Ya existe un usuario registrado con el correo: " + correo);
    }
}