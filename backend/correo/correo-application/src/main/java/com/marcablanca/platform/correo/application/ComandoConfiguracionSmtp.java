package com.marcablanca.platform.correo.application;

/**
 * Datos de una configuracion SMTP, compartidos entre el puerto de entrada
 * (GestionarConfiguracionCorreo) y el de salida (RepositorioConfiguracionCorreo)
 * para no repetir la misma lista de 9 parametros en cada capa (java:S107).
 *
 * clave: la clave SMTP en texto plano (se cifra antes de guardar) -- null
 * significa "no cambiarla".
 */
public record ComandoConfiguracionSmtp(String remitenteNombre, String remitenteCorreo, String responderA,
                                        String host, int puerto, String usuario, String secretoRef,
                                        String seguridad, String clave) {
}
