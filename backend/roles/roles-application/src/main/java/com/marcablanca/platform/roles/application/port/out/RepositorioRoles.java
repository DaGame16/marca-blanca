package com.marcablanca.platform.roles.application.port.out;

import com.marcablanca.platform.roles.domain.Rol;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepositorioRoles {

    Optional<Rol> buscarPorUuid(UUID uuid);

    Optional<Rol> buscarPorNombre(String nombre);

    List<Rol> listarTodos();

    Rol guardar(Rol rol);

    void eliminar(Rol rol);

    long contarUsuariosAsignados(Long rolId);
}
