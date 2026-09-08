package com.marcablanca.platform.aprovisionamiento.infrastructure.persistencia;

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

@Entity(name = "TareaDeAprovisionamiento")
@Table(name = "tbl_aprovisionamiento_tareas", schema = "plataforma")
class TareaDeAprovisionamientoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "evento_saliente_id")
    private Long eventoSalienteId;

    @Column(nullable = false)
    private String estado;

    @Column(name = "paso_actual", nullable = false)
    private String pasoActual;

    @Column(name = "nombre_bd")
    private String nombreBd;

    @Column(nullable = false)
    private int intentos;

    @Column(name = "max_intentos", nullable = false)
    private int maxIntentos;

    @Column(name = "disponible_en", nullable = false)
    private OffsetDateTime disponibleEn;

    @Column(name = "ultimo_error")
    private String ultimoError;

    @Column(name = "iniciado_en")
    private OffsetDateTime iniciadoEn;

    @Column(name = "completado_en")
    private OffsetDateTime completadoEn;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    protected TareaDeAprovisionamientoEntity() {
        // Requerido por JPA
    }

    @PrePersist
    void alCrear() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
        creadoEn = ahora;
        actualizadoEn = ahora;
        if (iniciadoEn == null) {
            iniciadoEn = ahora;
        }
    }

    @PreUpdate
    void alActualizar() {
        actualizadoEn = OffsetDateTime.now(ZoneOffset.UTC);
    }

    Long getId() { return id; }
    UUID getUuid() { return uuid; }
    Long getEmpresaId() { return empresaId; }
    Long getEventoSalienteId() { return eventoSalienteId; }
    String getEstado() { return estado; }
    String getPasoActual() { return pasoActual; }
    String getNombreBd() { return nombreBd; }
    int getIntentos() { return intentos; }
    int getMaxIntentos() { return maxIntentos; }
    OffsetDateTime getDisponibleEn() { return disponibleEn; }
    String getUltimoError() { return ultimoError; }
    OffsetDateTime getCompletadoEn() { return completadoEn; }

    void setUuid(UUID uuid) { this.uuid = uuid; }
    void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }
    void setEventoSalienteId(Long id) { this.eventoSalienteId = id; }
    void setEstado(String estado) { this.estado = estado; }
    void setPasoActual(String pasoActual) { this.pasoActual = pasoActual; }
    void setNombreBd(String nombreBd) { this.nombreBd = nombreBd; }
    void setIntentos(int intentos) { this.intentos = intentos; }
    void setMaxIntentos(int maxIntentos) { this.maxIntentos = maxIntentos; }
    void setDisponibleEn(OffsetDateTime t) { this.disponibleEn = t; }
    void setUltimoError(String ultimoError) { this.ultimoError = ultimoError; }
    void setCompletadoEn(OffsetDateTime t) { this.completadoEn = t; }
}