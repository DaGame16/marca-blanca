package com.marcablanca.platform.roles.infrastructure.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Mapeo MINIMO y de solo lectura de seguridad.tbl_usuarios -- solo lo que
 * hace falta para resolver id interno <-> uuid en los pivotes de este
 * modulo. A proposito NO se reutiliza la entidad real de usuarios-infrastructure
 * (mismo criterio que UsuarioRefDeAutenticacion, en autenticacion-infrastructure).
 */
@Entity
@Table(name = "tbl_usuarios", schema = "seguridad")
class UsuarioRefDeRoles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    protected UsuarioRefDeRoles() {
    }

    Long getId() {
        return id;
    }

    UUID getUuid() {
        return uuid;
    }
}
