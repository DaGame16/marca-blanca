package com.marcablanca.platform.usuarios.infrastructure.persistencia;

import com.marcablanca.platform.usuarios.domain.Correo;
import com.marcablanca.platform.usuarios.domain.Usuario;
import com.marcablanca.platform.usuarios.domain.port.out.RepositorioUsuarios;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class RepositorioUsuariosJpaAdapter implements RepositorioUsuarios {

    private final SpringDataUsuarioRepository jpaRepository;

    public RepositorioUsuariosJpaAdapter(SpringDataUsuarioRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(Correo correo) {
        return jpaRepository.findByCorreo(correo.valor()).map(UsuarioMapper::aDominio);
    }

    @Override
    public Optional<Usuario> buscarPorUuid(UUID uuid) {
        return jpaRepository.findByUuid(uuid).map(UsuarioMapper::aDominio);
    }

    @Override
    public List<Usuario> listarTodos() {
        return jpaRepository.findAll().stream().map(UsuarioMapper::aDominio).toList();
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        UsuarioJpaEntity guardada = jpaRepository.save(UsuarioMapper.aEntidad(usuario));
        return UsuarioMapper.aDominio(guardada);
    }
}