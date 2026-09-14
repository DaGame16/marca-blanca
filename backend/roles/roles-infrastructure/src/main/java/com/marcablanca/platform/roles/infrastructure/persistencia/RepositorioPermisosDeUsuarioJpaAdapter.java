package com.marcablanca.platform.roles.infrastructure.persistencia;

import com.marcablanca.platform.roles.application.port.out.RepositorioPermisosDeUsuario;
import com.marcablanca.platform.roles.domain.UsuarioNoEncontradoException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RepositorioPermisosDeUsuarioJpaAdapter implements RepositorioPermisosDeUsuario {

    private final SpringDataPermisoDeUsuarioRepository permisoDeUsuarioRepository;
    private final SpringDataPermisoRepository permisoRepository;
    private final SpringDataUsuarioRefRepository usuarioRefRepository;

    public RepositorioPermisosDeUsuarioJpaAdapter(SpringDataPermisoDeUsuarioRepository permisoDeUsuarioRepository,
                                                   SpringDataPermisoRepository permisoRepository,
                                                   SpringDataUsuarioRefRepository usuarioRefRepository) {
        this.permisoDeUsuarioRepository = permisoDeUsuarioRepository;
        this.permisoRepository = permisoRepository;
        this.usuarioRefRepository = usuarioRefRepository;
    }

    @Override
    public void registrarAjuste(UUID usuarioUuid, Long permisoId, boolean esConcedido) {
        Long usuarioId = resolverUsuarioId(usuarioUuid);
        permisoDeUsuarioRepository.findByUsuarioIdAndPermisoId(usuarioId, permisoId)
                .ifPresentOrElse(
                        existente -> {
                            existente.setEsConcedido(esConcedido);
                            permisoDeUsuarioRepository.save(existente);
                        },
                        () -> permisoDeUsuarioRepository.save(
                                new PermisoDeUsuarioJpaEntity(usuarioId, permisoId, esConcedido)));
    }

    @Override
    public void eliminarAjuste(UUID usuarioUuid, Long permisoId) {
        Long usuarioId = resolverUsuarioId(usuarioUuid);
        permisoDeUsuarioRepository.deleteByUsuarioIdAndPermisoId(usuarioId, permisoId);
    }

    @Override
    public Map<String, Boolean> listarAjustesDe(UUID usuarioUuid) {
        Long usuarioId = resolverUsuarioId(usuarioUuid);
        List<PermisoDeUsuarioJpaEntity> ajustes = permisoDeUsuarioRepository.findByUsuarioId(usuarioId);

        Set<Long> permisoIds = ajustes.stream().map(PermisoDeUsuarioJpaEntity::getPermisoId).collect(Collectors.toSet());
        Map<Long, String> nombresPorId = permisoRepository.findAllById(permisoIds).stream()
                .collect(Collectors.toMap(PermisoJpaEntity::getId, PermisoJpaEntity::getNombre));

        Map<String, Boolean> resultado = new HashMap<>();
        for (PermisoDeUsuarioJpaEntity ajuste : ajustes) {
            String nombre = nombresPorId.get(ajuste.getPermisoId());
            if (nombre != null) {
                resultado.put(nombre, ajuste.isEsConcedido());
            }
        }
        return resultado;
    }

    private Long resolverUsuarioId(UUID usuarioUuid) {
        return usuarioRefRepository.findByUuid(usuarioUuid)
                .map(UsuarioRefDeRoles::getId)
                .orElseThrow(() -> new UsuarioNoEncontradoException(usuarioUuid));
    }
}
