package com.marcablanca.platform.identidadvisual.domain;

/**
 * Los 4 campos son opcionales -- ninguno tiene constraint NOT NULL en la
 * tabla (una empresa puede no haber configurado su marca todavia).
 * Por ahora son 2 colores (primario/secundario) -- confirmado con Luis
 * que un tercero, si llega a hacer falta, requiere una columna nueva en
 * la tabla real primero (coordinar con Leidi antes de agregarlo aca).
 */
public record MarcaDeEmpresa(String urlLogo, ColorHex colorPrimario, ColorHex colorSecundario, String dominioPropio) {
}
