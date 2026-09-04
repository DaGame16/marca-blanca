package com.marcablanca.platform.autenticacion.infrastructure.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Mapeo de seguridad.tbl_sesiones -- vive en la base de CADA empresa,
 * igual que tbl_usuarios. "usuario_id" se guarda como el id interno
 * (BIGINT) sin relacion @ManyToOne para no acoplar este modulo a
 * UsuarioEntity (que vive en usuarios-infrastructure) -- se resuelve
 * con consultas JPQL cruzadas, ver SesionJpaRepository.
 *
 * direccion_ip (tipo INET en la base) no se mapea todavia -- queda
 * pendiente para cuando haga falta de verdad, para no meterse con la
 * conversion de tipos Postgres <-> Java sin necesidad real.
 */
@Entity
@Table(name = "tbl_sesiones", schema = "seguridad")
class SesionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "info_dispositivo")
    private String infoDispositivo;

    @Column(name = "hash_token_refresco", nullable = false)
    private String hashTokenRefresco;

    @Column(name = "expira_en", nullable = false)
    private OffsetDateTime expiraEn;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    protected SesionEntity() {
        // Requerido por JPA
    }

    SesionEntity(UUID uuid, Long usuarioId, String infoDispositivo, String hashTokenRefresco,
                 OffsetDateTime expiraEn) {
        this.uuid = uuid;
        this.usuarioId = usuarioId;
        this.infoDispositivo = infoDispositivo;
        this.hashTokenRefresco = hashTokenRefresco;
        this.expiraEn = expiraEn;
        this.creadoEn = OffsetDateTime.now();
    }

    Long getUsuarioId() {
        return usuarioId;
    }

    String getHashTokenRefresco() {
        return hashTokenRefresco;
    }

    OffsetDateTime getExpiraEn() {
        return expiraEn;
    }
}
