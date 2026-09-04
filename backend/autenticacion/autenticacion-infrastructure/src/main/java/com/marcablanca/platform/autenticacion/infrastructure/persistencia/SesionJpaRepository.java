package com.marcablanca.platform.autenticacion.infrastructure.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Las dos consultas que referencian "UsuarioEntity" cruzan hacia la
 * entidad de usuarios-infrastructure por NOMBRE (JPQL no exige que
 * las clases sean publicas ni del mismo paquete -- se resuelve contra
 * el modelo de la unidad de persistencia "cliente" en tiempo de
 * ejecucion, no en tiempo de compilacion Java).
 */
interface SesionJpaRepository extends JpaRepository<SesionEntity, Long> {

    void deleteByHashTokenRefresco(String hashTokenRefresco);

    void deleteByUsuarioId(Long usuarioId);

    @Query("select u.id from UsuarioEntity u where u.uuid = :usuarioUuid")
    Optional<Long> buscarIdInternoPorUuid(@Param("usuarioUuid") UUID usuarioUuid);

    @Query("select u.uuid from SesionEntity s join UsuarioEntity u on u.id = s.usuarioId "
            + "where s.hashTokenRefresco = :hash and s.expiraEn > :ahora")
    Optional<UUID> buscarUuidUsuarioPorHashActivo(@Param("hash") String hash,
                                                    @Param("ahora") OffsetDateTime ahora);
}
