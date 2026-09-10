package com.marcablanca.platform.omnicanal.infrastructure.persistencia.cliente;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

/** omnicanal.tbl_configuracion_omnicanal -- fila unica por empresa. */
@Entity
@Table(name = "tbl_configuracion_omnicanal", schema = "omnicanal")
class ConfiguracionOmnicanalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    /** Token de la cuenta LIWA, cifrado (ver CifradorOmnicanal). */
    @Column(name = "liwa_api_token")
    private String liwaApiToken;

    @Column(name = "liwa_base_url")
    private String liwaBaseUrl;

    @Column(name = "liwa_custom_field_ads")
    private String liwaCustomFieldAds;

    @Column(name = "ia_habilitada", nullable = false)
    private boolean iaHabilitada;

    @Column(name = "openai_modelo")
    private String openaiModelo;

    /** JSON con overrides de PerfilDeAnalisisOmnicanal; null => perfil por defecto. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "perfil_analisis")
    private String perfilAnalisis;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    protected ConfiguracionOmnicanalEntity() {
    }

    String getLiwaApiToken() {
        return liwaApiToken;
    }

    boolean isIaHabilitada() {
        return iaHabilitada;
    }

    String getOpenaiModelo() {
        return openaiModelo;
    }

    String getPerfilAnalisis() {
        return perfilAnalisis;
    }
}
