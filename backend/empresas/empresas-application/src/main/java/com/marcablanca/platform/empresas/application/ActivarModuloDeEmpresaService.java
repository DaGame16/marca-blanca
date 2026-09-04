package com.marcablanca.platform.empresas.application;

import com.marcablanca.platform.empresas.application.port.in.ActivarModuloDeEmpresa;
import com.marcablanca.platform.empresas.application.port.out.RepositorioModulosDeEmpresa;

import java.util.UUID;

public class ActivarModuloDeEmpresaService implements ActivarModuloDeEmpresa {

    private final RepositorioModulosDeEmpresa repositorioModulosDeEmpresa;

    public ActivarModuloDeEmpresaService(RepositorioModulosDeEmpresa repositorioModulosDeEmpresa) {
        this.repositorioModulosDeEmpresa = repositorioModulosDeEmpresa;
    }

    @Override
    public void ejecutar(UUID empresaId, String codigoModulo) {
        repositorioModulosDeEmpresa.activar(empresaId, codigoModulo);
    }
}
