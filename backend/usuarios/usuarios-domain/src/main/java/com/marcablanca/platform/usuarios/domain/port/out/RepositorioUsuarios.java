package com.marcablanca.platform.usuarios.domain.port.out;

import com.marcablanca.platform.usuarios.domain.Correo;
import com.marcablanca.platform.usuarios.domain.Usuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepositorioUsuarios {
    Optional<Usuario> buscarPorCorreo(Correo correo);
    Optional<Usuario> buscarPorUuid(UUID uuid);
    List<Usuario> listarTodos();
    Usuario guardar(Usuario usuario);
}