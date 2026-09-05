package com.marcablanca.platform.modulosempresa.application;

import com.marcablanca.platform.modulosempresa.application.port.in.ListarModulos;
import com.marcablanca.platform.modulosempresa.application.port.out.RepositorioModulos;
import com.marcablanca.platform.modulosempresa.domain.Modulo;

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
