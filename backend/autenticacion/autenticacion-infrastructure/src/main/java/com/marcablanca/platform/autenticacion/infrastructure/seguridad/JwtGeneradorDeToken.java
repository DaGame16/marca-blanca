package com.marcablanca.platform.autenticacion.infrastructure.seguridad;

import com.marcablanca.platform.autenticacion.application.port.out.GeneradorDeToken;
import com.marcablanca.platform.usuarios.domain.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtGeneradorDeToken implements GeneradorDeToken {

    private final SecretKey claveFirma;
    private final long minutosExpiracion;

    public JwtGeneradorDeToken(
            @Value("${app.jwt.secret}") String secreto,
            @Value("${app.jwt.expiracion-minutos:15}") long minutosExpiracion) {
        this.claveFirma = Keys.hmacShaKeyFor(secreto.getBytes());
        this.minutosExpiracion = minutosExpiracion;
    }

    @Override
    public String generarPara(Usuario usuario, String identificadorEmpresa) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getId().toString())
                .claim("correo", usuario.getCorreo().valor())
                .claim("empresa", identificadorEmpresa)
                .issuedAt(Date.from(ahora)) //NOSONAR jjwt 0.12.6 solo acepta java.util.Date en su API
                .expiration(Date.from(ahora.plus(minutosExpiracion, ChronoUnit.MINUTES))) //NOSONAR idem
                .signWith(claveFirma)
                .compact();
    }
}
