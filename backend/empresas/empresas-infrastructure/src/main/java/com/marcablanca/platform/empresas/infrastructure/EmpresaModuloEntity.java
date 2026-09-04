package com.marcablanca.platform.empresas.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tbl_empresa_modulos", schema = "plataforma")
class EmpresaModuloEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "modulo_id", nullable = false)
    private Long moduloId;

    @Column(name = "es_activo", nullable = false)
    private Boolean esActivo;

    @Column(name = "activado_en")
    private OffsetDateTime activadoEn;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    protected EmpresaModuloEntity() {
        // Requerido por JPA
    }

    EmpresaModuloEntity(UUID uuid, Long empresaId, Long moduloId, boolean esActivo, OffsetDateTime activadoEn) {
        this.uuid = uuid;
        this.empresaId = empresaId;
        this.moduloId = moduloId;
        this.esActivo = esActivo;
        this.activadoEn = activadoEn;
        this.creadoEn = OffsetDateTime.now();
        this.actualizadoEn = OffsetDateTime.now();
    }

    void activar() {
        this.esActivo = true;
        this.activadoEn = OffsetDateTime.now();
        this.actualizadoEn = OffsetDateTime.now();
    }

    void desactivar() {
        this.esActivo = false;
        this.actualizadoEn = OffsetDateTime.now();
    }
}
