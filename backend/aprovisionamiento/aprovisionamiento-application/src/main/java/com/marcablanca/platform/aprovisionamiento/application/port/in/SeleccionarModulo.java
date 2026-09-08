package com.marcablanca.platform.aprovisionamiento.application.port.in;

import java.util.UUID;

/** Paso 2 del registro: el prospecto marca/desmarca un modulo sobre su empresa en borrador. */
public interface SeleccionarModulo {
    void activar(UUID empresaId, String codigoModulo);
    void desactivar(UUID empresaId, String codigoModulo);
}