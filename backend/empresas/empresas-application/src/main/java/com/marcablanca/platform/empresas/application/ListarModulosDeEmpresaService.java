package com.marcablanca.platform.empresas.application;

import com.marcablanca.platform.empresas.application.port.in.ListarModulosDeEmpresa;
import com.marcablanca.platform.empresas.application.port.out.RepositorioModulosDeEmpresa;
import com.marcablanca.platform.empresas.domain.ModuloDeEmpresa;

import java.util.List;
import java.util.UUID;

public class ListarModulosDeEmpresaService implements ListarModulosDeEmpresa {

    private final RepositorioModulosDeEmpresa repositorioModulosDeEmpresa;

    public ListarModulosDeEmpresaService(RepositorioModulosDeEmpresa repositorioModulosDeEmpresa) {
        this.repositorioModulosDeEmpresa = repositorioModulosDeEmpresa;
    }

    @Override
    public List<ModuloDeEmpresa> ejecutar(UUID empresaId) {
        return repositorioModulosDeEmpresa.listarPorEmpresa(empresaId);
    }
}
