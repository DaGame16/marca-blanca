package com.marcablanca.platform.omnicanal.infrastructure;

import com.marcablanca.platform.omnicanal.application.IngestarConversacionArchivada;
import com.marcablanca.platform.omnicanal.application.IngestarConversacionArchivadaService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Decorador delgado que hace atomicas las escrituras de la ingesta
 * (conversacion + turnos + casos) contra la base del cliente. Mismo patron
 * que los *Transaccional de aprovisionamiento; usa clienteTransactionManager
 * porque esas tablas viven en la base de cada empresa, no en la de control.
 */
class IngestarConversacionArchivadaTransaccional implements IngestarConversacionArchivada {

    private final IngestarConversacionArchivadaService delegado;

    IngestarConversacionArchivadaTransaccional(IngestarConversacionArchivadaService delegado) {
        this.delegado = delegado;
    }

    @Override
    @Transactional(transactionManager = "clienteTransactionManager")
    public Ingesta ejecutar(Map<String, Object> payload) {
        return delegado.ejecutar(payload);
    }
}
