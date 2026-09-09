package com.marcablanca.platform.modulosempresa.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "tbl_modulos", schema = "plataforma")
class ModuloEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(nullable = false)
    private BigDecimal precio;

    @Column(nullable = false, length = 3)
    private String moneda;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    protected ModuloEntity() {
        // Requerido por JPA
    }

    @PrePersist
    void alCrear() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        creadoEn = OffsetDateTime.now(ZoneOffset.UTC);
        actualizadoEn = creadoEn;
    }

    @PreUpdate
    void alActualizar() {
        actualizadoEn = OffsetDateTime.now(ZoneOffset.UTC);
    }

    Long getId() {
        return id;
    }

    UUID getUuid() {
        return uuid;
    }

    String getCodigo() {
        return codigo;
    }

    String getNombre() {
        return nombre;
    }

    String getDescripcion() {
        return descripcion;
    }

    BigDecimal getPrecio() {
        return precio;
    }

    String getMoneda() {
        return moneda;
    }

    void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    void setNombre(String nombre) {
        this.nombre = nombre;
    }

    void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    void setMoneda(String moneda) {
        this.moneda = moneda;
    }
}
