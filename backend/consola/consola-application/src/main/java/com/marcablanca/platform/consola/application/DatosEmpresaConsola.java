package com.marcablanca.platform.consola.application;

/** Entrada para editar los datos de contacto de una empresa desde la consola. */
public record DatosEmpresaConsola(
        String nombreLegal,
        String representanteLegal,
        String correo,
        String telefono,
        String sitioWeb) {
}
