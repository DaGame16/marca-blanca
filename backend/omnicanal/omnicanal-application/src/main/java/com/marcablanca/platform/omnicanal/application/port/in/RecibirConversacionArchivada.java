package com.marcablanca.platform.omnicanal.application.port.in;

import java.util.Map;

/**
 * Ingesta de una conversacion archivada de LIWA. La empresa (tenant) ya
 * quedo resuelta y puesta en ContextoEmpresaActual por el filtro del
 * webhook (FiltroTenantOmnicanalWebhook) antes de llegar aca -- este puerto
 * solo recibe el payload crudo.
 */
public interface RecibirConversacionArchivada {
    void ejecutar(Map<String, Object> payload);
}
