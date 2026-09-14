package com.marcablanca.platform.correo.domain;

import java.util.regex.Pattern;

/** Value object -- valida formato y normaliza a minuscula. */
public record DireccionCorreo(String valor) {

    // No se cambia a cuantificadores posesivos (java:S8786): el grupo del
    // dominio necesita poder devolver caracteres para que el "." literal
    // matchee (ej. "a@b.c" con [^\s@]++ posesivo se comeria el "." completo y
    // dejaria de validar direcciones validas). Sin grupos anidados -- backtracking
    // acotado (lineal), no exponencial; el riesgo real de ReDoS es bajo.
    private static final Pattern PATRON = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"); //NOSONAR ver comentario arriba

    public DireccionCorreo {
        if (valor == null || !PATRON.matcher(valor).matches()) {
            throw new DireccionCorreoInvalidaException(valor);
        }
        valor = valor.strip().toLowerCase();
    }
}
