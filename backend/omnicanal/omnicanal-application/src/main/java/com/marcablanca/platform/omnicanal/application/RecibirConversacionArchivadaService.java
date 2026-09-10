package com.marcablanca.platform.omnicanal.application;

import com.marcablanca.platform.omnicanal.application.port.in.RecibirConversacionArchivada;
import com.marcablanca.platform.omnicanal.application.port.out.AnalizadorDeConversacion;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioCasos;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConversaciones;
import com.marcablanca.platform.omnicanal.domain.Caso;

import java.util.Map;

/**
 * Orquesta la ingesta del webhook: primero persiste todo (conversacion,
 * turnos, casos) de forma ATOMICA vía IngestarConversacionArchivada, y luego
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

    public RecibirConversacionArchivadaService(IngestarConversacionArchivada ingestar,
                                                AnalizadorDeConversacion analizadorDeConversacion,
                                                RepositorioAnalisisEscritor escritorAnalisis,
                                                RepositorioConversaciones repositorioConversaciones,
                                                RepositorioCasos repositorioCasos) {
        this.ingestar = ingestar;
        this.analizadorDeConversacion = analizadorDeConversacion;
        this.escritorAnalisis = escritorAnalisis;
        this.repositorioConversaciones = repositorioConversaciones;
        this.repositorioCasos = repositorioCasos;
    }

    @Override
    public void ejecutar(Map<String, Object> payload) {
        IngestarConversacionArchivada.Ingesta ingesta = ingestar.ejecutar(payload);

        for (Caso caso : ingesta.casosNuevos()) {
            try {
                escritorAnalisis.analizarYGuardar(caso, analizadorDeConversacion, repositorioConversaciones,
                        repositorioCasos, ingesta.idContacto());
            } catch (Exception ignored) {
                // El caso queda con procesada=false -- se reintenta despues via
                // GestionarReprocesamiento. No se propaga.
            }
        }
    }
}
