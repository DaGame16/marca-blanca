package com.marcablanca.platform.usuarios.infrastructure.persistencia;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
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

    @Column(name = "es_activo", nullable = false)
    private boolean activo;

    @Column(name = "intentos_fallidos", nullable = false)
    private int intentosFallidos;

    @Column(name = "bloqueado_hasta")
    private OffsetDateTime bloqueadoHasta;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    protected UsuarioJpaEntity() {}

    public UsuarioJpaEntity(Long id, UUID uuid, String correo, String hashContrasena,
                             String nombreCompleto, boolean activo,
                             int intentosFallidos, OffsetDateTime bloqueadoHasta) {
        this.id = id;
        this.uuid = uuid;
        this.correo = correo;
        this.hashContrasena = hashContrasena;
        this.nombreCompleto = nombreCompleto;
        this.activo = activo;
        this.intentosFallidos = intentosFallidos;
        this.bloqueadoHasta = bloqueadoHasta;
    }

    @PrePersist
    void alCrear() {
        if (uuid == null) uuid = UUID.randomUUID();
        creadoEn = OffsetDateTime.now();
        actualizadoEn = creadoEn;
    }

    @PreUpdate
    void alActualizar() {
        actualizadoEn = OffsetDateTime.now();
    }

    // --- Getters (necesarios para el mapper) ---
    public Long getId() { return id; }
    public UUID getUuid() { return uuid; }
    public String getCorreo() { return correo; }
    public String getHashContrasena() { return hashContrasena; }
    public String getNombreCompleto() { return nombreCompleto; }
    public boolean isActivo() { return activo; }
    public int getIntentosFallidos() { return intentosFallidos; }
    public OffsetDateTime getBloqueadoHasta() { return bloqueadoHasta; }
}