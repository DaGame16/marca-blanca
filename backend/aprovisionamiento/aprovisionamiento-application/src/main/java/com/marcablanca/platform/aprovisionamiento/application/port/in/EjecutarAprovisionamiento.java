package com.marcablanca.platform.aprovisionamiento.application.port.in;

import com.marcablanca.platform.aprovisionamiento.domain.EmpresaRegistrada;

/** Capa 2: ejecuta (o reanuda) el pipeline de aprovisionamiento para una empresa registrada. */
public interface EjecutarAprovisionamiento {
    void ejecutar(EmpresaRegistrada evento);
}