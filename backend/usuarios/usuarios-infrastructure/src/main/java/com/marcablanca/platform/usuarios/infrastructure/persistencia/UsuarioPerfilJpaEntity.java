package com.marcablanca.platform.usuarios.infrastructure.persistencia;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "tbl_usuario_perfiles", schema = "seguridad")
public class UsuarioPerfilJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "usuario_id", nullable = false, unique = true)
    private Long usuarioId;

    @Column(name = "id_empleado", unique = true)
    private String idEmpleado;

    @Column(name = "url_foto")
    private String urlFoto;

    private String cedula;

    @Column(name = "tipo_documento")
    private String tipoDocumento;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    private String telefono;
    private String direccion;

    @Column(name = "contacto_emergencia")
    private String contactoEmergencia;

    @Column(name = "telefono_emergencia")
    private String telefonoEmergencia;

    private String zona;

    @Column(name = "cuadrilla_id")
    private Long cuadrillaId;

    @Column(name = "estado_laboral")
    private String estadoLaboral;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    // NOTA: tipo_sangre y notas_medicas existen en la tabla real pero se excluyen
    // de esta entidad a proposito (datos de salud, Ley 1581, cifrado pendiente
    // por columna — ver changeset seguridad-0004 y ADR correspondiente).

    protected UsuarioPerfilJpaEntity() {}

    private UsuarioPerfilJpaEntity(Builder b) {
        this.id = b.id;
        this.uuid = b.uuid;
        this.usuarioId = b.usuarioId;
        this.idEmpleado = b.idEmpleado;
        this.urlFoto = b.urlFoto;
        this.cedula = b.cedula;
        this.tipoDocumento = b.tipoDocumento;
        this.fechaNacimiento = b.fechaNacimiento;
        this.telefono = b.telefono;
        this.direccion = b.direccion;
        this.contactoEmergencia = b.contactoEmergencia;
        this.telefonoEmergencia = b.telefonoEmergencia;
        this.zona = b.zona;
        this.cuadrillaId = b.cuadrillaId;
        this.estadoLaboral = b.estadoLaboral;
    }

    public static Builder builder() {
        return new Builder();
    }

    @PrePersist
    void alCrear() {
        if (uuid == null) uuid = UUID.randomUUID();
        creadoEn = OffsetDateTime.now(ZoneOffset.UTC);
        actualizadoEn = creadoEn;
    }

    @PreUpdate
    void alActualizar() {
        actualizadoEn = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() { return id; }
    public UUID getUuid() { return uuid; }
    public Long getUsuarioId() { return usuarioId; }
    public String getIdEmpleado() { return idEmpleado; }
    public String getUrlFoto() { return urlFoto; }
    public String getCedula() { return cedula; }
    public String getTipoDocumento() { return tipoDocumento; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public String getTelefono() { return telefono; }
    public String getDireccion() { return direccion; }
    public String getContactoEmergencia() { return contactoEmergencia; }
    public String getTelefonoEmergencia() { return telefonoEmergencia; }
    public String getZona() { return zona; }
    public Long getCuadrillaId() { return cuadrillaId; }
    public String getEstadoLaboral() { return estadoLaboral; }

    public static class Builder {
        private Long id;
        private UUID uuid;
        private Long usuarioId;
        private String idEmpleado;
        private String urlFoto;
        private String cedula;
        private String tipoDocumento;
        private LocalDate fechaNacimiento;
        private String telefono;
        private String direccion;
        private String contactoEmergencia;
        private String telefonoEmergencia;
        private String zona;
        private Long cuadrillaId;
        private String estadoLaboral;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder uuid(UUID uuid) { this.uuid = uuid; return this; }
        public Builder usuarioId(Long usuarioId) { this.usuarioId = usuarioId; return this; }
        public Builder idEmpleado(String idEmpleado) { this.idEmpleado = idEmpleado; return this; }
        public Builder urlFoto(String urlFoto) { this.urlFoto = urlFoto; return this; }
        public Builder cedula(String cedula) { this.cedula = cedula; return this; }
        public Builder tipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; return this; }
        public Builder fechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; return this; }
        public Builder telefono(String telefono) { this.telefono = telefono; return this; }
        public Builder direccion(String direccion) { this.direccion = direccion; return this; }
        public Builder contactoEmergencia(String contactoEmergencia) { this.contactoEmergencia = contactoEmergencia; return this; }
        public Builder telefonoEmergencia(String telefonoEmergencia) { this.telefonoEmergencia = telefonoEmergencia; return this; }
        public Builder zona(String zona) { this.zona = zona; return this; }
        public Builder cuadrillaId(Long cuadrillaId) { this.cuadrillaId = cuadrillaId; return this; }
        public Builder estadoLaboral(String estadoLaboral) { this.estadoLaboral = estadoLaboral; return this; }

        public UsuarioPerfilJpaEntity build() {
            return new UsuarioPerfilJpaEntity(this);
        }
    }
}