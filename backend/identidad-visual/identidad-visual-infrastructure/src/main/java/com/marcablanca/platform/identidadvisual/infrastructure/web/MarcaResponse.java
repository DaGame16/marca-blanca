package com.marcablanca.platform.identidadvisual.infrastructure.web;

public record MarcaResponse(
        String urlLogo,
        String colorPrimario,
        String colorSecundario,
        String dominioPropio,
        Integer tipoLogin,
        Integer tipoPantallaPrincipal) {
}
