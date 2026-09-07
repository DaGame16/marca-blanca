package com.marcablanca.platform.aprovisionamiento.infrastructure.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity(name = "EventoSaliente")
@Table(name = "tbl_eventos_salientes", schema = "plataforma")
class EventoSalienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "tipo_evento", nullable = false)
    private String tipoEvento;

    @Column(name = "agregado_tipo", nullable = false)
    private String agregadoTipo;

    @Column(name = "agregado_id", nullable = false)
    private Long agregadoId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private String payload;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false)
    private int intentos;

    @Column(name = "max_intentos", nullable = false)
    private int maxIntentos;

    @Column(name = "disponible_en", nullable = false)
    private OffsetDateTime disponibleEn;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    protected EventoSalienteEntity() {
        // Requerido por JPA
    }

    EventoSalienteEntity(String tipoEvento, String agregadoTipo, Long agregadoId, String payload) {
        this.tipoEvento = tipoEvento;
        this.agregadoTipo = agregadoTipo;
        this.agregadoId = agregadoId;
        this.payload = payload;
    }

    @PrePersist
    void alCrear() {
        OffsetDateTime ahora = OffsetDateTime.now();
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        if (estado == null) {
            estado = "pendiente";
        }
        if (maxIntentos == 0) {
            maxIntentos = 10;
        }
        if (disponibleEn == null) {
            disponibleEn = ahora;
        }
        creadoEn = ahora;
        actualizadoEn = ahora;
    }
}