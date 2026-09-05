package com.marcablanca.platform.modulosempresa.application;

import com.marcablanca.platform.modulosempresa.application.port.in.ActivarModuloDeEmpresa;
import com.marcablanca.platform.modulosempresa.application.port.out.RepositorioModulosDeEmpresa;

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
