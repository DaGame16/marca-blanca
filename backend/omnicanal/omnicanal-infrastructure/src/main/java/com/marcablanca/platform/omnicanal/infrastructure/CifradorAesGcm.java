package com.marcablanca.platform.omnicanal.infrastructure;

import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Cifrado simetrico de secretos de infraestructura para guardar en base:
 * AES-256-GCM (cifra y autentica a la vez), IV aleatorio por mensaje, llave
 * derivada de la clave maestra con PBKDF2-HMAC-SHA256.
 *
 * <p>Formato de salida: {@code "1:" + Base64(iv[12] || ciphertext||tag)}. El
 * prefijo de version permite descifrar tambien el formato anterior
 * ({@code Encryptors.text} de Spring Security, que devuelve hex): se mantiene
 * la lectura por compatibilidad hasta que se roten todos los secretos ya
 * guardados. La escritura siempre usa el formato nuevo.
 *
 * <p>Reemplaza el uso directo de {@code Encryptors.text}, deprecado en Spring
 * Security 7 (toda la fabrica {@code Encryptors} lo esta) y con una derivacion
 * de llave mas debil que esta.
 */
final class CifradorAesGcm {

    private static final String PREFIJO_V1 = "1:";
    private static final int ITERACIONES_PBKDF2 = 310_000;
    private static final int BITS_LLAVE = 256;
    private static final int BYTES_IV = 12;
    private static final int BITS_TAG = 128;
    private static final String TRANSFORMACION = "AES/GCM/NoPadding";

    private final SecretKeySpec llave;
    private final SecureRandom random = new SecureRandom();
    private final TextEncryptor descifradorLegacy;

    CifradorAesGcm(String claveMaestra, String saltHex) {
        this.llave = derivarLlave(claveMaestra, HexFormat.of().parseHex(saltHex));
        this.descifradorLegacy = descifradorFormatoAnterior(claveMaestra, saltHex);
    }

    String cifrar(String texto) {
        try {
            byte[] iv = new byte[BYTES_IV];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMACION);
            cipher.init(Cipher.ENCRYPT_MODE, llave, new GCMParameterSpec(BITS_TAG, iv));
            byte[] cifrado = cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8));

            byte[] salida = new byte[iv.length + cifrado.length];
            System.arraycopy(iv, 0, salida, 0, iv.length);
            System.arraycopy(cifrado, 0, salida, iv.length, cifrado.length);
            return PREFIJO_V1 + Base64.getEncoder().encodeToString(salida);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo cifrar el secreto", e);
        }
    }

    String descifrar(String valor) {
        if (!valor.startsWith(PREFIJO_V1)) {
            return descifradorLegacy.decrypt(valor);
        }
        try {
            byte[] datos = Base64.getDecoder().decode(valor.substring(PREFIJO_V1.length()));
            byte[] iv = Arrays.copyOfRange(datos, 0, BYTES_IV);
            byte[] cifrado = Arrays.copyOfRange(datos, BYTES_IV, datos.length);
            Cipher cipher = Cipher.getInstance(TRANSFORMACION);
            cipher.init(Cipher.DECRYPT_MODE, llave, new GCMParameterSpec(BITS_TAG, iv));
            return new String(cipher.doFinal(cifrado), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo descifrar el secreto", e);
        }
    }

    private static SecretKeySpec derivarLlave(String claveMaestra, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(claveMaestra.toCharArray(), salt, ITERACIONES_PBKDF2, BITS_LLAVE);
            byte[] bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return new SecretKeySpec(bytes, "AES");
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo derivar la llave de cifrado", e);
        }
    }

    @SuppressWarnings("deprecation") // Solo para LEER secretos escritos con el formato anterior; se retira al rotarlos.
    private static TextEncryptor descifradorFormatoAnterior(String claveMaestra, String saltHex) {
        return Encryptors.text(claveMaestra, saltHex);
    }
}
