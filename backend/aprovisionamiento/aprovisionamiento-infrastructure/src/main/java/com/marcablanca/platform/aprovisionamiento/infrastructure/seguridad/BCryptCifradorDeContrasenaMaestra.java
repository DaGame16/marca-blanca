package com.marcablanca.platform.aprovisionamiento.infrastructure.seguridad;

import com.marcablanca.platform.aprovisionamiento.application.port.out.CifradorDeContrasenaMaestra;
import com.marcablanca.platform.aprovisionamiento.domain.HashContrasenaMaestra;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class BCryptCifradorDeContrasenaMaestra implements CifradorDeContrasenaMaestra {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public HashContrasenaMaestra cifrar(String contrasenaEnClaro) {
        if (contrasenaEnClaro == null || contrasenaEnClaro.isBlank()) {
            throw new IllegalArgumentException("La contrasena maestra no puede estar vacia.");
        }
        return new HashContrasenaMaestra(encoder.encode(contrasenaEnClaro));
    }
}