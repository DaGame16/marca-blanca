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

/** Pivote usuario <-> rol (tbl_usuarios_roles). Sin @ManyToOne: usuario_id y rol_id
 *  se guardan como id interno (BIGINT) plano, para no acoplar este modulo a las
 *  entidades JPA de usuarios-infrastructure. */
@Entity
@Table(name = "tbl_usuarios_roles", schema = "seguridad")
class UsuarioRolJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "rol_id", nullable = false)
    private Long rolId;

    @Column(name = "asignado_en", nullable = false)
    private OffsetDateTime asignadoEn;

    protected UsuarioRolJpaEntity() {
    }

    UsuarioRolJpaEntity(Long usuarioId, Long rolId) {
        this.usuarioId = usuarioId;
        this.rolId = rolId;
    }

    @PrePersist
    void alCrear() {
        if (uuid == null) uuid = UUID.randomUUID();
        asignadoEn = OffsetDateTime.now(ZoneOffset.UTC);
    }

    Long getRolId() {
        return rolId;
    }
}
