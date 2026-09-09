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

    // SMALLINT en la BD (ver 0015-agregar-variantes-ui-marca.yaml) -- Short,
    // no Integer, o Hibernate falla la validacion de esquema al arrancar.
    @Column(name = "tipo_login", nullable = false)
    private Short tipoLogin;

    @Column(name = "tipo_pantalla_principal", nullable = false)
    private Short tipoPantallaPrincipal;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    protected EmpresaMarcaEntity() {
        // Requerido por JPA
    }

    EmpresaMarcaEntity(Long empresaId, String urlLogo, String colorPrimario, String colorSecundario,
                        String dominioPropio, Integer tipoLogin, Integer tipoPantallaPrincipal) {
        this.uuid = UUID.randomUUID();
        this.empresaId = empresaId;
        this.urlLogo = urlLogo;
        this.colorPrimario = colorPrimario;
        this.colorSecundario = colorSecundario;
        this.dominioPropio = dominioPropio;
        this.tipoLogin = aCorto(tipoLogin, (short) 1);
        this.tipoPantallaPrincipal = aCorto(tipoPantallaPrincipal, (short) 1);
        this.creadoEn = OffsetDateTime.now();
        this.actualizadoEn = OffsetDateTime.now();
    }

    void actualizar(String urlLogo, String colorPrimario, String colorSecundario, String dominioPropio,
                     Integer tipoLogin, Integer tipoPantallaPrincipal) {
        this.urlLogo = urlLogo;
        this.colorPrimario = colorPrimario;
        this.colorSecundario = colorSecundario;
        this.dominioPropio = dominioPropio;
        this.tipoLogin = aCorto(tipoLogin, this.tipoLogin);
        this.tipoPantallaPrincipal = aCorto(tipoPantallaPrincipal, this.tipoPantallaPrincipal);
        this.actualizadoEn = OffsetDateTime.now();
    }

    private static Short aCorto(Integer valor, Short porDefecto) {
        return valor != null ? valor.shortValue() : porDefecto;
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

    Integer getTipoLogin() {
        return tipoLogin != null ? tipoLogin.intValue() : null;
    }

    Integer getTipoPantallaPrincipal() {
        return tipoPantallaPrincipal != null ? tipoPantallaPrincipal.intValue() : null;
    }

    String getDominioPropio() {
        return dominioPropio;
    }
}
