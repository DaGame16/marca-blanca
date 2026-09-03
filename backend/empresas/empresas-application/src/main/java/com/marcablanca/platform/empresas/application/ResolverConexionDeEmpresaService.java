package com.marcablanca.platform.empresas.application;

import com.marcablanca.platform.empresas.application.port.in.ResolverConexionDeEmpresa;
import com.marcablanca.platform.empresas.application.port.out.RepositorioEmpresaConexiones;
import com.marcablanca.platform.empresas.domain.EmpresaConexion;
import com.marcablanca.platform.empresas.domain.EmpresaSinConexionActivaException;

public class ResolverConexionDeEmpresaService implements ResolverConexionDeEmpresa {

    private final RepositorioEmpresaConexiones repositorioEmpresaConexiones;

    public ResolverConexionDeEmpresaService(RepositorioEmpresaConexiones repositorioEmpresaConexiones) {
        this.repositorioEmpresaConexiones = repositorioEmpresaConexiones;
    }

    @Override
    public EmpresaConexion ejecutar(String identificadorEmpresa) {
        return repositorioEmpresaConexiones.buscarConexionActivaPorIdentificador(identificadorEmpresa)
                .orElseThrow(() -> new EmpresaSinConexionActivaException(identificadorEmpresa));
    }
}
