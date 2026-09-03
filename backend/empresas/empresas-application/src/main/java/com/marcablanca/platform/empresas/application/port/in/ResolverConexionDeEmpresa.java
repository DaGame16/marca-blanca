package com.marcablanca.platform.empresas.application.port.in;

import com.marcablanca.platform.empresas.domain.EmpresaConexion;

public interface ResolverConexionDeEmpresa {
    EmpresaConexion ejecutar(String identificadorEmpresa);
}
