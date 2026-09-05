package com.marcablanca.platform.autenticacion.infrastructure.persistencia;

import com.marcablanca.platform.autenticacion.application.port.out.AlmacenDeTokensDeRefresco;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Los metodos derivados de borrado (deleteBy...) de Spring Data JPA NO
 * traen transaccion de escritura incluida como si trae save() -- por
 * defecto, Spring Data envuelve los metodos de consulta en una
 * transaccion de solo lectura, y "remove" exige una de escritura real.
 * Se especifica "clienteTransactionManager" por nombre porque el
 * proyecto tiene DOS transaction managers (control y cliente) -- un
 * @Transactional sin nombre es ambiguo entre los dos.
 *
 * No va en RenovarTokenService (capa de aplicacion) a proposito: esa
 * capa se mantiene libre de anotaciones de Spring en todo el proyecto.
 */
@Component
class AlmacenDeTokensDeRefrescoJpa implements AlmacenDeTokensDeRefresco {

    private final SesionJpaRepository sesionJpaRepository;

    AlmacenDeTokensDeRefrescoJpa(SesionJpaRepository sesionJpaRepository) {
        this.sesionJpaRepository = sesionJpaRepository;
    }

    @Override
    public void guardar(UUID usuarioId, String hashTokenRefresco, OffsetDateTime expiraEn, String infoDispositivo) {
        Long usuarioIdInterno = sesionJpaRepository.buscarIdInternoPorUuid(usuarioId)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + usuarioId));

        sesionJpaRepository.save(new SesionEntity(
                UUID.randomUUID(), usuarioIdInterno, infoDispositivo, hashTokenRefresco, expiraEn));
    }

    @Override
    public Optional<UUID> buscarUsuarioPorHashActivo(String hashTokenRefresco, OffsetDateTime ahora) {
        return sesionJpaRepository.buscarUuidUsuarioPorHashActivo(hashTokenRefresco, ahora);
    }

    @Override
    @Transactional("clienteTransactionManager")
    public void eliminarPorHash(String hashTokenRefresco) {
        sesionJpaRepository.deleteByHashTokenRefresco(hashTokenRefresco);
    }

    @Override
    @Transactional("clienteTransactionManager")
    public void eliminarTodosDeUsuario(UUID usuarioId) {
        sesionJpaRepository.buscarIdInternoPorUuid(usuarioId)
                .ifPresent(sesionJpaRepository::deleteByUsuarioId);
    }
}
