import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../../../core/auth/auth.service';
import { RolService } from '../../data/rol.service';
import { PermisoService } from '../../data/permiso.service';
import { Rol } from '../../models/rol.model';
import { Permiso } from '../../models/permiso.model';
import { agruparPermisosPorModulo } from '../../models/modulo-permiso.util';

@Component({
  selector: 'app-lista-roles',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatIconModule,
    MatButtonModule,
    MatInputModule,
    MatFormFieldModule,
    MatProgressSpinnerModule,
    MatCheckboxModule,
    MatSnackBarModule,
  ],
  template: `
    <div class="roles-page">
      <header class="roles-header">
        <div>
          <h1>Roles y permisos</h1>
          <p>Crea roles y define que puede hacer cada uno.</p>
        </div>
        @if (puedeGestionar()) {
          <button mat-flat-button color="primary" (click)="alternarFormularioCreacion()">
            <mat-icon>{{ mostrarFormularioCreacion() ? 'close' : 'add' }}</mat-icon>
            {{ mostrarFormularioCreacion() ? 'Cancelar' : 'Nuevo rol' }}
          </button>
        }
      </header>

      @if (mostrarFormularioCreacion()) {
        <form class="crear-form" [formGroup]="formCrear" (ngSubmit)="crear()">
          <mat-form-field appearance="outline">
            <mat-label>Nombre</mat-label>
            <input matInput formControlName="nombre" />
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Descripción</mat-label>
            <input matInput formControlName="descripcion" />
          </mat-form-field>

          <button mat-flat-button color="primary" type="submit" [disabled]="formCrear.invalid || creando()">
            @if (creando()) {
              <mat-spinner diameter="18"></mat-spinner>
            } @else {
              Crear rol
            }
          </button>
        </form>
      }

      @if (cargando()) {
        <div class="state-container">
          <mat-spinner diameter="44"></mat-spinner>
          <p>Cargando roles...</p>
        </div>
      } @else if (error()) {
        <div class="state-container">
          <mat-icon color="warn">error_outline</mat-icon>
          <p>{{ error() }}</p>
          <button mat-stroked-button (click)="cargar()">Reintentar</button>
        </div>
      } @else if (roles().length === 0) {
        <div class="state-container">
          <mat-icon>shield_moon</mat-icon>
          <p>Todavía no hay roles creados.</p>
        </div>
      } @else {
        <div class="roles-tabla">
          @for (rol of roles(); track rol.uuid) {
            <div class="rol-card">
              <div class="rol-cabecera">
                <div class="rol-info">
                  @if (editandoUuid() === rol.uuid) {
                    <input class="input-inline" [(ngModel)]="nombreEnEdicion" [ngModelOptions]="{standalone: true}" placeholder="Nombre" />
                    <input class="input-inline" [(ngModel)]="descripcionEnEdicion" [ngModelOptions]="{standalone: true}" placeholder="Descripción" />
                  } @else {
                    <span class="rol-nombre">
                      {{ rol.nombre }}
                      @if (rol.esDelSistema) {
                        <span class="chip-sistema">Sistema</span>
                      }
                    </span>
                    <span class="rol-descripcion">{{ rol.descripcion }}</span>
                  }
                </div>
                @if (puedeGestionar() && !rol.esDelSistema) {
                  <div class="acciones">
                    @if (editandoUuid() === rol.uuid) {
                      <button mat-icon-button (click)="guardarEdicion(rol)" aria-label="Guardar">
                        <mat-icon>check</mat-icon>
                      </button>
                      <button mat-icon-button (click)="cancelarEdicion()" aria-label="Cancelar">
                        <mat-icon>close</mat-icon>
                      </button>
                    } @else {
                      <button mat-icon-button (click)="iniciarEdicion(rol)" aria-label="Editar">
                        <mat-icon>edit</mat-icon>
                      </button>
                      <button mat-icon-button (click)="eliminar(rol)" aria-label="Eliminar">
                        <mat-icon>delete</mat-icon>
                      </button>
                    }
                  </div>
                }
                <button mat-stroked-button (click)="alternarPermisos(rol)">
                  {{ permisosAbiertoUuid() === rol.uuid ? 'Ocultar permisos' : 'Permisos' }}
                </button>
              </div>

              @if (permisosAbiertoUuid() === rol.uuid) {
                <div class="permisos-panel">
                  @if (cargandoPermisos()) {
                    <mat-spinner diameter="28"></mat-spinner>
                  } @else {
                    @for (grupo of gruposDePermisos(); track grupo.modulo) {
                      <div class="permisos-grupo">
                        <span class="permisos-grupo-titulo">{{ grupo.tituloModulo }}</span>
                        @for (permiso of grupo.permisos; track permiso.uuid) {
                          <mat-checkbox
                            class="permiso-item"
                            [checked]="permisosDelRol().has(permiso.uuid)"
                            [disabled]="!puedeGestionar() || procesandoPermiso()"
                            (change)="alternarPermisoDeRol(rol, permiso, $event.checked)"
                          >
                            <span class="permiso-nombre">{{ permiso.descripcion }}</span>
                            <code class="permiso-codigo">{{ permiso.nombre }}</code>
                          </mat-checkbox>
                        }
                      </div>
                    }
                  }
                </div>
              }
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .roles-page {
      max-width: 1000px;
      margin: 0 auto;
      padding: 40px 24px 80px;
    }

    .roles-header {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      gap: 24px;
      flex-wrap: wrap;
      margin-bottom: 28px;
    }

    .roles-header h1 {
      font-size: 1.9rem;
      font-weight: 800;
      margin: 0 0 6px;
      color: #0f172a;
    }

    .roles-header p {
      margin: 0;
      color: #64748b;
    }

    .crear-form {
      display: flex;
      gap: 16px;
      flex-wrap: wrap;
      align-items: flex-start;
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 12px;
      padding: 20px;
      margin-bottom: 28px;
    }

    .crear-form mat-form-field {
      flex: 1 1 200px;
    }

    .crear-form button {
      height: 56px;
    }

    .state-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 16px;
      padding: 80px 24px;
      color: #64748b;
      text-align: center;
    }

    .roles-tabla {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .rol-card {
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 12px;
      overflow: hidden;
    }

    .rol-cabecera {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 16px 20px;
    }

    .rol-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 4px;
      min-width: 0;
    }

    .rol-nombre {
      font-weight: 700;
      color: #0f172a;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .rol-descripcion {
      color: #64748b;
      font-size: 0.88rem;
    }

    .chip-sistema {
      font-size: 0.7rem;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.04em;
      color: #7c3aed;
      background: #f3e8ff;
      border-radius: 999px;
      padding: 2px 8px;
    }

    .acciones {
      display: flex;
      gap: 4px;
    }

    .input-inline {
      width: 100%;
      padding: 6px 8px;
      margin-bottom: 4px;
      border: 1px solid #cbd5e1;
      border-radius: 6px;
      font-size: 0.9rem;
    }

    .permisos-panel {
      display: flex;
      flex-direction: column;
      gap: 18px;
      padding: 16px 20px 20px;
      background: #f8fafc;
      border-top: 1px solid #f1f5f9;
    }

    .permisos-grupo {
      display: flex;
      flex-direction: column;
      gap: 6px;
    }

    .permisos-grupo-titulo {
      font-size: 0.72rem;
      font-weight: 800;
      text-transform: uppercase;
      letter-spacing: 0.06em;
      color: #64748b;
      margin-bottom: 2px;
    }

    .permiso-item {
      display: block;
    }

    .permiso-nombre {
      color: #1e293b;
      font-size: 0.92rem;
    }

    .permiso-codigo {
      display: block;
      color: #94a3b8;
      font-size: 0.72rem;
      font-family: 'SFMono-Regular', Consolas, monospace;
      margin-top: 1px;
    }

    @media (max-width: 720px) {
      .rol-cabecera {
        flex-wrap: wrap;
      }
    }
  `],
})
export class ListaRolesComponent implements OnInit {
  private readonly rolService = inject(RolService);
  private readonly permisoService = inject(PermisoService);
  private readonly authService = inject(AuthService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly fb = inject(FormBuilder);

  readonly roles = signal<Rol[]>([]);
  readonly cargando = signal(false);
  readonly error = signal<string | null>(null);

  readonly mostrarFormularioCreacion = signal(false);
  readonly creando = signal(false);

  readonly editandoUuid = signal<string | null>(null);
  nombreEnEdicion = '';
  descripcionEnEdicion = '';

  readonly permisosAbiertoUuid = signal<string | null>(null);
  readonly cargandoPermisos = signal(false);
  readonly procesandoPermiso = signal(false);
  readonly catalogoPermisos = signal<Permiso[]>([]);
  readonly gruposDePermisos = computed(() => agruparPermisosPorModulo(this.catalogoPermisos()));
  readonly permisosDelRol = signal<Set<string>>(new Set());

  readonly formCrear = this.fb.nonNullable.group({
    nombre: ['', Validators.required],
    descripcion: [''],
  });

  ngOnInit(): void {
    this.cargar();
  }

  puedeGestionar(): boolean {
    return this.authService.tienePermiso('roles:gestionar');
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.rolService.listar().subscribe({
      next: (roles) => {
        this.roles.set(roles);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No pudimos cargar los roles. Intenta de nuevo en unos segundos.');
        this.cargando.set(false);
      },
    });
  }

  alternarFormularioCreacion(): void {
    this.mostrarFormularioCreacion.set(!this.mostrarFormularioCreacion());
    if (!this.mostrarFormularioCreacion()) {
      this.formCrear.reset();
    }
  }

  crear(): void {
    if (this.formCrear.invalid) {
      return;
    }
    this.creando.set(true);
    this.rolService.crear(this.formCrear.getRawValue()).subscribe({
      next: (rol) => {
        this.roles.set([...this.roles(), rol]);
        this.formCrear.reset();
        this.mostrarFormularioCreacion.set(false);
        this.creando.set(false);
        this.snackBar.open('Rol creado', 'Cerrar', { duration: 2500 });
      },
      error: () => {
        this.creando.set(false);
        this.snackBar.open('No se pudo crear el rol. Verifica los datos e intenta de nuevo.', 'Cerrar', {
          duration: 4000,
        });
      },
    });
  }

  iniciarEdicion(rol: Rol): void {
    this.editandoUuid.set(rol.uuid);
    this.nombreEnEdicion = rol.nombre;
    this.descripcionEnEdicion = rol.descripcion;
  }

  cancelarEdicion(): void {
    this.editandoUuid.set(null);
  }

  guardarEdicion(rol: Rol): void {
    const nombre = this.nombreEnEdicion.trim();
    if (!nombre) {
      return;
    }
    this.rolService.actualizar(rol.uuid, { nombre, descripcion: this.descripcionEnEdicion }).subscribe({
      next: (actualizado) => {
        this.roles.set(this.roles().map((r) => (r.uuid === rol.uuid ? actualizado : r)));
        this.editandoUuid.set(null);
        this.snackBar.open('Rol actualizado', 'Cerrar', { duration: 2500 });
      },
      error: () => {
        this.snackBar.open('No se pudo actualizar el rol. Intenta de nuevo.', 'Cerrar', { duration: 4000 });
      },
    });
  }

  eliminar(rol: Rol): void {
    this.rolService.eliminar(rol.uuid).subscribe({
      next: () => {
        this.roles.set(this.roles().filter((r) => r.uuid !== rol.uuid));
        this.snackBar.open('Rol eliminado', 'Cerrar', { duration: 2500 });
      },
      error: () => {
        this.snackBar.open(
          'No se pudo eliminar el rol (puede tener usuarios asignados). Intenta de nuevo.',
          'Cerrar',
          { duration: 4500 }
        );
      },
    });
  }

  alternarPermisos(rol: Rol): void {
    if (this.permisosAbiertoUuid() === rol.uuid) {
      this.permisosAbiertoUuid.set(null);
      return;
    }
    this.permisosAbiertoUuid.set(rol.uuid);
    this.cargarPermisosDeRol(rol);
  }

  private cargarPermisosDeRol(rol: Rol): void {
    this.cargandoPermisos.set(true);

    if (this.catalogoPermisos().length === 0) {
      this.permisoService.listar().subscribe({
        next: (permisos) => this.catalogoPermisos.set(permisos),
      });
    }

    this.rolService.listarPermisos(rol.uuid).subscribe({
      next: (permisos) => {
        this.permisosDelRol.set(new Set(permisos.map((p) => p.uuid)));
        this.cargandoPermisos.set(false);
      },
      error: () => {
        this.cargandoPermisos.set(false);
        this.snackBar.open('No se pudieron cargar los permisos del rol.', 'Cerrar', { duration: 4000 });
      },
    });
  }

  alternarPermisoDeRol(rol: Rol, permiso: Permiso, concedido: boolean): void {
    this.procesandoPermiso.set(true);
    const observable = concedido
      ? this.rolService.asignarPermiso(rol.uuid, permiso.uuid)
      : this.rolService.quitarPermiso(rol.uuid, permiso.uuid);

    observable.subscribe({
      next: () => {
        const actuales = new Set(this.permisosDelRol());
        if (concedido) {
          actuales.add(permiso.uuid);
        } else {
          actuales.delete(permiso.uuid);
        }
        this.permisosDelRol.set(actuales);
        this.procesandoPermiso.set(false);
      },
      error: () => {
        this.procesandoPermiso.set(false);
        this.snackBar.open('No se pudo actualizar el permiso. Intenta de nuevo.', 'Cerrar', { duration: 4000 });
      },
    });
  }
}
