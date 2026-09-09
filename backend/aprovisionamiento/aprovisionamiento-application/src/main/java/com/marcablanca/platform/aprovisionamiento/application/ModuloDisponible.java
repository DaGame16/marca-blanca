package com.marcablanca.platform.aprovisionamiento.application;

/** Un modulo del catalogo con su estado para una empresa concreta. */
public record ModuloDisponible(String codigo, String nombre, boolean activo) {
}
