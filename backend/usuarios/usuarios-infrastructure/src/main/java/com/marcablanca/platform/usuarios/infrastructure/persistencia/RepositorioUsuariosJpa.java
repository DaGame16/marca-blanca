package com.marcablanca.platform.usuarios.infrastructure.persistencia;

import com.marcablanca.platform.usuarios.domain.Correo;
import com.marcablanca.platform.usuarios.domain.EstadoUsuario;
import com.marcablanca.platform.usuarios.domain.HashContrasena;
import com.marcablanca.platform.usuarios.domain.Usuario;
import com.marcablanca.platform.usuarios.domain.port.out.RepositorioUsuarios;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
class RepositorioUsuariosJpa implements RepositorioUsuarios {

    private final UsuarioJpaRepository usuarioJpaRepository;

    RepositorioUsuariosJpa(UsuarioJpaRepository usuarioJpaRepository) {
        this.usuarioJpaRepository = usuarioJpaRepository;
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(Correo correo) {
        return usuarioJpaRepository.findByCorreo(correo.valor()).map(this::aDominio);
    }

    @Override
    public Optional<Usuario> buscarPorId(UUID id) {
        return usuarioJpaRepository.findByUuid(id).map(this::aDominio);
    }

    private Usuario aDominio(UsuarioEntity entity) {
        return new Usuario(
                entity.getUuid(),
                new Correo(entity.getCorreo()),
                new HashContrasena(entity.getHashContrasena()),
                resolverEstado(entity)
        );
    }

    /** es_activo + bloqueado_hasta -> EstadoUsuario. Confirmado con Luis. */
    private EstadoUsuario resolverEstado(UsuarioEntity entity) {
        OffsetDateTime bloqueadoHasta = entity.getBloqueadoHasta();
        if (bloqueadoHasta != null && bloqueadoHasta.isAfter(OffsetDateTime.now())) {
            return EstadoUsuario.BLOQUEADO;
        }
        if (Boolean.FALSE.equals(entity.getEsActivo())) {
            return EstadoUsuario.INACTIVO;
        }
        return EstadoUsuario.ACTIVO;
    }
}
