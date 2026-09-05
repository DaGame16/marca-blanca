package com.marcablanca.platform.identidadvisual.application.port.in;

import com.marcablanca.platform.identidadvisual.domain.MarcaDeEmpresa;

public interface ActualizarMarcaDeEmpresa {
    void ejecutar(String identificadorEmpresa, MarcaDeEmpresa marca);
}
