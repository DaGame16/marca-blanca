import { Component, computed, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ConsolaEmpresasService } from '../../../core/consola/consola-empresas.service';
import { EmpresaDetalle, ModuloDetalle } from '../../../core/consola/empresa-detalle.models';

const TIPOS_LOGIN = [
  { valor: 1, etiqueta: 'Lateral' },
  { valor: 2, etiqueta: 'Centrado' },
  { valor: 3, etiqueta: 'Fondo' },
];
const TIPOS_PANTALLA = [
  { valor: 1, etiqueta: 'Opción 1' },
  { valor: 2, etiqueta: 'Opción 2' },
  { valor: 3, etiqueta: 'Opción 3' },
];

/**
 * Detalle / edición de una empresa desde la consola (Fase 3). Tres secciones que
 * guardan por separado: Datos, Marca, Módulos. identificador y dominio son solo
 * lectura (nombran la base física).
 */
@Component({
  selector: 'app-consola-empresa-detalle',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatSnackBarModule,
  ],
  template: `
    <div class="marco">
      <a routerLink="/consola" class="volver"><mat-icon>arrow_back</mat-icon> Empresas</a>

      @if (cargando()) {
        <mat-progress-bar mode="indeterminate" />
      }
      @if (errorCarga()) {
        <p class="error"><mat-icon>error_outline</mat-icon>{{ errorCarga() }}</p>
      }

      @if (empresa(); as e) {
        <header>
          <div>
            <h1>{{ e.nombreLegal }}</h1>
            <p class="ident">{{ e.identificador }} · {{ e.dominio }} · <span class="chip">{{ e.estado }}</span></p>
          </div>
        </header>

        <!-- DATOS -->
        <section class="tarjeta">
          <h2>Datos de contacto</h2>
          <p class="hint">
            El correo de acá es el de contacto (facturación y avisos de la plataforma). No cambia el
            correo con el que inician sesión los usuarios de la empresa.
          </p>
          <form [formGroup]="datosForm" (ngSubmit)="guardarDatos()">
            <div class="grid">
              <mat-form-field appearance="outline">
                <mat-label>Nombre legal</mat-label>
                <input matInput formControlName="nombreLegal" />
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Representante legal</mat-label>
                <input matInput formControlName="representanteLegal" />
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Correo de contacto</mat-label>
                <input matInput type="email" formControlName="correo" />
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Teléfono</mat-label>
                <input matInput formControlName="telefono" />
              </mat-form-field>
              <mat-form-field appearance="outline" class="ancho">
                <mat-label>Sitio web</mat-label>
                <input matInput formControlName="sitioWeb" />
              </mat-form-field>
            </div>
            <button mat-flat-button color="primary" type="submit" [disabled]="datosForm.invalid || guardando() === 'datos'">
              Guardar datos
            </button>
          </form>
        </section>

        <!-- MARCA -->
        <section class="tarjeta">
          <h2>Marca</h2>
          <form [formGroup]="marcaForm" (ngSubmit)="guardarMarca()">
            <div class="grid">
              <label class="color">
                Color primario
                <input type="color" formControlName="colorPrimario" />
              </label>
              <label class="color">
                Color secundario
                <input type="color" formControlName="colorSecundario" />
              </label>
              <mat-form-field appearance="outline" class="ancho">
                <mat-label>URL del logo</mat-label>
                <input matInput formControlName="urlLogo" placeholder="https://…" />
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Tipo de login</mat-label>
                <mat-select formControlName="tipoLogin">
                  @for (t of tiposLogin; track t.valor) {
                    <mat-option [value]="t.valor">{{ t.etiqueta }}</mat-option>
                  }
                </mat-select>
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Pantalla principal</mat-label>
                <mat-select formControlName="tipoPantallaPrincipal">
                  @for (t of tiposPantalla; track t.valor) {
                    <mat-option [value]="t.valor">{{ t.etiqueta }}</mat-option>
                  }
                </mat-select>
              </mat-form-field>
            </div>
            <button mat-flat-button color="primary" type="submit" [disabled]="guardando() === 'marca'">
              Guardar marca
            </button>
          </form>
        </section>

        <!-- MÓDULOS -->
        <section class="tarjeta">
          <h2>Módulos</h2>
          @if (modulos().length === 0) {
            <p class="vacio">No hay módulos en el catálogo.</p>
          }
          <ul class="modulos">
            @for (m of modulos(); track m.codigo) {
              <li>
                <div>
                  <span class="nombre">{{ m.nombre }}</span>
                  <span class="codigo">{{ m.codigo }}</span>
                </div>
                <mat-slide-toggle
                  [checked]="m.activo"
                  [disabled]="moduloOcupado() === m.codigo"
                  (change)="alternarModulo(m, $event.checked)"
                />
              </li>
            }
          </ul>
        </section>
      }
    </div>
  `,
  styles: [
    `
      :host { display: block; min-height: 100vh; background: #f1f5f9; }
      .marco { max-width: 860px; margin: 0 auto; padding: 24px; }

      .volver {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        font-size: 0.85rem;
        color: #475569;
        text-decoration: none;
        margin-bottom: 16px;
      }
      .volver mat-icon { font-size: 18px; width: 18px; height: 18px; }

      header { margin-bottom: 8px; }
      h1 { font-size: 1.35rem; font-weight: 700; margin: 0; color: #0f172a; }
      .ident { margin: 4px 0 0; font-size: 0.82rem; color: #64748b; }
      .chip {
        display: inline-block;
        font-size: 0.7rem;
        font-weight: 600;
        text-transform: uppercase;
        padding: 2px 8px;
        border-radius: 999px;
        background: #e2e8f0;
        color: #334155;
      }

      .tarjeta {
        background: #fff;
        border: 1px solid #e2e8f0;
        border-radius: 14px;
        padding: 20px 22px;
        margin-top: 16px;
      }
      h2 { font-size: 1.05rem; font-weight: 700; margin: 0 0 14px; color: #0f172a; }
      .hint { margin: -6px 0 16px; font-size: 0.82rem; color: #64748b; line-height: 1.5; }

      .grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 4px 16px;
      }
      .ancho { grid-column: 1 / -1; }
      mat-form-field { width: 100%; }

      .color {
        display: flex;
        flex-direction: column;
        gap: 6px;
        font-size: 0.78rem;
        color: #475569;
        padding: 4px 0 12px;
      }
      .color input[type='color'] {
        width: 100%;
        height: 40px;
        border: 1px solid #cbd5e1;
        border-radius: 6px;
        background: none;
        cursor: pointer;
      }

      .modulos { list-style: none; margin: 0; padding: 0; display: grid; gap: 6px; }
      .modulos li {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 10px 12px;
        border: 1px solid #e2e8f0;
        border-radius: 8px;
      }
      .modulos .nombre { font-weight: 600; color: #0f172a; }
      .modulos .codigo { display: block; font-size: 0.75rem; color: #94a3b8; }

      .vacio { color: #64748b; font-size: 0.88rem; }
      .error {
        display: flex;
        align-items: center;
        gap: 8px;
        color: #b3261e;
        font-size: 13px;
        margin: 12px 0;
      }
      .error mat-icon { font-size: 18px; width: 18px; height: 18px; }

      @media (max-width: 620px) {
        .grid { grid-template-columns: 1fr; }
      }
    `,
  ],
})
export class ConsolaEmpresaDetalleComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);
  private readonly empresasService = inject(ConsolaEmpresasService);
  private readonly snack = inject(MatSnackBar);

  protected readonly tiposLogin = TIPOS_LOGIN;
  protected readonly tiposPantalla = TIPOS_PANTALLA;

  private readonly empresaId = this.route.snapshot.paramMap.get('id') ?? '';

  protected readonly empresa = signal<EmpresaDetalle | null>(null);
  protected readonly cargando = signal(true);
  protected readonly errorCarga = signal<string | null>(null);
  protected readonly guardando = signal<'datos' | 'marca' | null>(null);
  protected readonly moduloOcupado = signal<string | null>(null);
  protected readonly modulos = computed(() => this.empresa()?.modulos ?? []);

  protected readonly datosForm = this.fb.nonNullable.group({
    nombreLegal: ['', [Validators.required]],
    representanteLegal: ['', [Validators.required]],
    correo: ['', [Validators.required, Validators.email]],
    telefono: ['', [Validators.required]],
    sitioWeb: ['', [Validators.required]],
  });

  protected readonly marcaForm = this.fb.nonNullable.group({
    colorPrimario: ['#2563eb'],
    colorSecundario: ['#1e3a5f'],
    urlLogo: [''],
    tipoLogin: [1],
    tipoPantallaPrincipal: [1],
  });

  constructor() {
    this.cargar();
  }

  private cargar(): void {
    this.cargando.set(true);
    this.errorCarga.set(null);
    this.empresasService.detalle(this.empresaId).subscribe({
      next: (e) => {
        this.empresa.set(e);
        this.datosForm.patchValue({
          nombreLegal: e.nombreLegal,
          representanteLegal: e.representanteLegal,
          correo: e.correo,
          telefono: e.telefono,
          sitioWeb: e.sitioWeb,
        });
        this.marcaForm.patchValue({
          colorPrimario: e.colorPrimario ?? '#2563eb',
          colorSecundario: e.colorSecundario ?? '#1e3a5f',
          urlLogo: e.urlLogo ?? '',
          tipoLogin: e.tipoLogin,
          tipoPantallaPrincipal: e.tipoPantallaPrincipal,
        });
      },
      error: (err: HttpErrorResponse) => this.errorCarga.set(this.mensaje(err)),
      complete: () => this.cargando.set(false),
    });
  }

  guardarDatos(): void {
    if (this.datosForm.invalid) {
      return;
    }
    this.guardando.set('datos');
    this.empresasService.guardarDatos(this.empresaId, this.datosForm.getRawValue()).subscribe({
      next: () => this.snack.open('Datos guardados', 'OK', { duration: 2500 }),
      error: (err: HttpErrorResponse) => this.snack.open(this.mensaje(err), 'Cerrar', { duration: 4000 }),
      complete: () => this.guardando.set(null),
    });
  }

  guardarMarca(): void {
    this.guardando.set('marca');
    const v = this.marcaForm.getRawValue();
    this.empresasService
      .guardarMarca(this.empresaId, {
        colorPrimario: v.colorPrimario || null,
        colorSecundario: v.colorSecundario || null,
        urlLogo: v.urlLogo.trim() || null,
        tipoLogin: v.tipoLogin,
        tipoPantallaPrincipal: v.tipoPantallaPrincipal,
      })
      .subscribe({
        next: () => this.snack.open('Marca guardada', 'OK', { duration: 2500 }),
        error: (err: HttpErrorResponse) => this.snack.open(this.mensaje(err), 'Cerrar', { duration: 4000 }),
        complete: () => this.guardando.set(null),
      });
  }

  alternarModulo(modulo: ModuloDetalle, activar: boolean): void {
    this.moduloOcupado.set(modulo.codigo);
    const accion$ = activar
      ? this.empresasService.activarModulo(this.empresaId, modulo.codigo)
      : this.empresasService.desactivarModulo(this.empresaId, modulo.codigo);

    accion$.subscribe({
      next: () => {
        this.actualizarModuloLocal(modulo.codigo, activar);
        this.snack.open(activar ? 'Módulo activado' : 'Módulo desactivado', 'OK', { duration: 2000 });
      },
      error: (err: HttpErrorResponse) => {
        // el toggle ya se movió visualmente; recargamos para volver al estado real
        this.snack.open(this.mensaje(err), 'Cerrar', { duration: 4000 });
        this.cargar();
      },
      complete: () => this.moduloOcupado.set(null),
    });
  }

  private actualizarModuloLocal(codigo: string, activo: boolean): void {
    const e = this.empresa();
    if (!e) {
      return;
    }
    this.empresa.set({
      ...e,
      modulos: e.modulos.map((m) => (m.codigo === codigo ? { ...m, activo } : m)),
    });
  }

  private mensaje(e: HttpErrorResponse): string {
    if (e.status === 0) {
      return 'No hay conexión con el servidor.';
    }
    if (e.status === 409) {
      return (e.error as { mensaje?: string } | null)?.mensaje ?? 'La empresa no admite esta edición ahora.';
    }
    if (e.status === 404) {
      return 'La empresa no existe.';
    }
    const backend = (e.error as { mensaje?: string } | null)?.mensaje;
    return backend || 'No se pudo completar la operación.';
  }
}
