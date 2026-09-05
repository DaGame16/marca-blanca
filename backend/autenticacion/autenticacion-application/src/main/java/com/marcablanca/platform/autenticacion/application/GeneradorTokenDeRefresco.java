package com.marcablanca.platform.autenticacion.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Genera y hashea el valor del refresh token. Package-private a proposito:
 * detalle interno de AutenticarUsuarioService y RenovarTokenService.
 *
 * Usa SHA-256, NO BCrypt: BCrypt esta pensado para ir lento a proposito
 * (protege contraseñas humanas, faciles de adivinar). Un refresh token es
 * un valor aleatorio de 256 bits -- imposible de adivinar sin importar
 * la velocidad del hash. SHA-256 alcanza y sobra, y no agrega latencia
 * innecesaria en cada renovacion.
 */
final class GeneradorTokenDeRefresco {

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private GeneradorTokenDeRefresco() {
    }

    static String generarValor() {
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String hashear(String valor) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha256.digest(valor.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
