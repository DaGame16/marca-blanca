package com.marcablanca.platform.roles.infrastructure.persistencia;

import com.marcablanca.platform.roles.application.port.out.RepositorioRoles;
import com.marcablanca.platform.roles.domain.Rol;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class RepositorioRolesJpaAdapter implements RepositorioRoles {

    private final SpringDataRolRepository jpaRepository;
    private final SpringDataUsuarioRolRepository usuarioRolRepository;

    public RepositorioRolesJpaAdapter(SpringDataRolRepository jpaRepository,
                                       SpringDataUsuarioRolRepository usuarioRolRepository) {
        this.jpaRepository = jpaRepository;
        this.usuarioRolRepository = usuarioRolRepository;
    }

    @Override
    public Optional<Rol> buscarPorUuid(UUID uuid) {
        return jpaRepository.findByUuid(uuid).map(RolMapper::aDominio);
    }

    @Override
    public Optional<Rol> buscarPorNombre(String nombre) {
        return jpaRepository.findByNombre(nombre).map(RolMapper::aDominio);
    }

    @Override
    public List<Rol> listarTodos() {
        return jpaRepository.findAll().stream().map(RolMapper::aDominio).toList();
    }

    @Override
    public Rol guardar(Rol rol) {
        RolJpaEntity guardado = jpaRepository.save(RolMapper.aEntidad(rol));
        return RolMapper.aDominio(guardado);
    }

    @Override
    public void eliminar(Rol rol) {
        jpaRepository.deleteById(rol.getId());
    }

    @Override
    public long contarUsuariosAsignados(Long rolId) {
        return usuarioRolRepository.countByRolId(rolId);
    }
}
