export interface Usuario {
  uuid: string;
  correo: string;
  nombreCompleto: string;
  activo: boolean;
}

export interface CrearUsuarioRequest {
  correo: string;
  contrasena: string;
  nombreCompleto: string;
}

export interface ActualizarUsuarioRequest {
  nombreCompleto: string;
}

export interface ActualizarPerfilRequest {
  cedula: string;
  tipoDocumento: string;
  telefono: string;
  direccion: string;
  contactoEmergencia: string;
  telefonoEmergencia: string;
}
