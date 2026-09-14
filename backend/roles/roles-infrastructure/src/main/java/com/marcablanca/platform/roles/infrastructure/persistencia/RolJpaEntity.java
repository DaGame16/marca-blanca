package com.marcablanca.platform.roles.infrastructure.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "tbl_roles", schema = "seguridad")
public class RolJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(nullable = false, unique = true)
    private String nombre;

    private String descripcion;

    @Column(name = "es_del_sistema", nullable = false)
    private boolean esDelSistema;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    protected RolJpaEntity() {
    }

    public RolJpaEntity(Long id, UUID uuid, String nombre, String descripcion, boolean esDelSistema) {
        this.id = id;
        this.uuid = uuid;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.esDelSistema = esDelSistema;
    }

    @PrePersist
    void alCrear() {
        if (uuid == null) uuid = UUID.randomUUID();
        creadoEn = OffsetDateTime.now(ZoneOffset.UTC);
        actualizadoEn = creadoEn;
    }

    @PreUpdate
    void alActualizar() {
        actualizadoEn = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isEsDelSistema() {
        return esDelSistema;
    }
}
