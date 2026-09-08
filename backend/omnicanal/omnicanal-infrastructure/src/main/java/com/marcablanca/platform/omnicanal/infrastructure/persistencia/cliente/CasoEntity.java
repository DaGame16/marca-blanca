package com.marcablanca.platform.omnicanal.infrastructure.persistencia.cliente;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tbl_casos_liwa", schema = "omnicanal")
class CasoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "conversacion_id", nullable = false)
    private Long conversacionId;

    @Column(name = "turno_orden_inicio", nullable = false)
    private Integer turnoOrdenInicio;

    @Column(name = "turno_orden_fin", nullable = false)
    private Integer turnoOrdenFin;

    @Column(name = "es_procesada", nullable = false)
    private boolean esProcesada;

    @Column(name = "es_de_ads", nullable = false)
    private boolean esDeAds;

    @Column(name = "archivada_en", nullable = false)
    private OffsetDateTime archivadaEn;

    protected CasoEntity() {
    }

    CasoEntity(Long conversacionId, int turnoOrdenInicio, int turnoOrdenFin, boolean esDeAds) {
        this.uuid = UUID.randomUUID();
        this.conversacionId = conversacionId;
        this.turnoOrdenInicio = turnoOrdenInicio;
        this.turnoOrdenFin = turnoOrdenFin;
        this.esProcesada = false;
        this.esDeAds = esDeAds;
        this.archivadaEn = OffsetDateTime.now();
    }

    void marcarProcesada(boolean valor) {
        this.esProcesada = valor;
    }

    Long getId() {
        return id;
    }

    UUID getUuid() {
        return uuid;
    }

    Long getConversacionId() {
        return conversacionId;
    }

    Integer getTurnoOrdenInicio() {
        return turnoOrdenInicio;
    }

    Integer getTurnoOrdenFin() {
        return turnoOrdenFin;
    }

    boolean isEsProcesada() {
        return esProcesada;
    }

    boolean isEsDeAds() {
        return esDeAds;
    }

    OffsetDateTime getArchivadaEn() {
        return archivadaEn;
    }
}
