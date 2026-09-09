import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../auth/auth.service';

// Estas llamadas son publicas (permitAll en el backend) y no necesitan el
// token para nada. Mandarlo igual es innecesario y, si ese token quedo con
// la contrasena temporal pendiente (un login anterior sin completar), el
// backend lo rechaza -- incluso siendo una ruta publica -- y la pantalla de
// login termina sin poder pintar el logo/colores de la empresa. Ver
// JwtAuthFilter.esRutaQueRequiereContrasenaDefinitiva (mismo problema,
// resuelto tambien del lado del servidor).
function esRutaPublica(url: string): boolean {
  return (
    url.includes('/auth/login') ||
    url.includes('/auth/identificador-empresa') ||
    url.includes('/registro/') ||
    /\/empresas\/[^/]+\/marca(\?|$)/.test(url)
  );
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // La consola de operacion usa su propio token (consolaAuthInterceptor). El
  // token de tenant nunca debe viajar a /api/v1/consola/**.
  if (req.url.includes('/api/v1/consola/')) {
    return next(req);
  }

  const authService = inject(AuthService);
  const token = !esRutaPublica(req.url) ? authService.getToken() : null;

  const authReq = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  // PENDIENTE (documento TO-BE, sección final): el AS-IS de Next.js oculta x-api-key
  // mediante un proxy server-side. Angular, como SPA sin servidor propio, no tiene un
  // lugar equivalente. No resolver aquí sin definición conjunta con el equipo de backend.

  return next(authReq);
};