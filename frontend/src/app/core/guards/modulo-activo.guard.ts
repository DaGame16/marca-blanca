import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { MisModulosService } from '../../features/empresas/pages/mis-modulos/mis-modulos.service';

/**
 * Protege rutas de un panel de modulo (ej. panel/omnicanal) verificando que
 * la empresa autenticada lo tenga activo (GET /mi-empresa/modulos, el mismo
 * endpoint que usa la pantalla "Mis modulos"). Si no esta activo -- o no se
 * pudo confirmar por un error de red -- se redirige a /mis-modulos en vez de
 * dejar pasar por defecto.
 *
 * Uso: canActivate: [moduloActivoGuard('omnicanal')]
 */
export function moduloActivoGuard(codigoModulo: string): CanActivateFn {
  return () => {
    const misModulosService = inject(MisModulosService);
    const router = inject(Router);

    return misModulosService.listar().pipe(
      map((modulos) => {
        const activo = modulos.some((m) => m.codigo === codigoModulo && m.activo);
        return activo ? true : router.parseUrl('/mis-modulos');
      }),
      catchError(() => of(router.parseUrl('/mis-modulos')))
    );
  };
}
