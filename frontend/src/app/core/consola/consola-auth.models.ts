// Contratos de /api/v1/consola/auth/** -- espejo de los DTO del modulo backend `consola`.

export interface ConsolaLoginRequest {
  correo: string;
  contrasena: string;
}

export interface ConsolaLoginResponse {
  operadorId: string;
  correo: string;
  rol: string;
  token: string;
  debeCambiarContrasena: boolean;
}

export interface ConsolaCambiarContrasenaRequest {
  contrasenaActual: string;
  contrasenaNueva: string;
}

/** Datos del operador que el frontend guarda tras el login (subconjunto de la respuesta). */
export interface OperadorEnSesion {
  operadorId: string;
  correo: string;
  rol: string;
}
