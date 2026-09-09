import { Routes } from '@angular/router';
import { operadorGuard, operadorConContrasenaTemporalGuard } from '../../core/consola/operador.guard';

/**
 * Seccion /consola -- cargada de forma diferida (loadChildren en app.routes.ts),
 * asi el codigo de la consola de operacion no entra en el bundle inicial de la
 * app de empresas.
 */
export const CONSOLA_ROUTES: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./login/consola-login.component').then((m) => m.ConsolaLoginComponent),
  },
  {
    path: 'cambiar-contrasena',
    canActivate: [operadorConContrasenaTemporalGuard],
    loadComponent: () =>
      import('./cambiar-contrasena/consola-cambiar-contrasena.component').then(
        (m) => m.ConsolaCambiarContrasenaComponent,
      ),
  },
  {
    path: '',
    canActivate: [operadorGuard],
    loadComponent: () =>
      import('./empresas/consola-empresas.component').then((m) => m.ConsolaEmpresasComponent),
  },
  {
    path: 'empresas/:id',
    canActivate: [operadorGuard],
    loadComponent: () =>
      import('./empresa/consola-empresa-detalle.component').then((m) => m.ConsolaEmpresaDetalleComponent),
  },
  { path: '**', redirectTo: '' },
];
