package com.marcablanca.platform.empresas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Mapeo de solo lectura de plataforma.tbl_empresas (base de control).
 */
@Entity
@Table(name = "tbl_empresas", schema = "plataforma")
public class EmpresaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(nullable = false, unique = true)
    private String identificador;

    @Column(name = "nombre_legal", nullable = false)
    private String nombreLegal;

    @Column(nullable = false)
    private String estado;

    protected EmpresaEntity() {
        // Requerido por JPA
    }

    public Long getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getIdentificador() {
        return identificador;
    }

    public String getNombreLegal() {
        return nombreLegal;
    }

    public String getEstado() {
        return estado;
    }
}
