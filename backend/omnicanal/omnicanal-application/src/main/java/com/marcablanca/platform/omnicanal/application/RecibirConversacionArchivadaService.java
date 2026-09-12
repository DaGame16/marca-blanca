package com.marcablanca.platform.omnicanal.application;

import com.marcablanca.platform.omnicanal.application.port.in.RecibirConversacionArchivada;
import com.marcablanca.platform.omnicanal.application.port.out.AnalizadorDeConversacion;
import com.marcablanca.platform.omnicanal.application.port.out.NotificadorEventosOmnicanal;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioCasos;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConversaciones;
import com.marcablanca.platform.omnicanal.domain.Caso;

import java.util.Map;

/**
 * Orquesta la ingesta del webhook: primero persiste el bloque completo
 * (conversacion, turnos, casos) de forma ATOMICA vía IngestarConversacionArchivada, y luego
 * dispara el analisis IA de cada caso nuevo -- ese analisis llama a OpenAI, va
 * FUERA de la transaccion, y un caso que falla no tumba la respuesta 200 al
 * webhook (se reintenta despues con GestionarReprocesamiento).
 */
public class RecibirConversacionArchivadaService implements RecibirConversacionArchivada {

    private final IngestarConversacionArchivada ingestar;
    private final AnalizadorDeConversacion analizadorDeConversacion;
    private final RepositorioAnalisisEscritor escritorAnalisis;
    private final RepositorioConversaciones repositorioConversaciones;
    private final RepositorioCasos repositorioCasos;
    private final NotificadorEventosOmnicanal notificador;

    public RecibirConversacionArchivadaService(IngestarConversacionArchivada ingestar,
                                                AnalizadorDeConversacion analizadorDeConversacion,
                                                RepositorioAnalisisEscritor escritorAnalisis,
                                                RepositorioConversaciones repositorioConversaciones,
                                                RepositorioCasos repositorioCasos,
                                                NotificadorEventosOmnicanal notificador) {
        this.ingestar = ingestar;
        this.analizadorDeConversacion = analizadorDeConversacion;
        this.escritorAnalisis = escritorAnalisis;
        this.repositorioConversaciones = repositorioConversaciones;
        this.repositorioCasos = repositorioCasos;
        this.notificador = notificador;
    }

    @Override
    public void ejecutar(Map<String, Object> payload) {
        IngestarConversacionArchivada.Ingesta ingesta = ingestar.ejecutar(payload);

        // La ingesta ya esta comprometida en la base en este punto
        // (IngestarConversacionArchivada.ejecutar() es transaccional, ver
        // IngestarConversacionArchivadaTransaccional) -- avisar ahora mismo
        // (conversacion nueva o solo re-archivada) en vez de esperar a que
        // termine el analisis IA de cada caso, que puede demorar (llama a
        // OpenAI) o fallar y reintentarse despues.
        notificador.notificarCambio();

        for (Caso caso : ingesta.casosNuevos()) {
            try {
                escritorAnalisis.analizarYGuardar(caso, analizadorDeConversacion, repositorioConversaciones,
                        repositorioCasos, ingesta.idContacto());
            } catch (RuntimeException _) {
                // El caso queda con procesada=false -- se reintenta despues via
                // GestionarReprocesamiento. No se propaga: un caso fallido no
                // debe tumbar la respuesta 200 al webhook.
            }
        }
    }
}
