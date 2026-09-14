package com.marcablanca.platform.roles.application;

import com.marcablanca.platform.roles.application.port.in.GestionarRoles;
import com.marcablanca.platform.roles.application.port.out.RepositorioPermisos;
import com.marcablanca.platform.roles.application.port.out.RepositorioRoles;
import com.marcablanca.platform.roles.domain.Permiso;
import com.marcablanca.platform.roles.domain.PermisoNoEncontradoException;
import com.marcablanca.platform.roles.domain.Rol;
import com.marcablanca.platform.roles.domain.RolConUsuariosAsignadosException;
import com.marcablanca.platform.roles.domain.RolNoEncontradoException;
import com.marcablanca.platform.roles.domain.RolYaExisteException;

import java.util.List;
import java.util.UUID;

public class GestionarRolesService implements GestionarRoles {

    private final RepositorioRoles repositorioRoles;
    private final RepositorioPermisos repositorioPermisos;

    public GestionarRolesService(RepositorioRoles repositorioRoles, RepositorioPermisos repositorioPermisos) {
        this.repositorioRoles = repositorioRoles;
        this.repositorioPermisos = repositorioPermisos;
    }

    @Override
    public Rol crear(String nombre, String descripcion) {
        if (repositorioRoles.buscarPorNombre(nombre).isPresent()) {
            throw new RolYaExisteException(nombre);
        }
        return repositorioRoles.guardar(Rol.nuevo(nombre, descripcion));
    }

    @Override
    public Rol actualizar(UUID uuid, String nombre, String descripcion) {
        Rol rol = obtenerOLanzar(uuid);
        rol.actualizar(nombre, descripcion);
        return repositorioRoles.guardar(rol);
    }

    @Override
    public void eliminar(UUID uuid) {
        Rol rol = obtenerOLanzar(uuid);
        rol.verificarEliminable();
        if (repositorioRoles.contarUsuariosAsignados(rol.getId()) > 0) {
            throw new RolConUsuariosAsignadosException(rol.getNombre());
        }
        repositorioRoles.eliminar(rol);
    }

    @Override
    public Rol consultarPorUuid(UUID uuid) {
        return obtenerOLanzar(uuid);
    }

    @Override
    public List<Rol> listar() {
        return repositorioRoles.listarTodos();
    }

    @Override
    public void asignarPermiso(UUID rolUuid, UUID permisoUuid) {
        Rol rol = obtenerOLanzar(rolUuid);
        Permiso permiso = obtenerPermisoOLanzar(permisoUuid);
        repositorioPermisos.asignarARol(rol.getId(), permiso.getId());
    }

    @Override
    public void quitarPermiso(UUID rolUuid, UUID permisoUuid) {
        Rol rol = obtenerOLanzar(rolUuid);
        Permiso permiso = obtenerPermisoOLanzar(permisoUuid);
        repositorioPermisos.quitarDeRol(rol.getId(), permiso.getId());
    }

    @Override
    public List<Permiso> listarPermisosDeRol(UUID rolUuid) {
        Rol rol = obtenerOLanzar(rolUuid);
        return repositorioPermisos.listarDeRol(rol.getId());
    }

    private Rol obtenerOLanzar(UUID uuid) {
        return repositorioRoles.buscarPorUuid(uuid).orElseThrow(() -> new RolNoEncontradoException(uuid));
    }

    private Permiso obtenerPermisoOLanzar(UUID uuid) {
        return repositorioPermisos.buscarPorUuid(uuid).orElseThrow(() -> new PermisoNoEncontradoException(uuid));
    }
}
