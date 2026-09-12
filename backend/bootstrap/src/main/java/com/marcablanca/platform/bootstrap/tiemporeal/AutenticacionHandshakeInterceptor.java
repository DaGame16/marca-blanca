package com.marcablanca.platform.bootstrap.tiemporeal;

import com.marcablanca.platform.autenticacion.application.port.out.UsuarioAutenticado;
import com.marcablanca.platform.autenticacion.application.port.out.VerificadorDeToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

/**
 * Autentica el handshake de /ws con el mismo JWT que usan los requests HTTP
 * normales (VerificadorDeToken, ver JwtAuthFilter en autenticacion-infrastructure)
 * -- pero via query param, porque un WebSocket nativo del navegador no deja
 * mandar headers custom en el handshake. Sin token valido, corta con 401
 * antes de que se abra el socket.
 *
 * El identificador de empresa queda en los atributos de la sesion WebSocket
 * (no hay ContextoEmpresaActual aca: la conexion vive mucho mas que un
 * request HTTP) para que AutorizacionSuscripcionInterceptor sepa a que
 * topics puede suscribirse esta sesion.
 */
@Component
public class AutenticacionHandshakeInterceptor implements HandshakeInterceptor {

    static final String ATRIBUTO_EMPRESA = "identificadorEmpresa";
    static final String ATRIBUTO_USUARIO = "usuarioId";

    private final VerificadorDeToken verificadorDeToken;

    public AutenticacionHandshakeInterceptor(VerificadorDeToken verificadorDeToken) {
        this.verificadorDeToken = verificadorDeToken;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = extraerToken(request);
        Optional<UsuarioAutenticado> usuario = token == null
                ? Optional.empty()
                : verificadorDeToken.verificar(token);

        if (usuario.isEmpty()) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        attributes.put(ATRIBUTO_EMPRESA, usuario.get().identificadorEmpresa());
        attributes.put(ATRIBUTO_USUARIO, usuario.get().usuarioId());
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception) {
        // No hace falta nada aca -- el resultado ya se decidio en beforeHandshake.
    }

    private static String extraerToken(ServerHttpRequest request) {
        String query = request.getURI().getQuery();
        if (query == null || query.isBlank()) {
            return null;
        }
        for (String par : query.split("&")) {
            int igual = par.indexOf('=');
            if (igual < 0) {
                continue;
            }
            String llave = par.substring(0, igual);
            if ("token".equals(llave)) {
                return URLDecoder.decode(par.substring(igual + 1), StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}
