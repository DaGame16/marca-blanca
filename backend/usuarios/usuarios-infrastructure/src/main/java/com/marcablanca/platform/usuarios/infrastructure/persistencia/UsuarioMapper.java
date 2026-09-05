package com.marcablanca.platform.usuarios.infrastructure.persistencia;

import com.marcablanca.platform.usuarios.domain.Correo;
import com.marcablanca.platform.usuarios.domain.HashContrasena;
import com.marcablanca.platform.usuarios.domain.Usuario;

public class UsuarioMapper {

    public static Usuario aDominio(UsuarioJpaEntity entidad) {
        return new Usuario(
                entidad.getId(),
                entidad.getUuid(),
                new Correo(entidad.getCorreo()),
                new HashContrasena(entidad.getHashContrasena()),
                entidad.getNombreCompleto(),
                entidad.isActivo(),
                entidad.getIntentosFallidos(),
                entidad.getBloqueadoHasta()
        );
    }

    public static UsuarioJpaEntity aEntidad(Usuario dominio) {
        return new UsuarioJpaEntity(
                dominio.getId(),
                dominio.getUuid(),
                dominio.getCorreo().valor(),
                dominio.getHashContrasena().valor(),
                dominio.getNombreCompleto(),
                dominio.isActivo(),
                dominio.getIntentosFallidos(),
                dominio.getBloqueadoHasta()
        );
    }
}