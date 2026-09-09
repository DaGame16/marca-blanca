package com.marcablanca.platform.aprovisionamiento.application.port.in;

import java.util.UUID;

/**
 * Caso de uso: transiciones operativas del ciclo de vida de una empresa que
 * dispara la consola de operacion. Cada transicion valida el estado de origen en
 * el agregado {@code Empresa}.
 */
public interface CambiarEstadoDeEmpresa {

    void ejecutar(UUID empresaId, Transicion transicion);

    enum Transicion {
        SUSPENDER,
        REACTIVAR
    }
}
