package com.marcablanca.platform.correo.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

/**
 * Cifra/descifra la clave SMTP antes de guardarla en tbl_config_correo -- no
 * puede viajar en texto plano en la base de datos.
 *
 * La llave de cifrado (app.correo.clave-maestra) SI sigue viniendo de una
 * variable de entorno -- pero es un secreto de INFRAESTRUCTURA (protege lo
 * que hay en la tabla), no la clave SMTP de ningun proveedor de correo en
 * particular. Un admin puede crear, editar y rotar tantas configuraciones
 * SMTP como quiera sin tocar el backend; la llave maestra practicamente
 * nunca cambia.
 */
@Component
public class CifradorDeCorreo {

    private final TextEncryptor encryptor;

    public CifradorDeCorreo(@Value("${app.correo.clave-maestra:solo-para-desarrollo-local-cambiar-siempre}") String claveMaestra) {
        // Encryptors.text exige un salt hexadecimal -- no es secreto (va junto a lo
        // cifrado, como un IV), solo tiene que ser fijo para que cifrar/descifrar
        // usen la misma derivacion de llave.
        this.encryptor = Encryptors.text(claveMaestra, "d3d1a2c5b6e7f809");
    }

    public String cifrar(String texto) {
        return texto == null ? null : encryptor.encrypt(texto);
    }

    public String descifrar(String textoCifrado) {
        return textoCifrado == null ? null : encryptor.decrypt(textoCifrado);
    }
}
