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

    private UsuarioPerfil(Builder b) {
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

    public static UsuarioPerfil nuevo(Long usuarioId) {
        return builder().usuarioId(usuarioId).build();
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

        public UsuarioPerfil build() {
            return new UsuarioPerfil(this);
        }
    }
}