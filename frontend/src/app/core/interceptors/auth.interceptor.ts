import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../auth/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  const authReq = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  // PENDIENTE (documento TO-BE, sección final): el AS-IS de Next.js oculta x-api-key
  // mediante un proxy server-side. Angular, como SPA sin servidor propio, no tiene un
  // lugar equivalente. No resolver aquí sin definición conjunta con el equipo de backend.

  return next(authReq);
};