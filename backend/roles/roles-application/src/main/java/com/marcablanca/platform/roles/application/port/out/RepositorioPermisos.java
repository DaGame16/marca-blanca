package com.marcablanca.platform.roles.application.port.out;

import com.marcablanca.platform.roles.domain.Permiso;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface RepositorioPermisos {

    List<Permiso> listarTodos();

    Optional<Permiso> buscarPorUuid(UUID uuid);

    List<Permiso> listarDeRol(Long rolId);

    Set<String> listarNombresDeRoles(Set<Long> rolIds);

    void asignarARol(Long rolId, Long permisoId);

    void quitarDeRol(Long rolId, Long permisoId);
}
