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
 * plataforma.tbl_empresas_omnicanal -- directorio de ruteo del webhook de LIWA:
 * a partir del secreto del header dice de que empresa es la conversacion
 * entrante. Es lo unico de omnicanal que vive en la base de CONTROL, porque el
 * webhook llega sin JWT ni subdominio y la resolucion de tenant necesita una
 * tabla transversal (mismo rol que tbl_empresa_conexiones).
 *
 * El resto de la configuracion de omnicanal por empresa (token LIWA, flags de
 * IA, perfil de analisis) vive en el schema "omnicanal" de la base de cada
 * empresa y se lee una vez resuelto el contexto de tenant.
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

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    protected EmpresaOmnicanalEntity() {
    }

    Long getEmpresaId() {
        return empresaId;
    }
}
