package com.marcablanca.platform.usuarios.infrastructure.persistencia;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "tbl_usuarios", schema = "seguridad")
public class UsuarioJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(name = "hash_contrasena", nullable = false)
    private String hashContrasena;

    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    @Column(name = "es_contrasena_temporal", nullable = false)
    private boolean esContrasenaTemporal;

    @Embedded
    private EstadoCuentaEmbeddable estadoCuenta;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    protected UsuarioJpaEntity() {}

    public UsuarioJpaEntity(Long id, UUID uuid, String correo, String hashContrasena,
                             String nombreCompleto, EstadoCuentaEmbeddable estadoCuenta,
                             boolean esContrasenaTemporal) {
        this.id = id;
        this.uuid = uuid;
        this.correo = correo;
        this.hashContrasena = hashContrasena;
        this.nombreCompleto = nombreCompleto;
        this.estadoCuenta = estadoCuenta;
        this.esContrasenaTemporal = esContrasenaTemporal;
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

    public Long getId() { return id; }
    public UUID getUuid() { return uuid; }
    public String getCorreo() { return correo; }
    public String getHashContrasena() { return hashContrasena; }
    public String getNombreCompleto() { return nombreCompleto; }
    public boolean isEsContrasenaTemporal() { return esContrasenaTemporal; }
    public boolean isActivo() { return estadoCuenta.isActivo(); }
    public int getIntentosFallidos() { return estadoCuenta.getIntentosFallidos(); }
    public OffsetDateTime getBloqueadoHasta() { return estadoCuenta.getBloqueadoHasta(); }
}