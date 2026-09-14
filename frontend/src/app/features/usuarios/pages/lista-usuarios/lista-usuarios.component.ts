import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatChipsModule } from '@angular/material/chips';
import { UsuarioService } from '../../data/usuario.service';
import { ActualizarPerfilRequest, Usuario } from '../../models/usuario.model';
import { AuthService } from '../../../../core/auth/auth.service';
import { AsignacionUsuarioRolService } from '../../../roles/data/asignacion-usuario-rol.service';
import { RolService } from '../../../roles/data/rol.service';
import { Rol } from '../../../roles/models/rol.model';

@Component({
  selector: 'app-lista-usuarios',
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
    MatSlideToggleModule,
    MatSnackBarModule,
    MatChipsModule,
  ],
  template: `
    <div class="usuarios-page">
      <header class="usuarios-header">
        <div>
          <h1>Usuarios</h1>
          <p>Crea, edita y activa o desactiva los usuarios de tu empresa.</p>
        </div>
        @if (authService.tienePermiso('usuarios:crear')) {
          <button mat-flat-button color="primary" (click)="alternarFormularioCreacion()">
            <mat-icon>{{ mostrarFormularioCreacion() ? 'close' : 'person_add' }}</mat-icon>
            {{ mostrarFormularioCreacion() ? 'Cancelar' : 'Nuevo usuario' }}
          </button>
        }
      </header>

      @if (mostrarFormularioCreacion()) {
        <form class="crear-form" [formGroup]="formCrear" (ngSubmit)="crear()">
          <mat-form-field appearance="outline">
            <mat-label>Nombre completo</mat-label>
            <input matInput formControlName="nombreCompleto" />
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Correo</mat-label>
            <input matInput type="email" formControlName="correo" />
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Contraseña</mat-label>
            <input matInput type="password" formControlName="contrasena" />
          </mat-form-field>

          <button mat-flat-button color="primary" type="submit" [disabled]="formCrear.invalid || creando()">
            @if (creando()) {
              <mat-spinner diameter="18"></mat-spinner>
            } @else {
              Crear usuario
            }
          </button>
        </form>
      }

      @if (cargando()) {
        <div class="state-container">
          <mat-spinner diameter="44"></mat-spinner>
          <p>Cargando usuarios...</p>
        </div>
      } @else if (error()) {
        <div class="state-container">
          <mat-icon color="warn">error_outline</mat-icon>
          <p>{{ error() }}</p>
          <button mat-stroked-button (click)="cargar()">Reintentar</button>
        </div>
      } @else if (usuarios().length === 0) {
        <div class="state-container">
          <mat-icon>group_off</mat-icon>
          <p>Todavía no hay usuarios registrados.</p>
        </div>
      } @else {
        <div class="usuarios-tabla">
          <div class="fila fila-encabezado">
            <span>Nombre</span>
            <span>Correo</span>
            <span>Estado</span>
            <span>Acciones</span>
          </div>

          @for (usuario of usuarios(); track usuario.uuid) {
            <div class="fila">
              <span class="celda-nombre">
                @if (editandoUuid() === usuario.uuid) {
                  <input class="input-inline" [(ngModel)]="nombreEnEdicion" [ngModelOptions]="{standalone: true}" />
                } @else {
                  {{ usuario.nombreCompleto }}
                }
              </span>
              <span>{{ usuario.correo }}</span>
              <span>
                <mat-slide-toggle
                  [checked]="usuario.activo"
                  [disabled]="procesandoUuid() === usuario.uuid || !authService.tienePermiso(usuario.activo ? 'usuarios:desactivar' : 'usuarios:activar')"
                  (change)="alternarActivo(usuario)"
                >
                  {{ usuario.activo ? 'Activo' : 'Inactivo' }}
                </mat-slide-toggle>
              </span>
              <span class="acciones">
                @if (editandoUuid() === usuario.uuid) {
                  <button mat-icon-button (click)="guardarEdicion(usuario)" [disabled]="procesandoUuid() === usuario.uuid" aria-label="Guardar">
                    <mat-icon>check</mat-icon>
                  </button>
                  <button mat-icon-button (click)="cancelarEdicion()" aria-label="Cancelar">
                    <mat-icon>close</mat-icon>
                  </button>
                } @else {
                  @if (authService.tienePermiso('usuarios:editar')) {
                    <button mat-icon-button (click)="iniciarEdicion(usuario)" aria-label="Editar nombre">
                      <mat-icon>edit</mat-icon>
                    </button>
                    <button mat-icon-button (click)="alternarPerfil(usuario)" aria-label="Editar perfil">
                      <mat-icon>badge</mat-icon>
                    </button>
                  }
                  @if (puedeVerRoles()) {
                    <button mat-icon-button (click)="alternarRoles(usuario)" aria-label="Roles">
                      <mat-icon>shield_moon</mat-icon>
                    </button>
                  }
                }
              </span>
            </div>

            @if (rolesAbiertoUuid() === usuario.uuid) {
              <div class="roles-panel">
                @if (cargandoRoles()) {
                  <mat-spinner diameter="28"></mat-spinner>
                } @else {
                  <mat-chip-set class="roles-chips">
                    @for (rol of rolesDelUsuario(); track rol.uuid) {
                      <mat-chip [removable]="puedeGestionarRoles()" (removed)="quitarRol(usuario, rol)">
                        {{ rol.nombre }}
                        @if (puedeGestionarRoles()) {
                          <button matChipRemove aria-label="Quitar rol">
                            <mat-icon>cancel</mat-icon>
                          </button>
                        }
                      </mat-chip>
                    } @empty {
                      <span class="sin-roles">Sin roles asignados.</span>
                    }
                  </mat-chip-set>

                  @if (puedeGestionarRoles()) {
                    <div class="asignar-rol">
                      <mat-form-field appearance="outline">
                        <mat-label>Asignar rol</mat-label>
                        <select matNativeControl [(ngModel)]="rolSeleccionadoUuid" [ngModelOptions]="{standalone: true}">
                          <option value="" disabled>Selecciona un rol</option>
                          @for (rol of rolesDisponiblesParaAsignar(); track rol.uuid) {
                            <option [value]="rol.uuid">{{ rol.nombre }}</option>
                          }
                        </select>
                      </mat-form-field>
                      <button mat-stroked-button (click)="asignarRol(usuario)" [disabled]="!rolSeleccionadoUuid">
                        Asignar
                      </button>
                    </div>
                  }
                }
              </div>
            }

            @if (perfilAbiertoUuid() === usuario.uuid) {
              <div class="perfil-panel">
                <form [formGroup]="formPerfil" (ngSubmit)="guardarPerfil(usuario)">
                  <mat-form-field appearance="outline">
                    <mat-label>Cédula</mat-label>
                    <input matInput formControlName="cedula" />
                  </mat-form-field>
                  <mat-form-field appearance="outline">
                    <mat-label>Tipo de documento</mat-label>
                    <input matInput formControlName="tipoDocumento" placeholder="CC, CE, NIT..." />
                  </mat-form-field>
                  <mat-form-field appearance="outline">
                    <mat-label>Teléfono</mat-label>
                    <input matInput formControlName="telefono" />
                  </mat-form-field>
                  <mat-form-field appearance="outline">
                    <mat-label>Dirección</mat-label>
                    <input matInput formControlName="direccion" />
                  </mat-form-field>
                  <mat-form-field appearance="outline">
                    <mat-label>Contacto de emergencia</mat-label>
                    <input matInput formControlName="contactoEmergencia" />
                  </mat-form-field>
                  <mat-form-field appearance="outline">
                    <mat-label>Teléfono de emergencia</mat-label>
                    <input matInput formControlName="telefonoEmergencia" />
                  </mat-form-field>

                  <div class="perfil-acciones">
                    <button mat-flat-button color="primary" type="submit" [disabled]="guardandoPerfil()">
                      @if (guardandoPerfil()) {
                        <mat-spinner diameter="18"></mat-spinner>
                      } @else {
                        Guardar perfil
                      }
                    </button>
                    <button mat-stroked-button type="button" (click)="cerrarPerfil()">Cerrar</button>
                  </div>
                </form>
              </div>
            }
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .usuarios-page {
      max-width: 1000px;
      margin: 0 auto;
      padding: 40px 24px 80px;
    }

    .usuarios-header {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      gap: 24px;
      flex-wrap: wrap;
      margin-bottom: 28px;
    }

    .usuarios-header h1 {
      font-size: 1.9rem;
      font-weight: 800;
      margin: 0 0 6px;
      color: #0f172a;
    }

    .usuarios-header p {
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

    .usuarios-tabla {
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 12px;
      overflow: hidden;
    }

    .fila {
      display: grid;
      grid-template-columns: 1.2fr 1.4fr 1fr auto;
      align-items: center;
      gap: 16px;
      padding: 14px 20px;
      border-bottom: 1px solid #f1f5f9;
    }

    .fila:last-child {
      border-bottom: none;
    }

    .fila-encabezado {
      background: #f8fafc;
      font-size: 0.8rem;
      font-weight: 700;
      color: #64748b;
      text-transform: uppercase;
      letter-spacing: 0.04em;
    }

    .input-inline {
      width: 100%;
      padding: 6px 8px;
      border: 1px solid #cbd5e1;
      border-radius: 6px;
      font-size: 0.9rem;
    }

    .acciones {
      display: flex;
      justify-content: flex-end;
      gap: 4px;
    }

    .perfil-panel {
      padding: 16px 20px 20px;
      background: #f8fafc;
      border-bottom: 1px solid #f1f5f9;
    }

    .roles-panel {
      padding: 16px 20px 20px;
      background: #f8fafc;
      border-bottom: 1px solid #f1f5f9;
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .roles-chips {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }

    .sin-roles {
      color: #94a3b8;
      font-size: 0.88rem;
    }

    .asignar-rol {
      display: flex;
      gap: 12px;
      align-items: center;
      flex-wrap: wrap;
    }

    .asignar-rol mat-form-field {
      flex: 1 1 220px;
    }

    .perfil-panel form {
      display: flex;
      flex-wrap: wrap;
      gap: 14px;
    }

    .perfil-panel mat-form-field {
      flex: 1 1 220px;
    }

    .perfil-acciones {
      display: flex;
      gap: 10px;
      align-items: center;
      width: 100%;
    }

    @media (max-width: 720px) {
      .fila {
        grid-template-columns: 1fr;
        gap: 6px;
      }

      .fila-encabezado {
        display: none;
      }
    }
  `],
})
export class ListaUsuariosComponent implements OnInit {
  private readonly usuarioService = inject(UsuarioService);
  protected readonly authService = inject(AuthService);
  private readonly asignacionRolService = inject(AsignacionUsuarioRolService);
  private readonly rolService = inject(RolService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly fb = inject(FormBuilder);

  readonly usuarios = signal<Usuario[]>([]);
  readonly cargando = signal(false);
  readonly error = signal<string | null>(null);
  readonly procesandoUuid = signal<string | null>(null);

  readonly mostrarFormularioCreacion = signal(false);
  readonly creando = signal(false);

  readonly editandoUuid = signal<string | null>(null);
  nombreEnEdicion = '';

  readonly perfilAbiertoUuid = signal<string | null>(null);
  readonly guardandoPerfil = signal(false);

  readonly rolesAbiertoUuid = signal<string | null>(null);
  readonly cargandoRoles = signal(false);
  readonly rolesDelUsuario = signal<Rol[]>([]);
  readonly catalogoRoles = signal<Rol[]>([]);
  rolSeleccionadoUuid = '';

  readonly formCrear = this.fb.nonNullable.group({
    nombreCompleto: ['', Validators.required],
    correo: ['', [Validators.required, Validators.email]],
    contrasena: ['', [Validators.required, Validators.minLength(8)]],
  });

  readonly formPerfil = this.fb.nonNullable.group({
    cedula: [''],
    tipoDocumento: [''],
    telefono: [''],
    direccion: [''],
    contactoEmergencia: [''],
    telefonoEmergencia: [''],
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.usuarioService.listar().subscribe({
      next: (usuarios: Usuario[]) => {
        this.usuarios.set(usuarios);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No pudimos cargar los usuarios. Intenta de nuevo en unos segundos.');
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
    this.usuarioService.crear(this.formCrear.getRawValue()).subscribe({
      next: (usuario: Usuario) => {
        this.usuarios.set([...this.usuarios(), usuario]);
        this.formCrear.reset();
        this.mostrarFormularioCreacion.set(false);
        this.creando.set(false);
        this.snackBar.open('Usuario creado', 'Cerrar', { duration: 2500 });
      },
      error: () => {
        this.creando.set(false);
        this.snackBar.open('No se pudo crear el usuario. Verifica los datos e intenta de nuevo.', 'Cerrar', {
          duration: 4000,
        });
      },
    });
  }

  alternarActivo(usuario: Usuario): void {
    this.procesandoUuid.set(usuario.uuid);
    const observable = usuario.activo
      ? this.usuarioService.desactivar(usuario.uuid)
      : this.usuarioService.activar(usuario.uuid);

    observable.subscribe({
      next: () => {
        this.usuarios.set(
          this.usuarios().map((u) => (u.uuid === usuario.uuid ? { ...u, activo: !u.activo } : u))
        );
        this.procesandoUuid.set(null);
        this.snackBar.open(usuario.activo ? 'Usuario desactivado' : 'Usuario activado', 'Cerrar', {
          duration: 2500,
        });
      },
      error: () => {
        this.procesandoUuid.set(null);
        this.snackBar.open('No se pudo completar la acción. Intenta de nuevo.', 'Cerrar', { duration: 4000 });
      },
    });
  }

  iniciarEdicion(usuario: Usuario): void {
    this.editandoUuid.set(usuario.uuid);
    this.nombreEnEdicion = usuario.nombreCompleto;
  }

  cancelarEdicion(): void {
    this.editandoUuid.set(null);
    this.nombreEnEdicion = '';
  }

  guardarEdicion(usuario: Usuario): void {
    const nombreCompleto = this.nombreEnEdicion.trim();
    if (!nombreCompleto) {
      return;
    }
    this.procesandoUuid.set(usuario.uuid);
    this.usuarioService.actualizar(usuario.uuid, { nombreCompleto }).subscribe({
      next: (actualizado: Usuario) => {
        this.usuarios.set(this.usuarios().map((u) => (u.uuid === usuario.uuid ? actualizado : u)));
        this.procesandoUuid.set(null);
        this.editandoUuid.set(null);
        this.snackBar.open('Nombre actualizado', 'Cerrar', { duration: 2500 });
      },
      error: () => {
        this.procesandoUuid.set(null);
        this.snackBar.open('No se pudo actualizar el nombre. Intenta de nuevo.', 'Cerrar', { duration: 4000 });
      },
    });
  }

  alternarPerfil(usuario: Usuario): void {
    if (this.perfilAbiertoUuid() === usuario.uuid) {
      this.cerrarPerfil();
      return;
    }
    this.perfilAbiertoUuid.set(usuario.uuid);
    this.formPerfil.reset();
  }

  cerrarPerfil(): void {
    this.perfilAbiertoUuid.set(null);
    this.formPerfil.reset();
  }

  guardarPerfil(usuario: Usuario): void {
    this.guardandoPerfil.set(true);
    const request: ActualizarPerfilRequest = this.formPerfil.getRawValue();
    this.usuarioService.actualizarPerfil(usuario.uuid, request).subscribe({
      next: () => {
        this.guardandoPerfil.set(false);
        this.cerrarPerfil();
        this.snackBar.open('Perfil actualizado', 'Cerrar', { duration: 2500 });
      },
      error: () => {
        this.guardandoPerfil.set(false);
        this.snackBar.open('No se pudo actualizar el perfil. Intenta de nuevo.', 'Cerrar', { duration: 4000 });
      },
    });
  }

  puedeVerRoles(): boolean {
    return this.authService.tienePermiso('roles:leer');
  }

  puedeGestionarRoles(): boolean {
    return this.authService.tienePermiso('roles:gestionar');
  }

  rolesDisponiblesParaAsignar(): Rol[] {
    const asignados = new Set(this.rolesDelUsuario().map((r) => r.uuid));
    return this.catalogoRoles().filter((r) => !asignados.has(r.uuid));
  }

  alternarRoles(usuario: Usuario): void {
    if (this.rolesAbiertoUuid() === usuario.uuid) {
      this.rolesAbiertoUuid.set(null);
      return;
    }
    this.rolesAbiertoUuid.set(usuario.uuid);
    this.cargarRolesDeUsuario(usuario);
  }

  private cargarRolesDeUsuario(usuario: Usuario): void {
    this.cargandoRoles.set(true);
    this.rolSeleccionadoUuid = '';

    if (this.catalogoRoles().length === 0 && this.puedeGestionarRoles()) {
      this.rolService.listar().subscribe({ next: (roles) => this.catalogoRoles.set(roles) });
    }

    this.asignacionRolService.listarRolesDeUsuario(usuario.uuid).subscribe({
      next: (roles) => {
        this.rolesDelUsuario.set(roles);
        this.cargandoRoles.set(false);
      },
      error: () => {
        this.cargandoRoles.set(false);
        this.snackBar.open('No se pudieron cargar los roles del usuario.', 'Cerrar', { duration: 4000 });
      },
    });
  }

  asignarRol(usuario: Usuario): void {
    if (!this.rolSeleccionadoUuid) {
      return;
    }
    this.asignacionRolService.asignarRol(usuario.uuid, this.rolSeleccionadoUuid).subscribe({
      next: () => {
        this.rolSeleccionadoUuid = '';
        this.cargarRolesDeUsuario(usuario);
        this.snackBar.open('Rol asignado', 'Cerrar', { duration: 2500 });
      },
      error: () => {
        this.snackBar.open('No se pudo asignar el rol. Intenta de nuevo.', 'Cerrar', { duration: 4000 });
      },
    });
  }

  quitarRol(usuario: Usuario, rol: Rol): void {
    this.asignacionRolService.quitarRol(usuario.uuid, rol.uuid).subscribe({
      next: () => {
        this.rolesDelUsuario.set(this.rolesDelUsuario().filter((r) => r.uuid !== rol.uuid));
        this.snackBar.open('Rol quitado', 'Cerrar', { duration: 2500 });
      },
      error: () => {
        this.snackBar.open('No se pudo quitar el rol. Intenta de nuevo.', 'Cerrar', { duration: 4000 });
      },
    });
  }
}
