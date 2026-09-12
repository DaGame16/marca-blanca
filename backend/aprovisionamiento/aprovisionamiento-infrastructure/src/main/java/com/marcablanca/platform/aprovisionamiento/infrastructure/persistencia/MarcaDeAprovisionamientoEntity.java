package com.marcablanca.platform.aprovisionamiento.infrastructure.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Mapeo de escritura de plataforma.tbl_empresas_marca para el wizard de registro.
 * name explicito para no chocar en Hibernate con EmpresaMarcaEntity de identidad-visual
 * (misma tabla, otro bounded context).
 */
@Entity(name = "MarcaDeAprovisionamiento")
@Table(name = "tbl_empresas_marca", schema = "plataforma")
class MarcaDeAprovisionamientoEntity {

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

    // Snapshot del primer color no-nulo que recibio esta fila -- ver
    // 0027-agregar-colores-originales.yaml. Se llena solo, en los setters de
    // abajo, la primera vez que llega un valor real; nunca se vuelve a tocar
    // aunque el wizard se reenvie con otros colores.
    @Column(name = "color_primario_original")
    private String colorPrimarioOriginal;

    @Column(name = "color_secundario_original")
    private String colorSecundarioOriginal;

    @Column(name = "tipo_login", nullable = false)
    private short tipoLogin;

    @Column(name = "tipo_pantalla_principal", nullable = false)
    private short tipoPantallaPrincipal;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    protected MarcaDeAprovisionamientoEntity() {
        // Requerido por JPA
    }

    @PrePersist
    void alCrear() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        creadoEn = OffsetDateTime.now();
        actualizadoEn = creadoEn;
    }

    @PreUpdate
    void alActualizar() {
        actualizadoEn = OffsetDateTime.now();
    }

    void setEmpresaId(Long v) { this.empresaId = v; }
    void setUrlLogo(String v) { this.urlLogo = v; }
    void setColorPrimario(String v) {
        if (this.colorPrimarioOriginal == null && v != null) {
            this.colorPrimarioOriginal = v;
        }
        this.colorPrimario = v;
    }
    void setColorSecundario(String v) {
        if (this.colorSecundarioOriginal == null && v != null) {
            this.colorSecundarioOriginal = v;
        }
        this.colorSecundario = v;
    }
    void setTipoLogin(short v) { this.tipoLogin = v; }
    void setTipoPantallaPrincipal(short v) { this.tipoPantallaPrincipal = v; }
}
