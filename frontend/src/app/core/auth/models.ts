// Request para login - coincide con backend LoginRequest
export interface LoginRequest {
  correo: string;
  contrasena: string;
  identificadorEmpresa: string;
}

// Response de login - coincide con backend LoginResponse
export interface LoginResponse {
  usuarioId: string;
  token: string;
  refreshToken: string;
}

// Request para renovar token - coincide con backend RefreshRequest
// (identificadorEmpresa es obligatorio en el backend: sin el, RenovarToken
// no puede establecer el ContextoEmpresaActual y falla)
export interface RefreshRequest {
  refreshToken: string;
  identificadorEmpresa: string;
}

// Response de refresh token - coincide con backend RefreshResponse
export interface RefreshResponse {
  usuarioId: string;
  token: string;
  refreshToken: string;
}

// Modelo de usuario para el frontend (derivado de la respuesta de login)
export interface UserInfo {
  usuarioId: string;
}
