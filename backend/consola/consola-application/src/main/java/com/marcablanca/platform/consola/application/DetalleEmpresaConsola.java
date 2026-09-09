package com.marcablanca.platform.consola.application;

import java.util.List;

/** Detalle completo de una empresa para la pantalla de edicion de la consola. */
public record DetalleEmpresaConsola(
        String id,
        String identificador,
        String nombreLegal,
        String dominio,
        String representanteLegal,
        String correo,
        String telefono,
        String sitioWeb,
        String estado,
        String colorPrimario,
        String colorSecundario,
        String urlLogo,
        int tipoLogin,
        int tipoPantallaPrincipal,
        List<ModuloConsola> modulos) {

    public record ModuloConsola(String codigo, String nombre, boolean activo) {
    }
}
