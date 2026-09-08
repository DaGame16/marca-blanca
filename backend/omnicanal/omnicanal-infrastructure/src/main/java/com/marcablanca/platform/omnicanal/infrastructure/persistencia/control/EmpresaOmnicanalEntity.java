package com.marcablanca.platform.omnicanal.infrastructure.persistencia.control;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * PENDIENTE -- tbl_empresas_omnicanal todavia NO EXISTE. Esta clase esta
 * lista para cuando Leidi la cree (ver spec pasada aparte), pero NO esta
 * registrada en ConfiguracionPersistenciaControl todavia -- si lo estuviera,
 * @EnableJpaRepositories con hibernate.ddl-auto=validate tumbaria el arranque
 * completo de la app contra una tabla que no existe.
 *
 * Pasos para activar cuando la tabla exista:
 *   1. Agregar "com.marcablanca.platform.omnicanal.infrastructure.persistencia.control"
 *      a basePackages de @EnableJpaRepositories Y a .packages(...) en
 *      ConfiguracionPersistenciaControl.
 *   2. Cambiar la anotacion @Component de ResolverEmpresaPorWebhookSecretoPendiente
 *      a ResolverEmpresaPorWebhookSecretoJpa (o borrar la Pendiente y renombrar).
 */
@Entity
@Table(name = "tbl_empresas_omnicanal", schema = "plataforma")
class EmpresaOmnicanalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "empresa_id", nullable = false, unique = true)
    private Long empresaId;

    @Column(name = "webhook_secret", nullable = false, unique = true)
    private String webhookSecret;

    @Column(name = "liwa_api_token")
    private String liwaApiToken;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    protected EmpresaOmnicanalEntity() {
    }

    Long getEmpresaId() {
        return empresaId;
    }

    String getWebhookSecret() {
        return webhookSecret;
    }

    String getLiwaApiToken() {
        return liwaApiToken;
    }
}
