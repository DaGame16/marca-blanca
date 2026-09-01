package com.marcablanca.platform.usuarios.domain;

/** El valor ya cifrado que se guarda en base de datos. El dominio no sabe con que algoritmo se genero. */
public record HashContrasena(String valor) {

    public HashContrasena {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El hash de la contrasena no puede estar vacio.");
        }
    }
}
