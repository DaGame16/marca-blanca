package com.marcablanca.platform.correo.infrastructure.web;

/** secretoRef y usuario pueden venir null -- no todo proveedor SMTP exige autenticacion. */
public record ConfiguracionCorreoRequest(String remitenteNombre, String remitenteCorreo, String responderA,
                                          String host, int puerto, String usuario, String secretoRef,
                                          String seguridad) {
}
