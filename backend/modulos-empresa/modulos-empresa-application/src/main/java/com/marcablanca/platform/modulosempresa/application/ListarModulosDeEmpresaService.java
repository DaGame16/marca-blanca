package com.marcablanca.platform.modulosempresa.application;

import com.marcablanca.platform.modulosempresa.application.port.in.ListarModulosDeEmpresa;
import com.marcablanca.platform.modulosempresa.application.port.out.RepositorioModulosDeEmpresa;
import com.marcablanca.platform.modulosempresa.domain.ModuloDeEmpresa;

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
