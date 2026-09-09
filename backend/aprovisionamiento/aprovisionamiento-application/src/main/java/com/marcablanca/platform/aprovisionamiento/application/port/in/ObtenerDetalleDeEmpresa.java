package com.marcablanca.platform.aprovisionamiento.application.port.in;

import com.marcablanca.platform.aprovisionamiento.application.DetalleDeEmpresa;

import java.util.Optional;
import java.util.UUID;

/** Caso de uso: datos + marca + modulos de una empresa, para la pantalla de edicion. */
public interface ObtenerDetalleDeEmpresa {

    Optional<DetalleDeEmpresa> ejecutar(UUID empresaId);
}
