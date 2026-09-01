package com.marcablanca.platform.usuarios.domain;

/** Representa una contrasena en texto plano, solo en el instante en que se recibe (login o registro). Nunca se persiste asi. */
public record Contrasena(String valorPlano) {

    private static final int LONGITUD_MINIMA = 8;

    public Contrasena {
        if (valorPlano == null || valorPlano.isBlank()) {
            throw new IllegalArgumentException("La contrasena no puede estar vacia.");
        }
        if (valorPlano.length() < LONGITUD_MINIMA) {
            throw new IllegalArgumentException(
                    "La contrasena debe tener al menos " + LONGITUD_MINIMA + " caracteres.");
        }
    }
}
