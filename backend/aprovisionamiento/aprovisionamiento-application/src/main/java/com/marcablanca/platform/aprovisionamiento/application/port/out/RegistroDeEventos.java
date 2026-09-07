package com.marcablanca.platform.aprovisionamiento.application.port.out;

import com.marcablanca.platform.aprovisionamiento.domain.EventoDeDominio;

import java.util.List;

/**
 * Outbox transaccional. La implementacion escribe en tbl_eventos_salientes
 * dentro de la MISMA transaccion en la que se guardo la empresa.
 */
public interface RegistroDeEventos {
    void publicar(List<EventoDeDominio> eventos);
}