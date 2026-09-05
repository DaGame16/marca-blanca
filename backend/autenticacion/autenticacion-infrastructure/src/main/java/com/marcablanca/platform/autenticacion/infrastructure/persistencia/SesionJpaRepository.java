package com.marcablanca.platform.autenticacion.infrastructure.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Las dos consultas que cruzan hacia el usuario lo hacen contra
 * UsuarioRefDeAutenticacion (mapeo propio, minimo, de solo lectura) --
 * nunca contra la entidad real del modulo usuarios, para no depender
 * de un nombre de clase que ese modulo puede cambiar sin aviso.
 */
public interface SesionJpaRepository extends JpaRepository<SesionEntity, Long> {

    void deleteByHashTokenRefresco(String hashTokenRefresco);

    void deleteByUsuarioId(Long usuarioId);

    @Query("select u.id from UsuarioRefDeAutenticacion u where u.uuid = :usuarioUuid")
    Optional<Long> buscarIdInternoPorUuid(@Param("usuarioUuid") UUID usuarioUuid);

    @Query("select u.uuid from SesionEntity s join UsuarioRefDeAutenticacion u on u.id = s.usuarioId "
            + "where s.hashTokenRefresco = :hash and s.expiraEn > :ahora")
    Optional<UUID> buscarUuidUsuarioPorHashActivo(@Param("hash") String hash,
                                                    @Param("ahora") OffsetDateTime ahora);
}
