package com.marcablanca.platform.roles.infrastructure.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/** Pivote rol <-> permiso (tbl_permisos_de_rol). */
@Entity
@Table(name = "tbl_permisos_de_rol", schema = "seguridad")
class PermisoDeRolJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "rol_id", nullable = false)
    private Long rolId;

    @Column(name = "permiso_id", nullable = false)
    private Long permisoId;

    @Column(name = "asignado_en", nullable = false)
    private OffsetDateTime asignadoEn;

    protected PermisoDeRolJpaEntity() {
    }

    PermisoDeRolJpaEntity(Long rolId, Long permisoId) {
        this.rolId = rolId;
        this.permisoId = permisoId;
    }

    @PrePersist
    void alCrear() {
        if (uuid == null) uuid = UUID.randomUUID();
        asignadoEn = OffsetDateTime.now(ZoneOffset.UTC);
    }

    Long getPermisoId() {
        return permisoId;
    }
}
