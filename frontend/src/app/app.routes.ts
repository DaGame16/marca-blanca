import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { HomeComponent } from './features/home/home.component';
import { LoginComponent } from './features/auth/login/login.component';
import { ListaTareas } from './features/tareas/pages/lista-tareas/lista-tareas';
import { DetalleTarea } from './features/tareas/pages/detalle-tarea/detalle-tarea';
import { ModulosAdminComponent } from './features/admin/pages/modulos-admin/modulos-admin.component';
import { MisModulosComponent } from './features/empresas/pages/mis-modulos/mis-modulos.component';
import { MiMarcaComponent } from './features/empresas/pages/mi-marca/mi-marca.component';
import { OmnicanalDetalleComponent } from './features/omnicanal/pages/detalle/omnicanal-detalle.component';
import { Pbx3cxDetalleComponent } from './features/3cx/pages/detalle/pbx-3cx-detalle.component';

export const routes: Routes = [
  {
    path: '',
    component: HomeComponent,
  },
  {
    path: 'login',
    component: LoginComponent,
  },
  {
    path: 'admin/modulos',
    component: ModulosAdminComponent,
    // TODO: Agregar guard de admin cuando se implemente sistema de roles
    // canActivate: [authGuard, adminGuard],
  },
  {
    path: 'modulos/omnicanal',
    component: OmnicanalDetalleComponent,
  },
  {
    path: 'modulos/pbx-3cx',
    component: Pbx3cxDetalleComponent,
  },
  {
    // Pantalla estilo "Apps" de Odoo: cada empresa activa/desactiva sus
    // propios modulos desde su propia sesion.
    path: 'mis-modulos',
    component: MisModulosComponent,
    canActivate: [authGuard],
  },
  {
    path: 'mi-marca',
    component: MiMarcaComponent,
    canActivate: [authGuard],
  },
  {
    path: 'tareas',
    component: ListaTareas,
    canActivate: [authGuard],
  },
  {
    path: 'tareas/:id',
    component: DetalleTarea,
    canActivate: [authGuard],
  },
];
