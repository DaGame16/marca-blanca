package com.marcablanca.platform.usuarios.domain;

import java.util.regex.Pattern;

public record Correo(String valor) {

    private static final Pattern FORMATO_VALIDO =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    public Correo {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El correo no puede estar vacio.");
        }
        valor = valor.trim().toLowerCase();
        if (!FORMATO_VALIDO.matcher(valor).matches()) {
            throw new IllegalArgumentException("El correo '" + valor + "' no tiene un formato valido.");
        }
    }
}
