package com.marcablanca.platform.usuarios.domain.port.out;

import com.marcablanca.platform.usuarios.domain.UsuarioPerfil;

import java.util.Optional;

public interface RepositorioUsuarioPerfiles {
    Optional<UsuarioPerfil> buscarPorUsuarioId(Long usuarioId);
    UsuarioPerfil guardar(UsuarioPerfil perfil);
}