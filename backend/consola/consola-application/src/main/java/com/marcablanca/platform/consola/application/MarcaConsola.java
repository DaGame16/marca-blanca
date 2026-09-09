package com.marcablanca.platform.consola.application;

/**
 * Entrada para editar la marca de una empresa desde la consola. Colores y logo
 * opcionales (null = sin definir); los tipos deben estar entre 1 y 3.
 */
public record MarcaConsola(
        String colorPrimario,
        String colorSecundario,
        String urlLogo,
        int tipoLogin,
        int tipoPantallaPrincipal) {
}
