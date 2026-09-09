package com.marcablanca.platform.empresas.application.port.out;

import com.marcablanca.platform.empresas.domain.EmpresaConexion;
import com.marcablanca.platform.empresas.domain.EmpresaConexionDeEmpresa;

import java.util.List;
import java.util.Optional;

public interface RepositorioEmpresaConexiones {
    Optional<EmpresaConexion> buscarConexionActivaPorIdentificador(String identificadorEmpresa);

    // Usado por ResolverEmpresaPorCorreo: no hay directorio correo->empresa,
    // asi que hay que poder recorrer todas las conexiones activas.
    List<EmpresaConexionDeEmpresa> listarConexionesActivas();
}
