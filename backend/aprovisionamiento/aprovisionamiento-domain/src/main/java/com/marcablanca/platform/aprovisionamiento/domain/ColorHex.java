package com.marcablanca.platform.aprovisionamiento.domain;

import java.util.regex.Pattern;

/** Color en hexadecimal (#RRGGBB). Inmutable y validado al construirse. */
public record ColorHex(String valor) {

    private static final Pattern FORMATO = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    public ColorHex {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El color no puede estar vacio.");
        }
        valor = valor.trim().toUpperCase();
        if (!FORMATO.matcher(valor).matches()) {
            throw new IllegalArgumentException(
                    "El color '" + valor + "' no tiene formato hexadecimal valido (ej: #FF5733).");
        }
    }
}
