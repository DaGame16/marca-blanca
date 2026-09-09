package com.marcablanca.platform.aprovisionamiento.application;

/** Datos de contacto editables de una empresa (los del paso 1 del wizard). */
public record DatosDeEmpresa(
        String nombreLegal,
        String representanteLegal,
        String correo,
        String telefono,
        String sitioWeb) {
}
