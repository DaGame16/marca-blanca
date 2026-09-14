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

    // TEXT, no VARCHAR: el logo del wizard de registro viaja como data: URL
    // (base64) porque todavia no hay subida real de archivos -- facilmente
    // supera los limites tipicos de VARCHAR. Ver 0018-ampliar-url-logo-a-texto.
    @Column(name = "url_logo", columnDefinition = "TEXT")
    private String urlLogo;

    @Column(name = "color_primario")
    private String colorPrimario;

    @Column(name = "color_secundario")
    private String colorSecundario;

    // Snapshot del primer color no-nulo que tuvo esta fila -- ya sea el que
    // trajo el wizard de registro (aprovisionamiento) o, si el wizard se
    // salto ese paso, el primer color que la empresa guarde aca en
    // "Mi marca". Nunca se sobreescribe despues -- permite un "volver a los
    // colores iniciales" real (no solo deshacer cambios sin guardar). Ver
    // 0027-agregar-colores-originales.yaml.
    @Column(name = "color_primario_original")
    private String colorPrimarioOriginal;

    @Column(name = "color_secundario_original")
    private String colorSecundarioOriginal;

    @Column(name = "dominio_propio")
    private String dominioPropio;

    // SMALLINT en la BD (ver 0015-agregar-variantes-ui-marca.yaml) -- Short,
    // no Integer, o Hibernate falla la validacion de esquema al arrancar.
    @Column(name = "tipo_login", nullable = false)
    private Short tipoLogin;

    @Column(name = "tipo_pantalla_principal", nullable = false)
    private Short tipoPantallaPrincipal;

    // 1=contener (no recorta, puede dejar espacio vacio), 2=cubrir (llena la
    // caja, puede recortar), 3=estirar (llena exacto, puede deformar). Ver
    // 0025-agregar-ajuste-logo.yaml.
    @Column(name = "ajuste_logo", nullable = false)
    private Short ajusteLogo;

    // 1=cuadrado, 2=rectangular, 3=circular -- forma de la caja del logo
    // (border-radius/aspect-ratio), independiente de ajusteLogo. Ver
    // 0026-agregar-forma-logo.yaml.
    @Column(name = "forma_logo", nullable = false)
    private Short formaLogo;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    protected EmpresaMarcaEntity() {
        // Requerido por JPA
    }

    EmpresaMarcaEntity(Long empresaId, String urlLogo, String colorPrimario, String colorSecundario,
                        String dominioPropio, Integer tipoLogin, Integer tipoPantallaPrincipal, Integer ajusteLogo,
                        Integer formaLogo) {
        this.uuid = UUID.randomUUID();
        this.empresaId = empresaId;
        this.urlLogo = urlLogo;
        this.colorPrimario = colorPrimario;
        this.colorSecundario = colorSecundario;
        this.colorPrimarioOriginal = colorPrimario;
        this.colorSecundarioOriginal = colorSecundario;
        this.dominioPropio = dominioPropio;
        this.tipoLogin = aCorto(tipoLogin, (short) 1);
        this.tipoPantallaPrincipal = aCorto(tipoPantallaPrincipal, (short) 1);
        this.ajusteLogo = aCorto(ajusteLogo, (short) 1);
        this.formaLogo = aCorto(formaLogo, (short) 1);
        this.creadoEn = OffsetDateTime.now();
        this.actualizadoEn = OffsetDateTime.now();
    }

    void actualizar(String urlLogo, String colorPrimario, String colorSecundario, String dominioPropio,
                     Integer tipoLogin, Integer tipoPantallaPrincipal, Integer ajusteLogo, Integer formaLogo) {
        this.urlLogo = urlLogo;
        if (this.colorPrimarioOriginal == null && colorPrimario != null) {
            this.colorPrimarioOriginal = colorPrimario;
        }
        if (this.colorSecundarioOriginal == null && colorSecundario != null) {
            this.colorSecundarioOriginal = colorSecundario;
        }
        this.colorPrimario = colorPrimario;
        this.colorSecundario = colorSecundario;
        this.dominioPropio = dominioPropio;
        this.tipoLogin = aCorto(tipoLogin, this.tipoLogin);
        this.tipoPantallaPrincipal = aCorto(tipoPantallaPrincipal, this.tipoPantallaPrincipal);
        this.ajusteLogo = aCorto(ajusteLogo, this.ajusteLogo);
        this.formaLogo = aCorto(formaLogo, this.formaLogo);
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

    String getColorPrimarioOriginal() {
        return colorPrimarioOriginal;
    }

    String getColorSecundarioOriginal() {
        return colorSecundarioOriginal;
    }

    Integer getTipoLogin() {
        return tipoLogin != null ? tipoLogin.intValue() : null;
    }

    Integer getTipoPantallaPrincipal() {
        return tipoPantallaPrincipal != null ? tipoPantallaPrincipal.intValue() : null;
    }

    Integer getAjusteLogo() {
        return ajusteLogo != null ? ajusteLogo.intValue() : null;
    }

    Integer getFormaLogo() {
        return formaLogo != null ? formaLogo.intValue() : null;
    }

    String getDominioPropio() {
        return dominioPropio;
    }
}
