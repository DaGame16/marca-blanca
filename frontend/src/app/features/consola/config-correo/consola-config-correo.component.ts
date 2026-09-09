import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ConsolaConfigService } from '../../../core/consola/consola-config.service';
import { ConfiguracionSmtp } from '../../../core/consola/config-plataforma.models';
import { ConsolaNavComponent } from '../nav/consola-nav.component';

const SEGURIDADES = ['ninguna', 'starttls', 'ssl'];

/**
 * CRUD de la configuración SMTP de la plataforma. Puede haber varias filas pero
 * solo una activa a la vez. La clave SMTP no se administra acá: `secretoRef`
 * apunta a dónde vive (variable de entorno / vault).
 */
@Component({
  selector: 'app-consola-config-correo',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatSelectModule,
    MatSnackBarModule,
    ConsolaNavComponent,
  ],
  template: `
    <div class="marco">
      <app-consola-nav />

      <section class="tarjeta">
        <div class="cab">
          <h1>Configuración de correo (SMTP)</h1>
          @if (editando()) {
            <button mat-button type="button" (click)="nuevo()"><mat-icon>add</mat-icon> Nueva</button>
          }
        </div>

        @if (cargando()) {
          <mat-progress-bar mode="indeterminate" />
        }

        <p class="nota">
          La contraseña del servidor no se guarda acá. <b>secretoRef</b> es el nombre de la variable de
          entorno / entrada de vault donde vive (hoy: <code>app.aprovisionamiento.smtp-password</code>).
        </p>

        <form [formGroup]="form" (ngSubmit)="guardar()" class="form">
          <div class="grid">
            <mat-form-field appearance="outline">
              <mat-label>Nombre del remitente</mat-label>
              <input matInput formControlName="remitenteNombre" />
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Correo del remitente</mat-label>
              <input matInput type="email" formControlName="remitenteCorreo" />
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Responder a (opcional)</mat-label>
              <input matInput type="email" formControlName="responderA" />
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Host</mat-label>
              <input matInput formControlName="host" placeholder="smtp.proveedor.com" />
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Puerto</mat-label>
              <input matInput type="number" formControlName="puerto" />
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Seguridad</mat-label>
              <mat-select formControlName="seguridad">
                @for (s of seguridades; track s) {
                  <mat-option [value]="s">{{ s }}</mat-option>
                }
              </mat-select>
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Usuario (opcional)</mat-label>
              <input matInput formControlName="usuario" />
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>secretoRef (opcional)</mat-label>
              <input matInput formControlName="secretoRef" />
            </mat-form-field>
          </div>
          <div class="acciones">
            <button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || guardando()">
              {{ editando() ? 'Guardar cambios' : 'Crear configuración' }}
            </button>
            @if (editando()) {
              <button mat-button type="button" (click)="nuevo()">Cancelar</button>
            }
          </div>
        </form>

        @if (configs().length === 0 && !cargando()) {
          <p class="vacio">No hay ninguna configuración SMTP. Creá una y activala.</p>
        }
        <ul class="lista">
          @for (c of configs(); track c.uuid) {
            <li [class.activa]="c.esActiva">
              <div class="info">
                <span class="host">{{ c.host }}:{{ c.puerto }} · {{ c.seguridad }}</span>
                <span class="rem">{{ c.remitenteNombre ? c.remitenteNombre + ' <' + c.remitenteCorreo + '>' : c.remitenteCorreo }}</span>
              </div>
              <div class="btns">
                @if (c.esActiva) {
                  <span class="badge">activa</span>
                } @else {
                  <button mat-stroked-button type="button" (click)="activar(c)">Activar</button>
                }
                <button mat-icon-button type="button" (click)="editar(c)" aria-label="Editar">
                  <mat-icon>edit</mat-icon>
                </button>
                <button mat-icon-button type="button" [disabled]="c.esActiva" (click)="eliminar(c)" aria-label="Eliminar">
                  <mat-icon>delete_outline</mat-icon>
                </button>
              </div>
            </li>
          }
        </ul>
      </section>
    </div>
  `,
  styles: [
    `
      :host { display: block; min-height: 100vh; background: #f1f5f9; }
      .marco { max-width: 900px; margin: 0 auto; padding: 24px; }
      .tarjeta { background: #fff; border: 1px solid #e2e8f0; border-radius: 14px; padding: 20px 22px; }
      .cab { display: flex; align-items: center; justify-content: space-between; }
      h1 { font-size: 1.2rem; font-weight: 700; margin: 0; color: #0f172a; }
      .nota { font-size: 0.82rem; color: #64748b; line-height: 1.5; margin: 8px 0 16px; }
      .nota code { background: #f1f5f9; padding: 1px 5px; border-radius: 4px; }
      .form { border: 1px solid #e2e8f0; border-radius: 10px; padding: 16px; margin-bottom: 20px; background: #f8fafc; }
      .grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 4px 16px; }
      mat-form-field { width: 100%; }
      .acciones { display: flex; gap: 8px; align-items: center; }
      .lista { list-style: none; margin: 0; padding: 0; display: grid; gap: 8px; }
      .lista li {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 12px;
        padding: 12px 14px;
        border: 1px solid #e2e8f0;
        border-radius: 10px;
      }
      .lista li.activa { border-color: #0e7490; background: #ecfeff; }
      .info { display: flex; flex-direction: column; gap: 2px; }
      .host { font-weight: 600; color: #0f172a; }
      .rem { font-size: 0.8rem; color: #64748b; }
      .btns { display: flex; align-items: center; gap: 6px; }
      .badge {
        font-size: 0.7rem;
        font-weight: 700;
        text-transform: uppercase;
        color: #0e7490;
        border: 1px solid #0e7490;
        border-radius: 999px;
        padding: 2px 8px;
      }
      .vacio { color: #64748b; font-size: 0.88rem; }
      @media (max-width: 620px) { .grid { grid-template-columns: 1fr; } }
    `,
  ],
})
export class ConsolaConfigCorreoComponent {
  private readonly fb = inject(FormBuilder);
  private readonly config = inject(ConsolaConfigService);
  private readonly snack = inject(MatSnackBar);

  protected readonly seguridades = SEGURIDADES;
  protected readonly configs = signal<ConfiguracionSmtp[]>([]);
  protected readonly cargando = signal(false);
  protected readonly guardando = signal(false);
  protected readonly editando = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    remitenteNombre: [''],
    remitenteCorreo: ['', [Validators.required, Validators.email]],
    responderA: [''],
    host: ['', [Validators.required]],
    puerto: [587, [Validators.required, Validators.min(1), Validators.max(65535)]],
    usuario: [''],
    secretoRef: [''],
    seguridad: ['starttls', [Validators.required]],
  });

  constructor() {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.config.listarConfigCorreo().subscribe({
      next: (lista) => this.configs.set(lista),
      error: (e: HttpErrorResponse) => this.snack.open(this.mensaje(e), 'Cerrar', { duration: 4000 }),
      complete: () => this.cargando.set(false),
    });
  }

  nuevo(): void {
    this.editando.set(null);
    this.form.reset({
      remitenteNombre: '',
      remitenteCorreo: '',
      responderA: '',
      host: '',
      puerto: 587,
      usuario: '',
      secretoRef: '',
      seguridad: 'starttls',
    });
  }

  editar(c: ConfiguracionSmtp): void {
    this.editando.set(c.uuid);
    this.form.setValue({
      remitenteNombre: c.remitenteNombre ?? '',
      remitenteCorreo: c.remitenteCorreo,
      responderA: c.responderA ?? '',
      host: c.host,
      puerto: c.puerto,
      usuario: c.usuario ?? '',
      secretoRef: c.secretoRef ?? '',
      seguridad: c.seguridad,
    });
  }

  guardar(): void {
    if (this.form.invalid) {
      return;
    }
    this.guardando.set(true);
    const v = this.form.getRawValue();
    const payload = {
      remitenteNombre: v.remitenteNombre.trim() || null,
      remitenteCorreo: v.remitenteCorreo.trim(),
      responderA: v.responderA.trim() || null,
      host: v.host.trim(),
      puerto: Number(v.puerto),
      usuario: v.usuario.trim() || null,
      secretoRef: v.secretoRef.trim() || null,
      seguridad: v.seguridad,
    };
    const id = this.editando();
    const op$ = id ? this.config.actualizarConfigCorreo(id, payload) : this.config.crearConfigCorreo(payload);

    op$.subscribe({
      next: () => {
        this.snack.open(id ? 'Configuración actualizada' : 'Configuración creada', 'OK', { duration: 2500 });
        this.nuevo();
        this.cargar();
      },
      error: (e: HttpErrorResponse) => this.snack.open(this.mensaje(e), 'Cerrar', { duration: 4000 }),
      complete: () => this.guardando.set(false),
    });
  }

  activar(c: ConfiguracionSmtp): void {
    this.config.activarConfigCorreo(c.uuid).subscribe({
      next: () => {
        this.snack.open('Configuración activada', 'OK', { duration: 2500 });
        this.cargar();
      },
      error: (e: HttpErrorResponse) => this.snack.open(this.mensaje(e), 'Cerrar', { duration: 4000 }),
    });
  }

  eliminar(c: ConfiguracionSmtp): void {
    if (!confirm(`¿Eliminar la configuración de ${c.host}?`)) {
      return;
    }
    this.config.eliminarConfigCorreo(c.uuid).subscribe({
      next: () => {
        this.snack.open('Configuración eliminada', 'OK', { duration: 2500 });
        if (this.editando() === c.uuid) {
          this.nuevo();
        }
        this.cargar();
      },
      error: (e: HttpErrorResponse) => this.snack.open(this.mensaje(e), 'Cerrar', { duration: 4000 }),
    });
  }

  private mensaje(e: HttpErrorResponse): string {
    if (e.status === 0) {
      return 'No hay conexión con el servidor.';
    }
    if (e.status === 409) {
      return (e.error as { mensaje?: string } | null)?.mensaje ?? 'No se puede: hay que activar otra primero.';
    }
    const backend = (e.error as { mensaje?: string } | null)?.mensaje;
    return backend || 'No se pudo completar la operación.';
  }
}
