package com.marcablanca.platform.correo.domain;

public class EnvioDeCorreoFallidoException extends RuntimeException {
    public EnvioDeCorreoFallidoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
