package com.marcablanca.platform.roles.application;

import com.marcablanca.platform.roles.application.port.in.ObtenerPermisosEfectivosDeUsuario;
import com.marcablanca.platform.roles.application.port.out.RepositorioPermisos;
import com.marcablanca.platform.roles.application.port.out.RepositorioPermisosDeUsuario;
import com.marcablanca.platform.roles.application.port.out.RepositorioRolesDeUsuario;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CalcularPermisosEfectivosService implements ObtenerPermisosEfectivosDeUsuario {

    private final RepositorioRolesDeUsuario repositorioRolesDeUsuario;
    private final RepositorioPermisos repositorioPermisos;
    private final RepositorioPermisosDeUsuario repositorioPermisosDeUsuario;

    public CalcularPermisosEfectivosService(RepositorioRolesDeUsuario repositorioRolesDeUsuario,
                                             RepositorioPermisos repositorioPermisos,
                                             RepositorioPermisosDeUsuario repositorioPermisosDeUsuario) {
        this.repositorioRolesDeUsuario = repositorioRolesDeUsuario;
        this.repositorioPermisos = repositorioPermisos;
        this.repositorioPermisosDeUsuario = repositorioPermisosDeUsuario;
    }

    @Override
    public Set<String> ejecutar(UUID usuarioUuid) {
        Set<Long> rolIds = repositorioRolesDeUsuario.listarRolIdsDe(usuarioUuid);
        Set<String> permisos = new HashSet<>(repositorioPermisos.listarNombresDeRoles(rolIds));

        Map<String, Boolean> ajustes = repositorioPermisosDeUsuario.listarAjustesDe(usuarioUuid);
        ajustes.forEach((nombre, concedido) -> {
            if (Boolean.TRUE.equals(concedido)) {
                permisos.add(nombre);
            } else {
                permisos.remove(nombre);
            }
        });

        return permisos;
    }
}
