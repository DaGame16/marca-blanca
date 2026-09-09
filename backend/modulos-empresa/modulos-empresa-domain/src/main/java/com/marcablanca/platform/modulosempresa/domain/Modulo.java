package com.marcablanca.platform.modulosempresa.domain;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Un modulo del catalogo de la plataforma (tbl_modulos). El precio es el valor
 * mensual que se le factura a una empresa que lo tenga activo.
 */
public record Modulo(
        UUID id,
        String codigo,
        String nombre,
        String descripcion,
        BigDecimal precio,
        String moneda) {

    public Modulo {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El codigo del modulo es obligatorio.");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del modulo es obligatorio.");
        }
        if (precio == null || precio.signum() < 0) {
            throw new IllegalArgumentException("El precio del modulo no puede ser negativo.");
        }
        if (moneda == null || moneda.length() != 3) {
            throw new IllegalArgumentException("La moneda debe ser un codigo ISO de 3 letras (ej: COP).");
        }
        codigo = codigo.trim().toLowerCase();
        nombre = nombre.trim();
        descripcion = descripcion == null || descripcion.isBlank() ? null : descripcion.trim();
        moneda = moneda.trim().toUpperCase();
    }

    /** Nuevo modulo del catalogo (id generado). */
    public static Modulo nuevo(String codigo, String nombre, String descripcion, BigDecimal precio, String moneda) {
        return new Modulo(UUID.randomUUID(), codigo, nombre, descripcion, precio, moneda);
    }
}
