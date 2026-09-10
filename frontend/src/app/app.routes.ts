import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { moduloActivoGuard } from './core/guards/modulo-activo.guard';
import { HomeComponent } from './features/home/home.component';
import { LoginComponent } from './features/auth/login/login.component';
import { CambiarContrasenaComponent } from './features/auth/cambiar-contrasena/cambiar-contrasena.component';
import { RegistroEmpresaComponent } from './features/auth/registro/registro-empresa.component';
import { ModulosAdminComponent } from './features/admin/pages/modulos-admin/modulos-admin.component';
import { MisModulosComponent } from './features/empresas/pages/mis-modulos/mis-modulos.component';
import { MiMarcaComponent } from './features/empresas/pages/mi-marca/mi-marca.component';
import { SelectorTemaLoginComponent } from './features/empresas/pages/selector-tema-login/selector-tema-login.component';
import { ListaUsuariosComponent } from './features/usuarios/pages/lista-usuarios/lista-usuarios.component';
import { Pbx3cxDetalleComponent } from './features/3cx/pages/detalle/pbx-3cx-detalle.component';
import { Pbx3cxPanelComponent } from './features/3cx/pages/panel/pbx-3cx-panel.component';
import { ShellComponent } from './layout/shell.component';

export const routes: Routes = [
  { path: '', component: HomeComponent, pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'cambiar-contrasena', component: CambiarContrasenaComponent },
  { path: 'registro', component: RegistroEmpresaComponent },
  { path: 'admin/modulos', component: ModulosAdminComponent },
  // Consola de operacion de GuajiraNet (Opcion C: seccion aparte, lazy,
  // token y guard propios). No se tematiza por empresa.
  {
    path: 'consola',
    loadChildren: () => import('./features/consola/consola.routes').then((m) => m.CONSOLA_ROUTES),
  },
  // Paginas publicas de "conocer la solucion" (enlazadas desde el home,
  // antes de comprar/loguearse) -- no confundir con el panel del modulo ya
  // instalado, que vive dentro del Shell mas abajo.
  {
    path: 'modulos/omnicanal',
    loadComponent: () =>
      import('./features/omnicanal-liwa/pages/detalle/omnicanal-detalle.component').then((m) => m.OmnicanalDetalleComponent),
  },
  { path: 'modulos/pbx-3cx', component: Pbx3cxDetalleComponent },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      { path: 'mis-modulos', component: MisModulosComponent },
      { path: 'mi-marca', component: MiMarcaComponent },
      { path: 'tema-login', component: SelectorTemaLoginComponent },
      { path: 'usuarios', component: ListaUsuariosComponent },
      // Omnicanal quedo unificado en un solo modulo (omnicanal-liwa):
      // 'panel/omnicanal' y 'panel/omnicanal/liwa' apuntan al mismo panel
      // real (conversaciones, analisis IA, calidad, indicadores, asesores,
      // ads, casos). El placeholder viejo de features/omnicanal/ (sin
      // logica real) fue retirado -- ver README-DEPRECATED.md ahi.
      {
        path: 'panel/omnicanal/liwa',
        loadComponent: () =>
          import('./features/omnicanal-liwa/pages/panel/omnicanal-liwa-panel.component').then((m) => m.OmnicanalLiwaPanelComponent),
        canActivate: [moduloActivoGuard('omnicanal')],
      },
      { path: 'panel/pbx-3cx', component: Pbx3cxPanelComponent },
    ],
  },
  { path: '**', redirectTo: '' },
];
