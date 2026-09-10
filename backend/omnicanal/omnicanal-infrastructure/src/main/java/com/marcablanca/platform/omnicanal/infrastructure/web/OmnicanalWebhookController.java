package com.marcablanca.platform.omnicanal.infrastructure.web;

import com.marcablanca.platform.omnicanal.application.port.in.RecibirConversacionArchivada;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Publico -- sin JWT. LIWA no tiene sesion ni login: el header del secreto
 * (x-liwa-webhook-secret) ES el dato que identifica la empresa. Quien lo lee
 * y resuelve el tenant es FiltroTenantOmnicanalWebhook, que corre antes de
 * este controlador y deja la empresa en ContextoEmpresaActual (o corta con
 * 401 si el secreto no corresponde a ninguna). Permitido explicito en
 * SecurityConfig ("/api/v1/omnicanal/webhook/**").
 */
@RestController
@RequestMapping("/api/v1/omnicanal/webhook")
public class OmnicanalWebhookController {

    private final RecibirConversacionArchivada recibirConversacionArchivada;

    public OmnicanalWebhookController(RecibirConversacionArchivada recibirConversacionArchivada) {
        this.recibirConversacionArchivada = recibirConversacionArchivada;
    }

    @PostMapping("/chat-history")
    public ResponseEntity<Map<String, Boolean>> recibir(@RequestBody Map<String, Object> payload) {
        recibirConversacionArchivada.ejecutar(payload);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("ok", true));
    }
}
