package com.marcablanca.platform.identidadvisual.application.port.out;

import com.marcablanca.platform.identidadvisual.domain.MarcaDeEmpresa;

public interface RepositorioMarcaDeEmpresa {
    MarcaDeEmpresa obtener(String identificadorEmpresa);
    void guardar(String identificadorEmpresa, MarcaDeEmpresa marca);
}
