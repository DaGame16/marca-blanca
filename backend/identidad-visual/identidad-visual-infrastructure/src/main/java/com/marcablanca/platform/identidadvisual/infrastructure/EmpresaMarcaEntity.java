package com.marcablanca.platform.identidadvisual.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tbl_empresas_marca", schema = "plataforma")
class EmpresaMarcaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "empresa_id", nullable = false, unique = true)
    private Long empresaId;

    @Column(name = "url_logo")
    private String urlLogo;

    @Column(name = "color_primario")
    private String colorPrimario;

    @Column(name = "color_secundario")
    private String colorSecundario;

    @Column(name = "dominio_propio")
    private String dominioPropio;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    protected EmpresaMarcaEntity() {
        // Requerido por JPA
    }

    EmpresaMarcaEntity(Long empresaId, String urlLogo, String colorPrimario, String colorSecundario,
                        String dominioPropio) {
        this.uuid = UUID.randomUUID();
        this.empresaId = empresaId;
        this.urlLogo = urlLogo;
        this.colorPrimario = colorPrimario;
        this.colorSecundario = colorSecundario;
        this.dominioPropio = dominioPropio;
        this.creadoEn = OffsetDateTime.now();
        this.actualizadoEn = OffsetDateTime.now();
    }

    void actualizar(String urlLogo, String colorPrimario, String colorSecundario, String dominioPropio) {
        this.urlLogo = urlLogo;
        this.colorPrimario = colorPrimario;
        this.colorSecundario = colorSecundario;
        this.dominioPropio = dominioPropio;
        this.actualizadoEn = OffsetDateTime.now();
    }

    String getUrlLogo() {
        return urlLogo;
    }

    String getColorPrimario() {
        return colorPrimario;
    }

    String getColorSecundario() {
        return colorSecundario;
    }

    String getDominioPropio() {
        return dominioPropio;
    }
}
