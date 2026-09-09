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
  // La clave en si NUNCA viaja de vuelta -- solo si ya hay una guardada,
  // para poder avisar en la UI si falta configurarla.
  claveConfigurada: boolean;
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
  // Clave SMTP en texto plano -- viaja solo en este request (HTTPS + sesion
  // de operador). null/omitido en una edicion = "no cambiarla".
  clave: string | null;
}

export interface ResultadoPruebaCorreo {
  enviado: boolean;
  error?: string;
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
