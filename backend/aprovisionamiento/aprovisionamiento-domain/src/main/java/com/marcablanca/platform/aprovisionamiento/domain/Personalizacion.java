package com.marcablanca.platform.aprovisionamiento.domain;

/**
 * Personalizacion visual del wizard: 2 colores, logo y las variantes de UI
 * (1 de 3 login, 1 de 3 pantalla principal). Colores y logo son opcionales;
 * las variantes deben estar entre 1 y 3.
 */
public record Personalizacion(
        ColorHex colorPrimario,
        ColorHex colorSecundario,
        String urlLogo,
        int tipoLogin,
        int tipoPantallaPrincipal
) {
    public Personalizacion {
        if (tipoLogin < 1 || tipoLogin > 3) {
            throw new IllegalArgumentException("El tipo de login debe ser 1, 2 o 3.");
        }
        if (tipoPantallaPrincipal < 1 || tipoPantallaPrincipal > 3) {
            throw new IllegalArgumentException("El tipo de pantalla principal debe ser 1, 2 o 3.");
        }
        if (urlLogo != null && urlLogo.isBlank()) {
            urlLogo = null;
        }
    }
}
