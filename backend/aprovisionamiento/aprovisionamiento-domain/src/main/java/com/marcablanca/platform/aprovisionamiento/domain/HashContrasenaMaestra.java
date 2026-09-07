package com.marcablanca.platform.aprovisionamiento.domain;

/** Hash ya cifrado de la contrasena maestra de la empresa. El dominio no sabe con que algoritmo se genero. */
public record HashContrasenaMaestra(String valor) {
    public HashContrasenaMaestra {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El hash de la contrasena maestra no puede estar vacio.");
        }
    }
}