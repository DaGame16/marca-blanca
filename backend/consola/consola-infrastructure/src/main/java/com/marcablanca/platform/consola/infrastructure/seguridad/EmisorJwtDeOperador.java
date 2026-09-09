package com.marcablanca.platform.consola.infrastructure.seguridad;

import com.marcablanca.platform.consola.application.port.out.EmisorDeTokenDeOperador;
import com.marcablanca.platform.consola.domain.Operador;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Token de sesion de un operador: JWT firmado con el mismo secreto de la app
 * ({@code app.jwt.secret}) pero con forma distinta a la del token de tenant
 * -- lleva {@code scope=plataforma} y {@code rol}, y NO lleva {@code empresa}.
 * Vida corta por defecto (15 min).
 */
@Component
public class EmisorJwtDeOperador implements EmisorDeTokenDeOperador {

    public static final String SCOPE_PLATAFORMA = "plataforma";

    private final SecretKey claveFirma;
    private final long minutosExpiracion;

    public EmisorJwtDeOperador(
            @Value("${app.jwt.secret}") String secreto,
            @Value("${app.consola.jwt.expiracion-minutos:15}") long minutosExpiracion) {
        this.claveFirma = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.minutosExpiracion = minutosExpiracion;
    }

    @Override
    public String emitirPara(Operador operador) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(operador.getId().toString())
                .claim("scope", SCOPE_PLATAFORMA)
                .claim("correo", operador.getCorreo())
                .claim("rol", operador.getRol().name())
                .claim("pwd_temp", operador.debeCambiarContrasena())
                .issuedAt(Date.from(ahora)) //NOSONAR jjwt 0.12.x solo acepta java.util.Date en su API
                .expiration(Date.from(ahora.plus(minutosExpiracion, ChronoUnit.MINUTES))) //NOSONAR idem
                .signWith(claveFirma)
                .compact();
    }
}
