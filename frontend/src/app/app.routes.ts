import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { HomeComponent } from './features/home/home.component';
import { LoginComponent } from './features/auth/login/login.component';
import { CambiarContrasenaComponent } from './features/auth/cambiar-contrasena/cambiar-contrasena.component';
import { RegistroEmpresaComponent } from './features/auth/registro/registro-empresa.component';
import { ListaTareas } from './features/tareas/pages/lista-tareas/lista-tareas';
import { DetalleTarea } from './features/tareas/pages/detalle-tarea/detalle-tarea';
import { ModulosAdminComponent } from './features/admin/pages/modulos-admin/modulos-admin.component';
import { MisModulosComponent } from './features/empresas/pages/mis-modulos/mis-modulos.component';
import { MiMarcaComponent } from './features/empresas/pages/mi-marca/mi-marca.component';
import { SelectorTemaLoginComponent } from './features/empresas/pages/selector-tema-login/selector-tema-login.component';
import { ListaUsuariosComponent } from './features/usuarios/pages/lista-usuarios/lista-usuarios.component';
import { OmnicanalDetalleComponent } from './features/omnicanal/pages/detalle/omnicanal-detalle.component';
import { Pbx3cxDetalleComponent } from './features/3cx/pages/detalle/pbx-3cx-detalle.component';
import { ShellComponent } from './layout/shell.component';

export const routes: Routes = [
  { path: '', component: HomeComponent, pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'cambiar-contrasena', component: CambiarContrasenaComponent },
  { path: 'registro', component: RegistroEmpresaComponent },
  { path: 'admin/modulos', component: ModulosAdminComponent },
  { path: 'modulos/omnicanal', component: OmnicanalDetalleComponent },
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
      { path: 'tareas', component: ListaTareas },
      { path: 'tareas/:id', component: DetalleTarea },
    ],
  },
  { path: '**', redirectTo: '' },
];
