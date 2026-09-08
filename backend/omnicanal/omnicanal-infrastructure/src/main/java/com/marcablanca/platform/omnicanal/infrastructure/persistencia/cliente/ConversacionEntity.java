package com.marcablanca.platform.omnicanal.infrastructure.persistencia.cliente;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tbl_conversaciones_liwa", schema = "omnicanal")
class ConversacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "id_contacto", nullable = false)
    private String idContacto;

    @Column(name = "nombre_contacto")
    private String nombreContacto;

    @Column(name = "historial_chat_completo", nullable = false)
    private String historialChatCompleto;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "datos_crudos", nullable = false)
    private String datosCrudos;

    @Column(name = "es_de_ads", nullable = false)
    private boolean esDeAds;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "archivada_en")
    private OffsetDateTime archivadaEn;

    protected ConversacionEntity() {
    }

    ConversacionEntity(String idContacto, String nombreContacto, String historialChatCompleto,
                        String datosCrudos, boolean esDeAds) {
        this.uuid = UUID.randomUUID();
        this.idContacto = idContacto;
        this.nombreContacto = nombreContacto;
        this.historialChatCompleto = historialChatCompleto;
        this.datosCrudos = datosCrudos;
        this.esDeAds = esDeAds;
        this.creadoEn = OffsetDateTime.now();
        this.archivadaEn = OffsetDateTime.now();
    }

    void actualizar(String historialChatCompleto, String datosCrudos, boolean esDeAds, OffsetDateTime archivadaEn) {
        this.historialChatCompleto = historialChatCompleto;
        this.datosCrudos = datosCrudos;
        this.esDeAds = esDeAds;
        this.archivadaEn = archivadaEn;
    }

    void marcarSoloArchivado(boolean esDeAds, OffsetDateTime archivadaEn) {
        this.esDeAds = esDeAds;
        this.archivadaEn = archivadaEn;
    }

    Long getId() {
        return id;
    }

    UUID getUuid() {
        return uuid;
    }

    String getIdContacto() {
        return idContacto;
    }

    String getNombreContacto() {
        return nombreContacto;
    }

    String getHistorialChatCompleto() {
        return historialChatCompleto;
    }

    String getDatosCrudos() {
        return datosCrudos;
    }

    boolean isEsDeAds() {
        return esDeAds;
    }

    OffsetDateTime getCreadoEn() {
        return creadoEn;
    }

    OffsetDateTime getArchivadaEn() {
        return archivadaEn;
    }
}
