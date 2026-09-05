package com.marcablanca.platform.identidadvisual.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Mapeo MINIMO y de solo lectura de plataforma.tbl_empresas -- solo lo
 * que hace falta para resolver el id interno a partir del identificador
 * (slug). A proposito NO se reutiliza EmpresaEntity del modulo empresas:
 * este modulo no depende de "empresas" en absoluto, para que no queden
 * mezclados (mismo criterio que separa "sesiones" de "usuarios").
 */
@Entity
@Table(name = "tbl_empresas", schema = "plataforma")
class EmpresaRefDeIdentidadVisual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(nullable = false, unique = true)
    private String identificador;

    @Column(nullable = false)
    private String estado;

    protected EmpresaRefDeIdentidadVisual() {
        // Requerido por JPA
    }

    Long getId() {
        return id;
    }
}
