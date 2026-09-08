package com.marcablanca.platform.aprovisionamiento.application.port.in;

import com.marcablanca.platform.aprovisionamiento.application.ResultadoFinalizarRegistro;

import java.util.UUID;

/** Paso 6: termina el wizard y dispara el aprovisionamiento. */
public interface FinalizarRegistro {
    ResultadoFinalizarRegistro ejecutar(UUID empresaId);
}
