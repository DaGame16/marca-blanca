package com.marcablanca.platform.usuarios.infrastructure.persistencia;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
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

    public UsuarioPerfilJpaEntity(Long id, UUID uuid, Long usuarioId, String idEmpleado, String urlFoto,
                                   String cedula, String tipoDocumento, LocalDate fechaNacimiento,
                                   String telefono, String direccion, String contactoEmergencia,
                                   String telefonoEmergencia, String zona, Long cuadrillaId, String estadoLaboral) {
        this.id = id;
        this.uuid = uuid;
        this.usuarioId = usuarioId;
        this.idEmpleado = idEmpleado;
        this.urlFoto = urlFoto;
        this.cedula = cedula;
        this.tipoDocumento = tipoDocumento;
        this.fechaNacimiento = fechaNacimiento;
        this.telefono = telefono;
        this.direccion = direccion;
        this.contactoEmergencia = contactoEmergencia;
        this.telefonoEmergencia = telefonoEmergencia;
        this.zona = zona;
        this.cuadrillaId = cuadrillaId;
        this.estadoLaboral = estadoLaboral;
    }

    @PrePersist
    void alCrear() {
        if (uuid == null) uuid = UUID.randomUUID();
        creadoEn = OffsetDateTime.now();
        actualizadoEn = creadoEn;
    }

    @PreUpdate
    void alActualizar() {
        actualizadoEn = OffsetDateTime.now();
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
}