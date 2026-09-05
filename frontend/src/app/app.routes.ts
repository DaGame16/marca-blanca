import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { ListaTareas } from './features/tareas/pages/lista-tareas/lista-tareas';
import { DetalleTarea } from './features/tareas/pages/detalle-tarea/detalle-tarea';
import { LoginComponent } from './features/auth/login/login.component';
import { ModulosAdminComponent } from './features/admin/pages/modulos-admin/modulos-admin.component';

export const routes: Routes = [
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
    path: 'tareas',
    component: ListaTareas,
    canActivate: [authGuard],
  },
  {
    path: 'tareas/:id',
    component: DetalleTarea,
    canActivate: [authGuard],
  },
  {
    path: '',
    redirectTo: 'tareas',
    pathMatch: 'full',
  },
];