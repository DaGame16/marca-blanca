package com.marcablanca.platform.usuarios.domain;

public class UsuarioNoDisponibleException extends RuntimeException {
    public UsuarioNoDisponibleException(EstadoUsuario estado) {
        super("El usuario no puede autenticarse. Estado actual: " + estado);
    }
}
