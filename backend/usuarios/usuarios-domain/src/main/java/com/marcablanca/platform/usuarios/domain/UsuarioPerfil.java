package com.marcablanca.platform.usuarios.domain;

import java.time.LocalDate;
import java.util.UUID;

public class UsuarioPerfil {

    private final Long id;
    private final UUID uuid;
    private final Long usuarioId;
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

    // NOTA: tipoSangre y notasMedicas existen en tbl_usuario_perfiles pero son
    // datos de salud (Ley 1581) pendientes de cifrado por columna (ver changeset
    // seguridad-0004). Se excluyen deliberadamente de este modelo hasta que ese
    // cifrado exista — no se leen ni se escriben desde este CRUD.

    public UsuarioPerfil(Long id, UUID uuid, Long usuarioId, String idEmpleado, String urlFoto,
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

    public static UsuarioPerfil nuevo(Long usuarioId) {
        return new UsuarioPerfil(null, null, usuarioId, null, null, null, null,
                null, null, null, null, null, null, null, null);
    }

    public void actualizarDatosPersonales(String cedula, String tipoDocumento, LocalDate fechaNacimiento,
                                           String telefono, String direccion,
                                           String contactoEmergencia, String telefonoEmergencia) {
        this.cedula = cedula;
        this.tipoDocumento = tipoDocumento;
        this.fechaNacimiento = fechaNacimiento;
        this.telefono = telefono;
        this.direccion = direccion;
        this.contactoEmergencia = contactoEmergencia;
        this.telefonoEmergencia = telefonoEmergencia;
    }

    public void asignarCuadrilla(Long cuadrillaId, String zona) {
        this.cuadrillaId = cuadrillaId;
        this.zona = zona;
    }

    // --- Getters ---
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