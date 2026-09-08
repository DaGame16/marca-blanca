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
 * Mapeo de escritura de plataforma.tbl_empresas para el ciclo de registro/activacion.
 *
 * name explicito en @Entity: ya existen otros mapeos de esta misma tabla en los
 * modulos empresas / identidad-visual / modulos-empresa. Sin un nombre de entidad
 * distinto, Hibernate choca al arrancar (leccion del ADR 0001 de modulos-empresa).
 */
@Entity(name = "EmpresaDeAprovisionamiento")
@Table(name = "tbl_empresas", schema = "plataforma")
class EmpresaDeAprovisionamientoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(nullable = false, unique = true)
    private String identificador;

    @Column(name = "nombre_legal", nullable = false)
    private String nombreLegal;

    @Column(name = "nombre_comercial")
    private String nombreComercial;

    @Column(unique = true)
    private String dominio;

    @Column(name = "representante_legal")
    private String representanteLegal;

    @Column(name = "correo_contacto")
    private String correoContacto;

    private String telefono;

    @Column(name = "sitio_web")
    private String sitioWeb;

    @Column(name = "hash_contrasena_maestra")
    private String hashContrasenaMaestra;

    @Column(nullable = false)
    private String estado;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    protected EmpresaDeAprovisionamientoEntity() {
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

    Long getId() { return id; }
    UUID getUuid() { return uuid; }
    String getIdentificador() { return identificador; }
    String getNombreLegal() { return nombreLegal; }
    String getNombreComercial() { return nombreComercial; }
    String getDominio() { return dominio; }
    String getRepresentanteLegal() { return representanteLegal; }
    String getCorreoContacto() { return correoContacto; }
    String getTelefono() { return telefono; }
    String getSitioWeb() { return sitioWeb; }
    String getHashContrasenaMaestra() { return hashContrasenaMaestra; }
    String getEstado() { return estado; }

    void setUuid(UUID uuid) { this.uuid = uuid; }
    void setIdentificador(String identificador) { this.identificador = identificador; }
    void setNombreLegal(String nombreLegal) { this.nombreLegal = nombreLegal; }
    void setNombreComercial(String nombreComercial) { this.nombreComercial = nombreComercial; }
    void setDominio(String dominio) { this.dominio = dominio; }
    void setRepresentanteLegal(String v) { this.representanteLegal = v; }
    void setCorreoContacto(String v) { this.correoContacto = v; }
    void setTelefono(String v) { this.telefono = v; }
    void setSitioWeb(String v) { this.sitioWeb = v; }
    void setHashContrasenaMaestra(String hash) { this.hashContrasenaMaestra = hash; }
    void setEstado(String estado) { this.estado = estado; }
}
