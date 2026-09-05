import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { HomeComponent } from './features/home/home.component';
import { LoginComponent } from './features/auth/login/login.component';
import { ListaTareas } from './features/tareas/pages/lista-tareas/lista-tareas';
import { DetalleTarea } from './features/tareas/pages/detalle-tarea/detalle-tarea';
import { MisModulosComponent } from './features/empresas/pages/mis-modulos/mis-modulos.component';
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