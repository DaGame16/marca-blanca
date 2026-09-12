package com.marcablanca.platform.omnicanal.infrastructure.realtime;

import com.marcablanca.platform.empresas.application.ContextoEmpresaActual;
import com.marcablanca.platform.omnicanal.application.port.out.NotificadorEventosOmnicanal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;

/**
 * Adaptador STOMP del puerto NotificadorEventosOmnicanal. Publica en
 * /topic/empresa/{identificadorEmpresa}/omnicanal -- el ChannelInterceptor de
 * bootstrap (ver ConfiguracionWebSocket) es quien garantiza que un cliente
 * solo pueda suscribirse al topic de SU propia empresa.
 *
 * Lee ContextoEmpresaActual aca (no en application, ver
 * ArquitecturaHexagonalTest.omnicanal_dominio_y_aplicacion_no_dependen_de_otros_contextos)
 * porque este metodo siempre corre en el mismo hilo del request que lo
 * establecio (el filtro del webhook, o el JwtAuthFilter para el reproceso
 * manual) -- nunca de forma asincrona fuera de esa peticion.
 */
public class NotificadorEventosOmnicanalStomp implements NotificadorEventosOmnicanal {

    private static final Logger log = LoggerFactory.getLogger(NotificadorEventosOmnicanalStomp.class);

    private final SimpMessagingTemplate mensajeria;

    public NotificadorEventosOmnicanalStomp(SimpMessagingTemplate mensajeria) {
        this.mensajeria = mensajeria;
    }

    @Override
    public void notificarCambio() {
        ContextoEmpresaActual.obtener().ifPresentOrElse(
                empresaId -> {
                    Object payload = Map.of("tipo", "omnicanal:cambio");
                    mensajeria.convertAndSend("/topic/empresa/" + empresaId + "/omnicanal", payload);
                },
                () -> log.warn("notificarCambio() sin empresa activa en ContextoEmpresaActual -- no se publico ningun evento"));
    }
}
