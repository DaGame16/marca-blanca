package com.marcablanca.platform.autenticacion.infrastructure.persistencia;

import com.marcablanca.platform.autenticacion.application.port.out.AlmacenDeTokensDeRefresco;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

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
    public void eliminarPorHash(String hashTokenRefresco) {
        sesionJpaRepository.deleteByHashTokenRefresco(hashTokenRefresco);
    }

    @Override
    public void eliminarTodosDeUsuario(UUID usuarioId) {
        sesionJpaRepository.buscarIdInternoPorUuid(usuarioId)
                .ifPresent(sesionJpaRepository::deleteByUsuarioId);
    }
}
