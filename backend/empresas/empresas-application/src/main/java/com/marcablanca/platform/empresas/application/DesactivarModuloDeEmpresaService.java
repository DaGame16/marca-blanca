package com.marcablanca.platform.empresas.application;

import com.marcablanca.platform.empresas.application.port.in.DesactivarModuloDeEmpresa;
import com.marcablanca.platform.empresas.application.port.out.RepositorioModulosDeEmpresa;

import java.util.UUID;

public class DesactivarModuloDeEmpresaService implements DesactivarModuloDeEmpresa {

    private final RepositorioModulosDeEmpresa repositorioModulosDeEmpresa;

    public DesactivarModuloDeEmpresaService(RepositorioModulosDeEmpresa repositorioModulosDeEmpresa) {
        this.repositorioModulosDeEmpresa = repositorioModulosDeEmpresa;
    }

    @Override
    public void ejecutar(UUID empresaId, String codigoModulo) {
        repositorioModulosDeEmpresa.desactivar(empresaId, codigoModulo);
    }
}
