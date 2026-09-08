package com.marcablanca.platform.aprovisionamiento.application.port.in;

import com.marcablanca.platform.aprovisionamiento.application.ComandoRegistrarEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.ResultadoRegistroEmpresa;

/** Paso 1: registra la empresa como borrador. El aprovisionamiento se dispara despues. */
public interface RegistrarEmpresa {
    ResultadoRegistroEmpresa ejecutar(ComandoRegistrarEmpresa comando);
}
