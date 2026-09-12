package com.marcablanca.platform.bootstrap.tiemporeal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

/**
 * Endpoint STOMP /ws para el tiempo real del panel de Omnicanal (Liwa) --
 * reemplaza el polling de 20s del frontend (ver
 * omnicanal-liwa-panel.component.ts). Un solo topic por empresa
 * (/topic/empresa/{id}/omnicanal, publicado por NotificadorEventosOmnicanalStomp
 * en omnicanal-infrastructure); no hay mensajeria cliente->servidor (no se
 * usa el prefijo /app para nada todavia).
 *
 * Autenticacion y multi-tenant: ver AutenticacionHandshakeInterceptor (JWT
 * por query param en el handshake) y AutorizacionSuscripcionInterceptor
 * (una empresa no puede suscribirse al topic de otra). SecurityConfig
 * (autenticacion-infrastructure) deja pasar /ws/** sin JWT por header porque
 * la autenticacion real ocurre en el handshake interceptor, no en el filtro
 * HTTP estandar -- mismo patron que el webhook de Omnicanal con su propio
 * filtro de tenant.
 */
@Configuration
@EnableWebSocketMessageBroker
public class ConfiguracionWebSocket implements WebSocketMessageBrokerConfigurer {

    private final AutenticacionHandshakeInterceptor handshakeInterceptor;
    private final AutorizacionSuscripcionInterceptor autorizacionSuscripcion;
    private final List<String> origenesPermitidos;

    public ConfiguracionWebSocket(AutenticacionHandshakeInterceptor handshakeInterceptor,
            AutorizacionSuscripcionInterceptor autorizacionSuscripcion,
            @Value("${app.cors.origenes-permitidos:http://localhost:4200,http://*.localhost:4200}")
            List<String> origenesPermitidos) {
        this.handshakeInterceptor = handshakeInterceptor;
        this.autorizacionSuscripcion = autorizacionSuscripcion;
        this.origenesPermitidos = origenesPermitidos;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(origenesPermitidos.toArray(new String[0]))
                .addInterceptors(handshakeInterceptor);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(autorizacionSuscripcion);
    }
}
