package com.marcablanca.platform.usuarios.infrastructure.persistencia;

import com.marcablanca.platform.usuarios.domain.Correo;
import com.marcablanca.platform.usuarios.domain.EstadoCuenta;
import com.marcablanca.platform.usuarios.domain.HashContrasena;
import com.marcablanca.platform.usuarios.domain.Usuario;

public final class UsuarioMapper {

    private UsuarioMapper() {
        throw new UnsupportedOperationException("Clase utilitaria, no instanciable");
    }

    public static Usuario aDominio(UsuarioJpaEntity entidad) {
        return new Usuario(
                entidad.getId(),
                entidad.getUuid(),
                new Correo(entidad.getCorreo()),
                new HashContrasena(entidad.getHashContrasena()),
                entidad.getNombreCompleto(),
                new EstadoCuenta(entidad.isActivo(), entidad.getIntentosFallidos(), entidad.getBloqueadoHasta()));
    }

    public static UsuarioJpaEntity aEntidad(Usuario dominio) {
        return new UsuarioJpaEntity(
                dominio.getId(),
                dominio.getUuid(),
                dominio.getCorreo().valor(),
                dominio.getHashContrasena().valor(),
                dominio.getNombreCompleto(),
                new EstadoCuentaEmbeddable(dominio.isActivo(), dominio.getIntentosFallidos(),
                        dominio.getBloqueadoHasta()));
    }
}