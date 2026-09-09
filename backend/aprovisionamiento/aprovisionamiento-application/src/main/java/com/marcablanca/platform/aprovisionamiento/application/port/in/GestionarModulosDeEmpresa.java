package com.marcablanca.platform.aprovisionamiento.application.port.in;

import java.util.UUID;

/**
 * Caso de uso operador: activar/desactivar modulos de una empresa ya registrada.
 * Camino paralelo al del wizard ({@code SeleccionarModulo}), que solo permite BORRADOR.
 */
public interface GestionarModulosDeEmpresa {

    void activar(UUID empresaId, String codigoModulo);

    void desactivar(UUID empresaId, String codigoModulo);
}
