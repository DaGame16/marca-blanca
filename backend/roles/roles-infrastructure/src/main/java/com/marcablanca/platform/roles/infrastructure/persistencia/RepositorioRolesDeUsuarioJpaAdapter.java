package com.marcablanca.platform.roles.infrastructure.persistencia;

import com.marcablanca.platform.roles.application.port.out.RepositorioRolesDeUsuario;
import com.marcablanca.platform.roles.domain.Rol;
import com.marcablanca.platform.roles.domain.UsuarioNoEncontradoException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RepositorioRolesDeUsuarioJpaAdapter implements RepositorioRolesDeUsuario {

    private final SpringDataUsuarioRolRepository usuarioRolRepository;
    private final SpringDataRolRepository rolRepository;
    private final SpringDataUsuarioRefRepository usuarioRefRepository;

    public RepositorioRolesDeUsuarioJpaAdapter(SpringDataUsuarioRolRepository usuarioRolRepository,
                                                SpringDataRolRepository rolRepository,
                                                SpringDataUsuarioRefRepository usuarioRefRepository) {
        this.usuarioRolRepository = usuarioRolRepository;
        this.rolRepository = rolRepository;
        this.usuarioRefRepository = usuarioRefRepository;
    }

    @Override
    public void asignar(UUID usuarioUuid, Long rolId) {
        Long usuarioId = resolverUsuarioId(usuarioUuid);
        if (usuarioRolRepository.findByUsuarioIdAndRolId(usuarioId, rolId).isEmpty()) {
            usuarioRolRepository.save(new UsuarioRolJpaEntity(usuarioId, rolId));
        }
    }

    @Override
    public void quitar(UUID usuarioUuid, Long rolId) {
        Long usuarioId = resolverUsuarioId(usuarioUuid);
        usuarioRolRepository.deleteByUsuarioIdAndRolId(usuarioId, rolId);
    }

    @Override
    public List<Rol> listarRolesDe(UUID usuarioUuid) {
        Set<Long> rolIds = listarRolIdsDe(usuarioUuid);
        return rolRepository.findAllById(rolIds).stream().map(RolMapper::aDominio).toList();
    }

    @Override
    public Set<Long> listarRolIdsDe(UUID usuarioUuid) {
        Long usuarioId = resolverUsuarioId(usuarioUuid);
        return usuarioRolRepository.findByUsuarioId(usuarioId).stream()
                .map(UsuarioRolJpaEntity::getRolId)
                .collect(Collectors.toSet());
    }

    private Long resolverUsuarioId(UUID usuarioUuid) {
        return usuarioRefRepository.findByUuid(usuarioUuid)
                .map(UsuarioRefDeRoles::getId)
                .orElseThrow(() -> new UsuarioNoEncontradoException(usuarioUuid));
    }
}
