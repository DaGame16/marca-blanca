package com.marcablanca.platform.omnicanal.application.port.out;

import com.marcablanca.platform.omnicanal.domain.ResultadoAnalisisIa;
import com.marcablanca.platform.omnicanal.domain.TurnoParseado;

import java.util.List;

/** Unico puerto que sabe que existe un motor de IA -- hoy implementado contra OpenAI. */
public interface AnalizadorDeConversacion {

    /**
     * @param resultado    lo que dijo la IA.
     * @param modeloUsado  el modelo que efectivamente respondio (para auditoria);
     *                     lo sabe el adaptador, no se re-adivina aguas abajo.
     */
    record AnalisisDeIa(ResultadoAnalisisIa resultado, String modeloUsado) {
    }

    AnalisisDeIa analizar(List<TurnoParseado> turnosRelevantes);
}
