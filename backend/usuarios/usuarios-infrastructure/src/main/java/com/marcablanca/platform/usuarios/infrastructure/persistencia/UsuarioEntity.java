package com.marcablanca.platform.usuarios.infrastructure.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Mapeo de seguridad.tbl_usuarios -- vive en la base de CADA empresa
 * (no en la base de control), por eso usa la segunda unidad de
 * persistencia ("cliente"), ver ConfiguracionPersistenciaCliente en bootstrap.
 */
@Entity
@Table(name = "tbl_usuarios", schema = "seguridad")
class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(name = "hash_contrasena", nullable = false)
    private String hashContrasena;

    @Column(name = "es_activo", nullable = false)
    private Boolean esActivo;

    @Column(name = "bloqueado_hasta")
    private OffsetDateTime bloqueadoHasta;

    protected UsuarioEntity() {
        // Requerido por JPA
    }

    UUID getUuid() {
        return uuid;
    }

    String getCorreo() {
        return correo;
    }

    String getHashContrasena() {
        return hashContrasena;
    }

    Boolean getEsActivo() {
        return esActivo;
    }

    OffsetDateTime getBloqueadoHasta() {
        return bloqueadoHasta;
    }
}
