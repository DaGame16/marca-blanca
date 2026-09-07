package com.marcablanca.platform.aprovisionamiento.domain;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Identificador de la empresa: el "slug" que nombra su base fisica db_cliente_<identificador>.
 * Inmutable y validado al construirse: si un Identificador existe, es un nombre de base valido.
 */
public record Identificador(String valor) {

    // letra minuscula al inicio; luego grupos alfanumericos separados por un solo guion bajo.
    // sin guion bajo al inicio, al final, ni dos seguidos.
    private static final Pattern FORMATO = Pattern.compile("^[a-z][a-z0-9]*(?:_[a-z0-9]+)*$");

    private static final int LARGO_MIN = 3;
    // "db_cliente_" son 11 caracteres; el limite de identificador de PostgreSQL es 63 => quedarian 52.
    // Se topa en 40 para dejar margen.
    private static final int LARGO_MAX = 40;

    private static final Set<String> RESERVADOS = Set.of(
            "postgres", "public", "template0", "template1",
            "control", "plantilla", "plantilla_maestra",
            "portal_guajiranet_control", "db_portal_guajiranet_control"
    );

    public Identificador {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El identificador de la empresa no puede estar vacio.");
        }
        valor = valor.trim().toLowerCase();

        if (valor.length() < LARGO_MIN || valor.length() > LARGO_MAX) {
            throw new IllegalArgumentException(
                    "El identificador debe tener entre " + LARGO_MIN + " y " + LARGO_MAX + " caracteres.");
        }
        if (!FORMATO.matcher(valor).matches()) {
            throw new IllegalArgumentException(
                    "El identificador '" + valor + "' no es valido: minusculas, digitos y guion bajo, "
                    + "empezando por letra, sin guiones bajos al inicio, al final ni repetidos.");
        }
        if (valor.startsWith("pg_")) {
            throw new IllegalArgumentException(
                    "El identificador no puede empezar por 'pg_' (prefijo reservado de PostgreSQL).");
        }
        if (RESERVADOS.contains(valor)) {
            throw new IllegalArgumentException("El identificador '" + valor + "' esta reservado.");
        }
    }

    /** Nombre de la base fisica de esta empresa. Lo usa la Capa 2 para el CREATE DATABASE. */
    public String nombreBaseDeDatos() {
        return "db_cliente_" + valor;
    }
}