package com.marcablanca.platform.omnicanal.domain;

public class WebhookSecretoInvalidoException extends RuntimeException {
    public WebhookSecretoInvalidoException() {
        super("Secreto de webhook invalido o empresa no configurada para Omnicanal.");
    }
}
