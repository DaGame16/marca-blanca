package com.marcablanca.platform.empresas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Mapeo de solo lectura de plataforma.tbl_empresa_conexiones (base de control).
 * "secreto_ref" se mapea pero, en DEV, no se resuelve contra ningun vault --
 * eso queda pendiente para infraestructura (QA/PROD).
 */
@Entity
@Table(name = "tbl_empresa_conexiones", schema = "plataforma")
public class EmpresaConexionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "empresa_id", nullable = false, unique = true)
    private Long empresaId;

    @Column(nullable = false)
    private String host;

    @Column(nullable = false)
    private Integer puerto;

    @Column(name = "nombre_bd", nullable = false)
    private String nombreBd;

    @Column(name = "secreto_ref")
    private String secretoRef;

    @Column(name = "es_activa", nullable = false)
    private Boolean esActiva;

    protected EmpresaConexionEntity() {
        // Requerido por JPA
    }

    public Long getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public String getHost() {
        return host;
    }

    public Integer getPuerto() {
        return puerto;
    }

    public String getNombreBd() {
        return nombreBd;
    }

    public String getSecretoRef() {
        return secretoRef;
    }

    public Boolean getEsActiva() {
        return esActiva;
    }
}
