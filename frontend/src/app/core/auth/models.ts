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
  debeCambiarContrasena: boolean;
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
  debeCambiarContrasena: boolean;
}

// Request para cambiar contraseña - coincide con backend CambiarContrasenaRequest
export interface CambiarContrasenaRequest {
  contrasenaActual: string;
  contrasenaNueva: string;
}

// Modelo de usuario para el frontend (derivado de la respuesta de login).
// "correo" no viene en LoginResponse/RefreshResponse (el backend solo manda
// el id) -- se guarda aparte, del propio formulario de login, porque es lo
// unico que tenemos para mostrar algo mas legible que un UUID en la barra
// superior. En un refresh de token se conserva el que ya habia.
export interface UserInfo {
  usuarioId: string;
  correo?: string;
}
