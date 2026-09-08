package com.marcablanca.platform.omnicanal.application.port.out;

import com.marcablanca.platform.omnicanal.domain.ResultadoAnalisisIa;
import com.marcablanca.platform.omnicanal.domain.TurnoParseado;

import java.util.List;

/** Unico puerto que sabe que existe un motor de IA -- hoy implementado contra OpenAI. */
public interface AnalizadorDeConversacion {
    ResultadoAnalisisIa analizar(List<TurnoParseado> turnosRelevantes);
}
