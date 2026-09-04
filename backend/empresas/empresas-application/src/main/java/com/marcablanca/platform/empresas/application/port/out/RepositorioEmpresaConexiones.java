package com.marcablanca.platform.empresas.application.port.out;

import com.marcablanca.platform.empresas.domain.EmpresaConexion;

import java.util.Optional;

public interface RepositorioEmpresaConexiones {
    Optional<EmpresaConexion> buscarConexionActivaPorIdentificador(String identificadorEmpresa);
}
