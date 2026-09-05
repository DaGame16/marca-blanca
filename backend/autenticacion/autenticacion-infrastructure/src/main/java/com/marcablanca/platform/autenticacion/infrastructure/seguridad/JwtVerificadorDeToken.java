package com.marcablanca.platform.autenticacion.infrastructure.seguridad;

import com.marcablanca.platform.autenticacion.application.port.out.UsuarioAutenticado;
import com.marcablanca.platform.autenticacion.application.port.out.VerificadorDeToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtVerificadorDeToken implements VerificadorDeToken {

    private final SecretKey claveFirma;

    public JwtVerificadorDeToken(@Value("${app.jwt.secret}") String secreto) {
        this.claveFirma = Keys.hmacShaKeyFor(secreto.getBytes());
    }

    @Override
    public Optional<UsuarioAutenticado> verificar(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(claveFirma)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            UUID usuarioId = UUID.fromString(claims.getSubject());
            String identificadorEmpresa = claims.get("empresa", String.class);
            return Optional.of(new UsuarioAutenticado(usuarioId, identificadorEmpresa));
        } catch (JwtException | IllegalArgumentException _) {
            // Firma invalida, token vencido, o formato incorrecto - todos tratados igual: no autenticado.
            return Optional.empty();
        }
    }
}
