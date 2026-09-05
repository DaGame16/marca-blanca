package com.marcablanca.platform.identidadvisual.domain;

import java.util.regex.Pattern;

public record ColorHex(String valor) {

    private static final Pattern FORMATO_VALIDO = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    public ColorHex {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El color no puede estar vacio.");
        }
        valor = valor.trim().toUpperCase();
        if (!FORMATO_VALIDO.matcher(valor).matches()) {
            throw new IllegalArgumentException(
                    "El color '" + valor + "' no tiene un formato hexadecimal valido (ej: #FF5733).");
        }
    }
}
