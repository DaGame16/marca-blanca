package com.marcablanca.platform.correo.domain;

import java.util.regex.Pattern;

/** Value object -- valida formato y normaliza a minuscula. */
public record DireccionCorreo(String valor) {

    private static final Pattern PATRON = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public DireccionCorreo {
        if (valor == null || !PATRON.matcher(valor).matches()) {
            throw new DireccionCorreoInvalidaException(valor);
        }
        valor = valor.strip().toLowerCase();
    }
}
