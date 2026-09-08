package com.marcablanca.platform.omnicanal.application.port.in;

import java.util.Map;

public interface RecibirConversacionArchivada {
    void ejecutar(String webhookSecret, Map<String, Object> payload);
}
