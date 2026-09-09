import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { ConsolaAuthService } from './consola-auth.service';

/**
 * Protege la seccion /consola. Sin sesion de operador -> a /consola/login.
 * Con contrasena temporal pendiente -> a /consola/cambiar-contrasena (el backend
 * rechaza cualquier otra ruta con 403 hasta que se cambie).
 */
export const operadorGuard: CanActivateFn = () => {
  const consolaAuth = inject(ConsolaAuthService);
  const router = inject(Router);

  if (!consolaAuth.estaAutenticado()) {
    return router.parseUrl('/consola/login');
  }
  if (consolaAuth.debeCambiarContrasena()) {
    return router.parseUrl('/consola/cambiar-contrasena');
  }
  return true;
};

/** Igual que operadorGuard pero permite quedarse en /consola/cambiar-contrasena. */
export const operadorConContrasenaTemporalGuard: CanActivateFn = () => {
  const consolaAuth = inject(ConsolaAuthService);
  const router = inject(Router);

  if (!consolaAuth.estaAutenticado()) {
    return router.parseUrl('/consola/login');
  }
  return true;
};
