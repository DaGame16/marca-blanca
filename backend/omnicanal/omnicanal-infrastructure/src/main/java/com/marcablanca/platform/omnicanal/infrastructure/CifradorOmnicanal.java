package com.marcablanca.platform.omnicanal.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Cifra/descifra el token de la API de LIWA antes de guardarlo en
 * omnicanal.tbl_configuracion_omnicanal -- no puede viajar en texto plano en
 * la base. Mismo patron que CifradorDeCorreo del modulo correo.
 *
 * La llave (app.omnicanal.clave-maestra) es un secreto de INFRAESTRUCTURA
 * (protege lo que hay en la tabla), no el token de LIWA de ninguna empresa.
 * El cifrado real (AES-256-GCM) vive en CifradorAesGcm.
 */
@Component
public class CifradorOmnicanal {

    private final CifradorAesGcm cifrador;

    public CifradorOmnicanal(
            @Value("${app.omnicanal.clave-maestra:solo-para-desarrollo-local-cambiar-siempre}") String claveMaestra) {
        // Salt hexadecimal fijo -- no es secreto (va junto a lo cifrado, como un IV),
        // solo tiene que ser estable para que cifrar/descifrar deriven la misma llave.
        this.cifrador = new CifradorAesGcm(claveMaestra, "a1b2c3d4e5f60718");
    }

    public String cifrar(String texto) {
        return texto == null ? null : cifrador.cifrar(texto);
    }

    public String descifrar(String textoCifrado) {
        return textoCifrado == null ? null : cifrador.descifrar(textoCifrado);
    }
}
