package com.marcablanca.platform.correo.infrastructure.persistencia;

import com.marcablanca.platform.correo.application.ComandoConfiguracionSmtp;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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

    // Cifrada con CifradorDeCorreo (AES) -- nunca se guarda ni se expone en
    // texto plano. Null si esta config todavia no tiene clave configurada.
    @Column(name = "clave_cifrada", columnDefinition = "TEXT")
    private String claveCifrada;

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

    ConfiguracionCorreoEntity(ComandoConfiguracionSmtp datos, String claveCifrada) {
        this.uuid = UUID.randomUUID();
        this.remitenteNombre = datos.remitenteNombre();
        this.remitenteCorreo = datos.remitenteCorreo();
        this.responderA = datos.responderA();
        this.host = datos.host();
        this.puerto = datos.puerto();
        this.usuario = datos.usuario();
        this.secretoRef = datos.secretoRef();
        this.seguridad = datos.seguridad();
        this.claveCifrada = claveCifrada;
        this.esActiva = false;
        this.creadoEn = OffsetDateTime.now(ZoneOffset.UTC);
        this.actualizadoEn = OffsetDateTime.now(ZoneOffset.UTC);
    }

    void actualizar(ComandoConfiguracionSmtp datos) {
        this.remitenteNombre = datos.remitenteNombre();
        this.remitenteCorreo = datos.remitenteCorreo();
        this.responderA = datos.responderA();
        this.host = datos.host();
        this.puerto = datos.puerto();
        this.usuario = datos.usuario();
        this.secretoRef = datos.secretoRef();
        this.seguridad = datos.seguridad();
        this.actualizadoEn = OffsetDateTime.now(ZoneOffset.UTC);
    }

    /** Null = "no cambiar la clave que ya tenia" -- así el admin no tiene que reescribirla en cada edicion. */
    void actualizarClave(String claveCifrada) {
        if (claveCifrada != null) {
            this.claveCifrada = claveCifrada;
            this.actualizadoEn = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }

    void marcarActiva(boolean valor) {
        this.esActiva = valor;
        this.actualizadoEn = OffsetDateTime.now(ZoneOffset.UTC);
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
    String getClaveCifrada() { return claveCifrada; }
    String getSeguridad() { return seguridad; }
    boolean isEsActiva() { return esActiva; }
    OffsetDateTime getCreadoEn() { return creadoEn; }
    OffsetDateTime getActualizadoEn() { return actualizadoEn; }
}
