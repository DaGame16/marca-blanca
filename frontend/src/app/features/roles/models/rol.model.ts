export interface Rol {
  uuid: string;
  nombre: string;
  descripcion: string;
  esDelSistema: boolean;
}

export interface CrearRolRequest {
  nombre: string;
  descripcion: string;
}

export interface ActualizarRolRequest {
  nombre: string;
  descripcion: string;
}
