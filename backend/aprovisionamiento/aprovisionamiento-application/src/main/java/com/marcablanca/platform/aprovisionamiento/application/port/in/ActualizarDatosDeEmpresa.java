package com.marcablanca.platform.aprovisionamiento.application.port.in;

import com.marcablanca.platform.aprovisionamiento.application.DatosDeEmpresa;

import java.util.UUID;

/** Caso de uso operador: editar los datos de contacto de una empresa ya registrada. */
public interface ActualizarDatosDeEmpresa {

    void ejecutar(UUID empresaId, DatosDeEmpresa datos);
}
