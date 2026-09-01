package com.marcablanca.platform.usuarios.domain;

import java.util.Optional;

public interface RepositorioUsuarios {
    Optional<Usuario> buscarPorCorreo(Correo correo);
}
