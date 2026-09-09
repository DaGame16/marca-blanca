import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { ConsolaAuthService } from '../consola/consola-auth.service';

/**
 * Adjunta el token de OPERADOR a las peticiones de /api/v1/consola/**. Va antes
 * que authInterceptor (que ignora esas rutas), para que el token de tenant nunca
 * viaje a la consola ni al reves.
 */
export const consolaAuthInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.includes('/api/v1/consola/')) {
    return next(req);
  }

  const token = inject(ConsolaAuthService).getToken();
  if (!token) {
    return next(req);
  }

  return next(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};
