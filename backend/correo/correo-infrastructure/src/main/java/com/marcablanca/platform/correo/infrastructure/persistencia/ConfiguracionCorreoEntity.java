package com.marcablanca.platform.correo.infrastructure.persistencia;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tbl_config_correo", schema = "plataforma")
class ConfiguracionCorreoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "remitente_nombre")
    private String remitenteNombre;

    @Column(name = "remitente_correo", nullable = false)
    private String remitenteCorreo;

    @Column(name = "responder_a")
    private String responderA;

    @Column(nullable = false)
    private String host;

    @Column(nullable = false)
    private int puerto;

    private String usuario;

    @Column(name = "secreto_ref")
    private String secretoRef;

    @Column(nullable = false)
    private String seguridad;

    @Column(name = "es_activa", nullable = false)
    private boolean esActiva;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    protected ConfiguracionCorreoEntity() {
    }

    ConfiguracionCorreoEntity(String remitenteNombre, String remitenteCorreo, String responderA, String host,
                               int puerto, String usuario, String secretoRef, String seguridad) {
        this.uuid = UUID.randomUUID();
        this.remitenteNombre = remitenteNombre;
        this.remitenteCorreo = remitenteCorreo;
        this.responderA = responderA;
        this.host = host;
        this.puerto = puerto;
        this.usuario = usuario;
        this.secretoRef = secretoRef;
        this.seguridad = seguridad;
        this.esActiva = false;
        this.creadoEn = OffsetDateTime.now();
        this.actualizadoEn = OffsetDateTime.now();
    }

    void actualizar(String remitenteNombre, String remitenteCorreo, String responderA, String host, int puerto,
                     String usuario, String secretoRef, String seguridad) {
        this.remitenteNombre = remitenteNombre;
        this.remitenteCorreo = remitenteCorreo;
        this.responderA = responderA;
        this.host = host;
        this.puerto = puerto;
        this.usuario = usuario;
        this.secretoRef = secretoRef;
        this.seguridad = seguridad;
        this.actualizadoEn = OffsetDateTime.now();
    }

    void marcarActiva(boolean valor) {
        this.esActiva = valor;
        this.actualizadoEn = OffsetDateTime.now();
    }

    Long getId() { return id; }
    UUID getUuid() { return uuid; }
    String getRemitenteNombre() { return remitenteNombre; }
    String getRemitenteCorreo() { return remitenteCorreo; }
    String getResponderA() { return responderA; }
    String getHost() { return host; }
    int getPuerto() { return puerto; }
    String getUsuario() { return usuario; }
    String getSecretoRef() { return secretoRef; }
    String getSeguridad() { return seguridad; }
    boolean isEsActiva() { return esActiva; }
    OffsetDateTime getCreadoEn() { return creadoEn; }
    OffsetDateTime getActualizadoEn() { return actualizadoEn; }
}
