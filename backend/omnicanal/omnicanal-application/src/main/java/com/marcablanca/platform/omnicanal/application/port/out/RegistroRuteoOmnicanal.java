package com.marcablanca.platform.omnicanal.application.port.out;

/**
 * Fila de ruteo del webhook en la base de CONTROL (plataforma.tbl_empresas_omnicanal),
 * para la empresa activa en la peticion. Se crea sola la primera vez que la
 * empresa entra a configurar omnicanal -- que es justo cuando necesita el
 * secreto para pegarlo en LIWA.
 */
public interface RegistroRuteoOmnicanal {

    /** Secreto actual de la empresa activa; lo crea si todavia no existe. */
    String secretoWebhook();

    /** Genera un secreto nuevo para la empresa activa y devuelve el nuevo. */
    String rotarSecretoWebhook();
}
