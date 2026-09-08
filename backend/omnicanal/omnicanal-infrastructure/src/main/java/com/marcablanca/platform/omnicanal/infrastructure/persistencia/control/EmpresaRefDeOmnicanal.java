package com.marcablanca.platform.omnicanal.infrastructure.persistencia.control;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Mapeo MINIMO y de solo lectura de plataforma.tbl_empresas -- mismo patron
 * que ya usan identidad-visual y modulos-empresa. Nombrada "DeOmnicanal" a
 * proposito: dos @Entity con el mismo nombre simple colisionan en el
 * contexto de persistencia de Hibernate (ya paso una vez en este proyecto,
 * ver fix en identidad-visual/modulos-empresa).
 */
@Entity
@Table(name = "tbl_empresas", schema = "plataforma")
class EmpresaRefDeOmnicanal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(nullable = false, unique = true)
    private String identificador;

    @Column(nullable = false)
    private String estado;

    protected EmpresaRefDeOmnicanal() {
    }

    Long getId() {
        return id;
    }

    String getIdentificador() {
        return identificador;
    }
}
