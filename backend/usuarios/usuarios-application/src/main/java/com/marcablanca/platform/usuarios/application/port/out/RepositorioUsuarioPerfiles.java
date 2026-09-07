package com.marcablanca.platform.usuarios.application.port.out;

import com.marcablanca.platform.usuarios.domain.UsuarioPerfil;

import java.util.Optional;

public interface RepositorioUsuarioPerfiles {
    Optional<UsuarioPerfil> buscarPorUsuarioId(Long usuarioId);
    UsuarioPerfil guardar(UsuarioPerfil perfil);
}