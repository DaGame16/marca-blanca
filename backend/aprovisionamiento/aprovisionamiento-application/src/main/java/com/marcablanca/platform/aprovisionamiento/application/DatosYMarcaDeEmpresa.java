package com.marcablanca.platform.aprovisionamiento.application;

import java.util.UUID;

/**
 * Datos de contacto + personalizacion visual de una empresa, tal como se leen de
 * la base de control para la pantalla de edicion. Los campos de marca pueden ser
 * null si la empresa nunca los configuro (los tipos caen a 1 por el default de
 * la tabla).
 */
public record DatosYMarcaDeEmpresa(
        UUID id,
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
        int tipoPantallaPrincipal) {
}
