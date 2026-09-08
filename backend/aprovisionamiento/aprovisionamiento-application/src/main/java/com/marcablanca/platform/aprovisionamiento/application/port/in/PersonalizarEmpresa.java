package com.marcablanca.platform.aprovisionamiento.application.port.in;

import com.marcablanca.platform.aprovisionamiento.domain.Personalizacion;

import java.util.UUID;

/** Pasos 3-5 del registro: colores, logo y variantes de UI sobre la empresa en borrador. */
public interface PersonalizarEmpresa {
    void ejecutar(UUID empresaId, Personalizacion personalizacion);
}
