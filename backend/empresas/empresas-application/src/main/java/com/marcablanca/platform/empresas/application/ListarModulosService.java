package com.marcablanca.platform.empresas.application;

import com.marcablanca.platform.empresas.application.port.in.ListarModulos;
import com.marcablanca.platform.empresas.application.port.out.RepositorioModulos;
import com.marcablanca.platform.empresas.domain.Modulo;

import java.util.List;

public class ListarModulosService implements ListarModulos {

    private final RepositorioModulos repositorioModulos;

    public ListarModulosService(RepositorioModulos repositorioModulos) {
        this.repositorioModulos = repositorioModulos;
    }

    @Override
    public List<Modulo> ejecutar() {
        return repositorioModulos.listarTodos();
    }
}
