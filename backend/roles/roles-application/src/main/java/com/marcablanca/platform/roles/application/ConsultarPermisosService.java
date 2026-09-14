package com.marcablanca.platform.roles.application;

import com.marcablanca.platform.roles.application.port.in.ConsultarPermisos;
import com.marcablanca.platform.roles.application.port.out.RepositorioPermisos;
import com.marcablanca.platform.roles.domain.Permiso;

import java.util.List;

public class ConsultarPermisosService implements ConsultarPermisos {

    private final RepositorioPermisos repositorioPermisos;

    public ConsultarPermisosService(RepositorioPermisos repositorioPermisos) {
        this.repositorioPermisos = repositorioPermisos;
    }

    @Override
    public List<Permiso> listar() {
        return repositorioPermisos.listarTodos();
    }
}
