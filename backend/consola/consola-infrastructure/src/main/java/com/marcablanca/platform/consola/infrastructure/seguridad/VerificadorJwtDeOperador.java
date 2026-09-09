package com.marcablanca.platform.consola.infrastructure.seguridad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

/**
 * Valida un token de operador: firma correcta, no expirado y con
 * {@code scope=plataforma}. Un token de tenant (sin ese scope) NO pasa por aca,
 * y un token de operador NO pasa por el filtro de tenant.
 */
@Component
public class VerificadorJwtDeOperador {

    private final SecretKey claveFirma;

    public VerificadorJwtDeOperador(@Value("${app.jwt.secret}") String secreto) {
        this.claveFirma = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
    }

    public Optional<OperadorAutenticado> verificar(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(claveFirma)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (!EmisorJwtDeOperador.SCOPE_PLATAFORMA.equals(claims.get("scope", String.class))) {
                return Optional.empty();
            }
            return Optional.of(new OperadorAutenticado(
                    UUID.fromString(claims.getSubject()),
                    claims.get("rol", String.class),
                    Boolean.TRUE.equals(claims.get("pwd_temp", Boolean.class))));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
