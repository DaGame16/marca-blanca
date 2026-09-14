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

/** Ajuste puntual de permiso por usuario (tbl_permisos_de_usuario). */
@Entity
@Table(name = "tbl_permisos_de_usuario", schema = "seguridad")
class PermisoDeUsuarioJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "permiso_id", nullable = false)
    private Long permisoId;

    @Column(name = "es_concedido", nullable = false)
    private boolean esConcedido;

    @Column(name = "asignado_en", nullable = false)
    private OffsetDateTime asignadoEn;

    protected PermisoDeUsuarioJpaEntity() {
    }

    PermisoDeUsuarioJpaEntity(Long usuarioId, Long permisoId, boolean esConcedido) {
        this.usuarioId = usuarioId;
        this.permisoId = permisoId;
        this.esConcedido = esConcedido;
    }

    @PrePersist
    void alCrear() {
        if (uuid == null) uuid = UUID.randomUUID();
        asignadoEn = OffsetDateTime.now(ZoneOffset.UTC);
    }

    Long getId() {
        return id;
    }

    Long getPermisoId() {
        return permisoId;
    }

    boolean isEsConcedido() {
        return esConcedido;
    }

    void setEsConcedido(boolean esConcedido) {
        this.esConcedido = esConcedido;
    }
}
