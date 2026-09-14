package com.marcablanca.platform.roles.application;

import com.marcablanca.platform.roles.application.port.in.GestionarAsignacionesDeUsuario;
import com.marcablanca.platform.roles.application.port.out.RepositorioPermisos;
import com.marcablanca.platform.roles.application.port.out.RepositorioPermisosDeUsuario;
import com.marcablanca.platform.roles.application.port.out.RepositorioRoles;
import com.marcablanca.platform.roles.application.port.out.RepositorioRolesDeUsuario;
import com.marcablanca.platform.roles.domain.Permiso;
import com.marcablanca.platform.roles.domain.PermisoNoEncontradoException;
import com.marcablanca.platform.roles.domain.Rol;
import com.marcablanca.platform.roles.domain.RolNoEncontradoException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GestionarAsignacionesDeUsuarioService implements GestionarAsignacionesDeUsuario {

    private final RepositorioRolesDeUsuario repositorioRolesDeUsuario;
    private final RepositorioPermisosDeUsuario repositorioPermisosDeUsuario;
    private final RepositorioRoles repositorioRoles;
    private final RepositorioPermisos repositorioPermisos;

    public GestionarAsignacionesDeUsuarioService(RepositorioRolesDeUsuario repositorioRolesDeUsuario,
                                                   RepositorioPermisosDeUsuario repositorioPermisosDeUsuario,
                                                   RepositorioRoles repositorioRoles,
                                                   RepositorioPermisos repositorioPermisos) {
        this.repositorioRolesDeUsuario = repositorioRolesDeUsuario;
        this.repositorioPermisosDeUsuario = repositorioPermisosDeUsuario;
        this.repositorioRoles = repositorioRoles;
        this.repositorioPermisos = repositorioPermisos;
    }

    @Override
    public void asignarRol(UUID usuarioUuid, UUID rolUuid) {
        Rol rol = obtenerRolOLanzar(rolUuid);
        repositorioRolesDeUsuario.asignar(usuarioUuid, rol.getId());
    }

    @Override
    public void quitarRol(UUID usuarioUuid, UUID rolUuid) {
        Rol rol = obtenerRolOLanzar(rolUuid);
        repositorioRolesDeUsuario.quitar(usuarioUuid, rol.getId());
    }

    @Override
    public List<Rol> listarRolesDeUsuario(UUID usuarioUuid) {
        return repositorioRolesDeUsuario.listarRolesDe(usuarioUuid);
    }

    @Override
    public void concederPermiso(UUID usuarioUuid, UUID permisoUuid) {
        Permiso permiso = obtenerPermisoOLanzar(permisoUuid);
        repositorioPermisosDeUsuario.registrarAjuste(usuarioUuid, permiso.getId(), true);
    }

    @Override
    public void revocarPermiso(UUID usuarioUuid, UUID permisoUuid) {
        Permiso permiso = obtenerPermisoOLanzar(permisoUuid);
        repositorioPermisosDeUsuario.registrarAjuste(usuarioUuid, permiso.getId(), false);
    }

    @Override
    public void quitarAjustePermiso(UUID usuarioUuid, UUID permisoUuid) {
        Permiso permiso = obtenerPermisoOLanzar(permisoUuid);
        repositorioPermisosDeUsuario.eliminarAjuste(usuarioUuid, permiso.getId());
    }

    @Override
    public Map<String, Boolean> listarAjustesDeUsuario(UUID usuarioUuid) {
        return repositorioPermisosDeUsuario.listarAjustesDe(usuarioUuid);
    }

    private Rol obtenerRolOLanzar(UUID uuid) {
        return repositorioRoles.buscarPorUuid(uuid).orElseThrow(() -> new RolNoEncontradoException(uuid));
    }

    private Permiso obtenerPermisoOLanzar(UUID uuid) {
        return repositorioPermisos.buscarPorUuid(uuid).orElseThrow(() -> new PermisoNoEncontradoException(uuid));
    }
}
