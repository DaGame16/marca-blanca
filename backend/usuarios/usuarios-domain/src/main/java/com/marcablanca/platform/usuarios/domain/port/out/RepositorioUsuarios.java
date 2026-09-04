package com.marcablanca.platform.usuarios.domain.port.out;

import com.marcablanca.platform.usuarios.domain.Correo;
import com.marcablanca.platform.usuarios.domain.Usuario;

import java.util.Optional;
import java.util.UUID;

public interface RepositorioUsuarios {
    Optional<Usuario> buscarPorCorreo(Correo correo);
    Optional<Usuario> buscarPorId(UUID id);
}
