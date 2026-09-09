package com.marcablanca.platform.consola.application.port.out;

import java.util.UUID;

/** Deja rastro de cada accion de operador en plataforma.tbl_auditoria_consola. */
public interface RegistroDeAuditoria {

    void registrar(UUID operadorId, String accion, UUID empresaUuid);
}
