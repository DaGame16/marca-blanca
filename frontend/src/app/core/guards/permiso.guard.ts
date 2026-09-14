import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';

/**
 * Protege una ruta segun el claim "permisos" del JWT (ver AuthService.tienePermiso).
 * Si el usuario no lo tiene, redirige a home en vez de dejar pasar por defecto.
 *
 * Uso: canActivate: [permisoGuard('roles:leer')]
 */
export function permisoGuard(permiso: string): CanActivateFn {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    return authService.tienePermiso(permiso) ? true : router.parseUrl('/');
  };
}
