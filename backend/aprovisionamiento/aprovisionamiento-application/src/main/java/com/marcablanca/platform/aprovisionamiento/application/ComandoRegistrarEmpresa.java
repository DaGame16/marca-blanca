package com.marcablanca.platform.aprovisionamiento.application;

/** Datos del paso 1 del registro de empresa (formulario publico). */
public record ComandoRegistrarEmpresa(
        String nombreEmpresa,
        String representanteLegal,
        String correo,
        String telefono,
        String sitioWeb
) {
}
