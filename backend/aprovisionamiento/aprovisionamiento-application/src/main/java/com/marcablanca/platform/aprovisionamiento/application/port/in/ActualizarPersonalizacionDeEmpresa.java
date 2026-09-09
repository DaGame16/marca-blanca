package com.marcablanca.platform.aprovisionamiento.application.port.in;

import com.marcablanca.platform.aprovisionamiento.domain.Personalizacion;

import java.util.UUID;

/**
 * Caso de uso operador: editar colores, logo y variantes de UI de una empresa ya
 * registrada. Camino paralelo al del wizard ({@code PersonalizarEmpresa}), que
 * solo permite BORRADOR.
 */
public interface ActualizarPersonalizacionDeEmpresa {

    void ejecutar(UUID empresaId, Personalizacion personalizacion);
}
