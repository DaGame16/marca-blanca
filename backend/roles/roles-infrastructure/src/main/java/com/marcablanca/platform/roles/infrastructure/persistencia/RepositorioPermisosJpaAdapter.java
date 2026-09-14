package com.marcablanca.platform.roles.infrastructure.persistencia;

import com.marcablanca.platform.roles.application.port.out.RepositorioPermisos;
import com.marcablanca.platform.roles.domain.Permiso;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RepositorioPermisosJpaAdapter implements RepositorioPermisos {

    private final SpringDataPermisoRepository permisoRepository;
    private final SpringDataPermisoDeRolRepository permisoDeRolRepository;

    public RepositorioPermisosJpaAdapter(SpringDataPermisoRepository permisoRepository,
                                          SpringDataPermisoDeRolRepository permisoDeRolRepository) {
        this.permisoRepository = permisoRepository;
        this.permisoDeRolRepository = permisoDeRolRepository;
    }

    @Override
    public List<Permiso> listarTodos() {
        return permisoRepository.findAll().stream().map(PermisoMapper::aDominio).toList();
    }

    @Override
    public Optional<Permiso> buscarPorUuid(UUID uuid) {
        return permisoRepository.findByUuid(uuid).map(PermisoMapper::aDominio);
    }

    @Override
    public List<Permiso> listarDeRol(Long rolId) {
        Set<Long> permisoIds = permisoDeRolRepository.findByRolId(rolId).stream()
                .map(PermisoDeRolJpaEntity::getPermisoId)
                .collect(Collectors.toSet());
        return permisoRepository.findAllById(permisoIds).stream().map(PermisoMapper::aDominio).toList();
    }

    @Override
    public Set<String> listarNombresDeRoles(Set<Long> rolIds) {
        if (rolIds.isEmpty()) {
            return Set.of();
        }
        Set<Long> permisoIds = permisoDeRolRepository.findByRolIdIn(rolIds).stream()
                .map(PermisoDeRolJpaEntity::getPermisoId)
                .collect(Collectors.toSet());
        return permisoRepository.findAllById(permisoIds).stream()
                .map(PermisoJpaEntity::getNombre)
                .collect(Collectors.toSet());
    }

    @Override
    public void asignarARol(Long rolId, Long permisoId) {
        if (permisoDeRolRepository.findByRolIdAndPermisoId(rolId, permisoId).isEmpty()) {
            permisoDeRolRepository.save(new PermisoDeRolJpaEntity(rolId, permisoId));
        }
    }

    @Override
    // Los metodos derivados de borrado (deleteBy...) de Spring Data JPA NO
    // traen transaccion de escritura incluida como si trae save() -- por
    // defecto, Spring Data envuelve los metodos de consulta en una
    // transaccion de solo lectura, y remove() individual (lo que dispara un
    // deleteBy sin @Modifying+@Query) exige una transaccion de escritura
    // real. Sin esto, quitarDeRol lanzaba TransactionRequiredException
    // (500 traducido a 401 por el filtro de seguridad), y el interceptor de
    // refresh del frontend, al ver un 401 tambien en el reintento, cerraba
    // la sesion del usuario -- un doble click en un permiso terminaba
    // deslogueando a alguien con una sesion perfectamente valida. Mismo
    // patron ya usado en AlmacenDeTokensDeRefrescoJpa. Se especifica
    // "clienteTransactionManager" por nombre porque el proyecto tiene DOS
    // transaction managers (control y cliente) -- un @Transactional sin
    // nombre es ambiguo entre los dos.
    @Transactional("clienteTransactionManager")
    public void quitarDeRol(Long rolId, Long permisoId) {
        permisoDeRolRepository.deleteByRolIdAndPermisoId(rolId, permisoId);
    }
}
