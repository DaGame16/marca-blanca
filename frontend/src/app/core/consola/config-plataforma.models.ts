// Config global de la plataforma administrada desde la consola.

// Espejo de correo.domain.ConfiguracionSmtp
export interface ConfiguracionSmtp {
  id: number;
  uuid: string;
  remitenteNombre: string | null;
  remitenteCorreo: string;
  responderA: string | null;
  host: string;
  puerto: number;
  usuario: string | null;
  secretoRef: string | null;
  seguridad: string; // ninguna | starttls | ssl
  esActiva: boolean;
  creadoEn: string;
  actualizadoEn: string;
}

export interface ConfigCorreoPayload {
  remitenteNombre: string | null;
  remitenteCorreo: string;
  responderA: string | null;
  host: string;
  puerto: number;
  usuario: string | null;
  secretoRef: string | null;
  seguridad: string;
}

// Espejo de modulosempresa.domain.Modulo
export interface ModuloCatalogo {
  id: string;
  codigo: string;
  nombre: string;
  descripcion: string | null;
  precio: number;
  moneda: string;
}

export interface ModuloPayload {
  codigo: string;
  nombre: string;
  descripcion: string | null;
  precio: number;
  moneda: string;
}
