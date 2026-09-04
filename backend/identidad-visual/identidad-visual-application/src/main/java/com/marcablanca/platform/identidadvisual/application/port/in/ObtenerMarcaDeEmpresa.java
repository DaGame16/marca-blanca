package com.marcablanca.platform.identidadvisual.application.port.in;

import com.marcablanca.platform.identidadvisual.domain.MarcaDeEmpresa;

public interface ObtenerMarcaDeEmpresa {
    MarcaDeEmpresa ejecutar(String identificadorEmpresa);
}
