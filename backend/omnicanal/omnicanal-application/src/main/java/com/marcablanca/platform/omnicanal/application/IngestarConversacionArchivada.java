package com.marcablanca.platform.omnicanal.application;

import com.marcablanca.platform.omnicanal.domain.Caso;

import java.util.List;
import java.util.Map;

/**
 * Colaborador interno de RecibirConversacionArchivadaService: hace TODAS las
 * escrituras a base de la ingesta (conversacion + turnos + casos) y devuelve
 * los casos nuevos para que el orquestador dispare su analisis IA aparte,
 * FUERA de la transaccion (el analisis llama a OpenAI y no debe tener una
 * conexion de base tomada mientras espera).
 *
 * Se separo para poder envolver solo esta parte en @Transactional (decorador
 * en -infrastructure) sin meter la llamada a OpenAI adentro de la tx.
 */
public interface IngestarConversacionArchivada {

    /** @param idContacto  el user_id del payload; @param casosNuevos  casos a analizar. */
    record Ingesta(String idContacto, List<Caso> casosNuevos) {
        public Ingesta {
            casosNuevos = List.copyOf(casosNuevos);
        }
    }

    Ingesta ejecutar(Map<String, Object> payload);
}
