package com.marcablanca.platform.autenticacion.infrastructure.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Mapeo MINIMO y de solo lectura de seguridad.tbl_usuarios -- solo lo
 * que hace falta para resolver id interno <-> uuid en las consultas de
 * SesionJpaRepository. A proposito NO se reutiliza la entidad real del
 * modulo usuarios (su nombre, y el modulo entero, pueden cambiar sin
 * aviso -- ya paso: se llamaba UsuarioEntity, ahora es UsuarioJpaEntity).
 * Mismo criterio que empresas ya usa en otros modulos (EmpresaRefEntity).
 */
@Entity
@Table(name = "tbl_usuarios", schema = "seguridad")
class UsuarioRefDeAutenticacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    protected UsuarioRefDeAutenticacion() {
        // Requerido por JPA
    }

    Long getId() {
        return id;
    }

    UUID getUuid() {
        return uuid;
    }
}
