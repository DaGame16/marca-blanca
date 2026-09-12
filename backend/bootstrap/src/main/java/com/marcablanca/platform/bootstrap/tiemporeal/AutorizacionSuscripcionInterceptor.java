package com.marcablanca.platform.bootstrap.tiemporeal;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Corta cualquier intento de suscripcion a /topic/empresa/{id}/... donde
 * {id} no coincida con la empresa del token que autentico el handshake --
 * sin esto, una empresa podria escuchar los eventos de otra con solo
 * adivinar/probar el identificador en la URL del topic (fuga de datos entre
 * tenants, inaceptable en un sistema marca blanca).
 */
@Component
public class AutorizacionSuscripcionInterceptor implements ChannelInterceptor {

    private static final Pattern TOPIC_DE_EMPRESA = Pattern.compile("^/topic/empresa/([^/]+)/.*$");

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (accessor.getCommand() != StompCommand.SUBSCRIBE) {
            return message;
        }

        String destino = accessor.getDestination();
        Matcher m = destino == null ? null : TOPIC_DE_EMPRESA.matcher(destino);
        if (m == null || !m.matches()) {
            throw new AccessDeniedException("Destino de suscripcion invalido: " + destino);
        }

        Map<String, Object> atributosSesion = accessor.getSessionAttributes();
        Object empresaDelToken = atributosSesion == null
                ? null
                : atributosSesion.get(AutenticacionHandshakeInterceptor.ATRIBUTO_EMPRESA);

        if (!m.group(1).equals(empresaDelToken)) {
            throw new AccessDeniedException("No autorizado a suscribirse a " + destino);
        }
        return message;
    }
}
