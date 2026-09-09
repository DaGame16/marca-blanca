package com.marcablanca.platform.consola.infrastructure.seguridad;

import com.marcablanca.platform.consola.application.port.out.CifradorDeContrasenaDeOperador;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CifradorBCryptDeOperador implements CifradorDeContrasenaDeOperador {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String cifrar(String contrasenaPlana) {
        return encoder.encode(contrasenaPlana);
    }

    @Override
    public boolean coincide(String contrasenaPlana, String hash) {
        if (hash == null || hash.isBlank()) {
            return false;
        }
        try {
            return encoder.matches(contrasenaPlana, hash);
        } catch (IllegalArgumentException e) {
            // hash con formato no-BCrypt (p. ej. el sentinela 'PENDIENTE' antes
            // de que SembradorDeOperadorInicial fije la contrasena real).
            return false;
        }
    }
}
