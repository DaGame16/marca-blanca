import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TemaLogin, TemaLoginService } from '../../../core/temas/tema-login.service';

// Dominio base solo para el preview visual (estilo "empresa.odoo.com").
// Cuando exista subdominio real por empresa, este valor debe salir de
// environment y coincidir con el dominio wildcard configurado en DNS.
const DOMINIO_BASE = 'marca-blanca.com';

const RANGO_DIACRITICOS = /[̀-ͯ]/g;

function generarSlug(nombre: string): string {
  return nombre
    .normalize('NFD')
    .replace(RANGO_DIACRITICOS, '') // quita acentos (diacríticos combinados tras normalize NFD)
    .toLowerCase()
    .replace(/[^a-z0-9\s-]/g, '')
    .trim()
    .replace(/\s+/g, '-')
    .replace(/-+/g, '-');
}

interface OpcionTema {
  codigo: TemaLogin;
  nombre: string;
  descripcion: string;
}

const OPCIONES_TEMA: OpcionTema[] = [
  {
    codigo: 'lateral',
    nombre: 'Panel lateral',
    descripcion: 'Panel de marca a un lado y el formulario al otro.',
  },
  {
    codigo: 'centrado',
    nombre: 'Centrado',
    descripcion: 'Tarjeta centrada con el logo arriba, sin panel lateral.',
  },
  {
    codigo: 'fondo',
    nombre: 'Fondo completo',
    descripcion: 'Fondo degradado a pantalla completa con el formulario flotando.',
  },
];

@Component({
  selector: 'app-registro-empresa',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
  ],
  template: `
    <div class="registro-page">
      <section class="brand-panel">
        <div class="brand-shape shape-a"></div>
        <div class="brand-shape shape-b"></div>

        <div class="brand-content">
          <div class="brand-logo">
            <mat-icon class="brand-logo-icon">hub</mat-icon>
            <span>Marca Blanca</span>
          </div>

          @if (paso() === 1) {
            <h1>Crea el espacio de tu empresa</h1>
            <p class="brand-tagline">
              Elige un identificador único: será la dirección desde la que tu equipo entra a la
              plataforma.
            </p>
          } @else {
            <h1>Personaliza tu cuenta</h1>
            <p class="brand-tagline">
              Elige cómo se va a ver la pantalla de inicio de sesión de tu equipo. Puedes cambiarlo
              cuando quieras más adelante.
            </p>
          }

          <div class="pasos-indicador">
            <span class="paso-punto" [class.paso-punto-activo]="paso() === 1">1. Tu empresa</span>
            <span class="paso-linea"></span>
            <span class="paso-punto" [class.paso-punto-activo]="paso() === 2">2. Diseño</span>
          </div>
        </div>
      </section>

      <section class="form-panel">
        <div class="form-wrapper">
          @if (paso() === 1) {
            <a routerLink="/login" class="back-link">
              <mat-icon>arrow_back</mat-icon>
              Ya tengo cuenta
            </a>

            <h2>Registra tu empresa</h2>
            <p class="form-subtitle">Así se verá la dirección de tu equipo</p>

            <form [formGroup]="form" (ngSubmit)="irAPaso2()">
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Nombre de la empresa</mat-label>
                <input
                  matInput
                  formControlName="nombreEmpresa"
                  (input)="onCambiarNombre()"
                  autocomplete="organization"
                />
                <mat-icon matPrefix>apartment</mat-icon>
              </mat-form-field>

              <div class="slug-preview">
                @if (editandoSlug()) {
                  <mat-form-field appearance="outline" class="full-width">
                    <mat-label>Identificador</mat-label>
                    <input matInput formControlName="slug" (input)="onEditarSlugManual()" />
                    <mat-icon matPrefix>link</mat-icon>
                  </mat-form-field>
                } @else {
                  <div class="slug-linea">
                    <span class="slug-texto">
                      <strong>{{ form.controls.slug.value || 'tu-empresa' }}</strong>.{{ dominioBase }}
                    </span>
                    <button
                      mat-icon-button
                      type="button"
                      (click)="editandoSlug.set(true)"
                      aria-label="Editar identificador"
                    >
                      <mat-icon>edit</mat-icon>
                    </button>
                  </div>
                }
                <p class="slug-hint">
                  Este será el identificador que tu equipo usará para iniciar sesión (y, más adelante,
                  tu subdominio propio).
                </p>
              </div>

              <button
                mat-flat-button
                color="primary"
                class="full-width submit-btn"
                type="submit"
                [disabled]="form.invalid"
              >
                Continuar
              </button>
            </form>
          } @else {
            <button mat-button type="button" class="back-link back-link-btn" (click)="paso.set(1)">
              <mat-icon>arrow_back</mat-icon>
              Volver
            </button>

            <h2>Diseño de inicio de sesión</h2>
            <p class="form-subtitle">
              Así se va a ver la pantalla de login de
              <strong>{{ form.controls.slug.value }}.{{ dominioBase }}</strong>
            </p>

            <div class="temas-grid">
              @for (opcion of opciones; track opcion.codigo) {
                <button
                  type="button"
                  class="tema-card"
                  [class.tema-card-activa]="temaLogin.tema() === opcion.codigo"
                  (click)="temaLogin.elegir(opcion.codigo)"
                >
                  <div class="preview" [class]="'preview-' + opcion.codigo">
                    @switch (opcion.codigo) {
                      @case ('lateral') {
                        <div class="preview-lateral">
                          <div class="preview-panel"></div>
                          <div class="preview-form">
                            <div class="preview-linea corta"></div>
                            <div class="preview-linea"></div>
                          </div>
                        </div>
                      }
                      @case ('centrado') {
                        <div class="preview-centrado">
                          <div class="preview-tarjeta">
                            <div class="preview-linea corta centrada"></div>
                            <div class="preview-linea"></div>
                          </div>
                        </div>
                      }
                      @case ('fondo') {
                        <div class="preview-fondo">
                          <div class="preview-tarjeta">
                            <div class="preview-linea corta centrada"></div>
                            <div class="preview-linea"></div>
                          </div>
                        </div>
                      }
                    }
                  </div>
                  <h3>{{ opcion.nombre }}</h3>
                  <p>{{ opcion.descripcion }}</p>
                  @if (temaLogin.tema() === opcion.codigo) {
                    <span class="tema-activo-badge">
                      <mat-icon inline>check_circle</mat-icon>
                      Seleccionado
                    </span>
                  }
                </button>
              }
            </div>

            <p class="tema-hint">
              Podrás cambiar este diseño cuando quieras desde "Mis módulos" → "Diseño de inicio de
              sesión".
            </p>

            <button
              mat-flat-button
              color="primary"
              class="full-width submit-btn"
              type="button"
              (click)="finalizar()"
            >
              Finalizar registro
            </button>

            <p class="nota-preview">
              <mat-icon>info_outline</mat-icon>
              Esta pantalla es un adelanto visual: la creación real de empresas todavía no está
              conectada al backend.
            </p>
          }
        </div>
      </section>
    </div>
  `,
  styles: [
    `
      :host {
        --brand-dark: #1e3a5f;
        --brand-light: #2563eb;
        display: block;
      }

      .registro-page {
        min-height: 100vh;
        display: grid;
        grid-template-columns: 1.1fr 1fr;
      }

      .brand-panel {
        position: relative;
        overflow: hidden;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 48px;
        background: linear-gradient(135deg, var(--brand-dark) 0%, var(--brand-light) 100%);
        color: #fff;
      }

      .brand-shape {
        position: absolute;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.08);
      }

      .shape-a {
        width: 420px;
        height: 420px;
        top: -120px;
        left: -140px;
      }

      .shape-b {
        width: 300px;
        height: 300px;
        bottom: -100px;
        right: -80px;
        background: rgba(255, 255, 255, 0.06);
      }

      .brand-content {
        position: relative;
        z-index: 1;
        max-width: 420px;
      }

      .brand-logo {
        display: flex;
        align-items: center;
        gap: 10px;
        font-size: 20px;
        font-weight: 700;
        margin-bottom: 48px;
        letter-spacing: 0.2px;
      }

      .brand-logo-icon {
        font-size: 28px;
        width: 28px;
        height: 28px;
      }

      .brand-content h1 {
        font-size: 2.2rem;
        line-height: 1.25;
        font-weight: 700;
        margin: 0 0 16px;
      }

      .brand-tagline {
        font-size: 1.05rem;
        line-height: 1.6;
        opacity: 0.9;
        margin: 0 0 32px;
      }

      .pasos-indicador {
        display: flex;
        align-items: center;
        gap: 10px;
        font-size: 0.85rem;
        opacity: 0.75;
      }

      .paso-punto {
        font-weight: 600;
      }

      .paso-punto-activo {
        opacity: 1;
        color: #fff;
      }

      .paso-linea {
        flex: 0 0 24px;
        height: 1px;
        background: rgba(255, 255, 255, 0.4);
      }

      .form-panel {
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 48px 24px;
        background: #f8fafc;
      }

      .form-wrapper {
        width: 100%;
        max-width: 460px;
      }

      .back-link {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        font-size: 0.85rem;
        color: #64748b;
        text-decoration: none;
        margin-bottom: 32px;
      }

      .back-link-btn {
        padding: 0;
      }

      .back-link:hover {
        color: var(--brand-light);
      }

      .back-link mat-icon {
        font-size: 18px;
        width: 18px;
        height: 18px;
      }

      .form-wrapper h2 {
        font-size: 1.75rem;
        font-weight: 700;
        margin: 0 0 8px;
        color: #0f172a;
      }

      .form-subtitle {
        color: #64748b;
        margin: 0 0 32px;
        font-size: 0.95rem;
      }

      .full-width {
        width: 100%;
      }

      .slug-preview {
        margin: -8px 0 20px;
      }

      .slug-linea {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 8px;
        background: white;
        border: 1px solid #e2e8f0;
        border-radius: 8px;
        padding: 10px 8px 10px 16px;
      }

      .slug-texto {
        font-size: 0.95rem;
        color: #334155;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .slug-texto strong {
        color: var(--brand-light);
      }

      .slug-hint {
        margin: 8px 0 0;
        font-size: 0.78rem;
        color: #94a3b8;
      }

      .submit-btn {
        margin-top: 8px;
        height: 44px;
        font-size: 15px;
        background-color: var(--brand-light) !important;
      }

      .nota-preview {
        display: flex;
        align-items: flex-start;
        gap: 8px;
        margin: 16px 0 0;
        font-size: 0.78rem;
        color: #94a3b8;
        line-height: 1.4;
      }

      .nota-preview mat-icon {
        font-size: 16px;
        width: 16px;
        height: 16px;
        margin-top: 1px;
      }

      /* ---------- Paso 2: temas ---------- */
      .temas-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
        gap: 12px;
        margin-bottom: 8px;
      }

      .tema-card {
        text-align: left;
        background: white;
        border: 1px solid #e2e8f0;
        border-radius: 12px;
        padding: 12px;
        cursor: pointer;
        font: inherit;
        color: inherit;
        display: flex;
        flex-direction: column;
      }

      .tema-card:hover {
        border-color: #93c5fd;
      }

      .tema-card-activa {
        border-color: var(--brand-light);
        box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
      }

      .preview {
        height: 70px;
        border-radius: 8px;
        overflow: hidden;
        margin-bottom: 10px;
        background: #f1f5f9;
      }

      .preview-lateral {
        display: grid;
        grid-template-columns: 1fr 1fr;
        height: 100%;
      }

      .preview-panel {
        background: linear-gradient(135deg, var(--brand-dark) 0%, var(--brand-light) 100%);
      }

      .preview-form {
        display: flex;
        flex-direction: column;
        justify-content: center;
        gap: 6px;
        padding: 10px;
        background: #f8fafc;
      }

      .preview-centrado {
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #e2e8f0;
      }

      .preview-fondo {
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        background: linear-gradient(160deg, #0f172a 0%, var(--brand-dark) 45%, var(--brand-light) 100%);
      }

      .preview-tarjeta {
        width: 70%;
        background: white;
        border-radius: 6px;
        padding: 8px;
        display: flex;
        flex-direction: column;
        gap: 6px;
      }

      .preview-linea {
        height: 5px;
        border-radius: 3px;
        background: #cbd5e1;
      }

      .preview-linea.corta {
        width: 60%;
        height: 6px;
      }

      .preview-linea.centrada {
        align-self: center;
      }

      .tema-card h3 {
        font-size: 0.85rem;
        font-weight: 700;
        margin: 0 0 2px;
        color: #0f172a;
      }

      .tema-card p {
        font-size: 0.72rem;
        color: #64748b;
        line-height: 1.3;
        margin: 0;
      }

      .tema-activo-badge {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        margin-top: 8px;
        font-size: 0.72rem;
        font-weight: 600;
        color: #16a34a;
      }

      .tema-hint {
        font-size: 0.78rem;
        color: #94a3b8;
        margin: 4px 0 20px;
      }

      @media (max-width: 900px) {
        .registro-page {
          grid-template-columns: 1fr;
        }

        .brand-panel {
          display: none;
        }

        .form-panel {
          padding: 32px 20px;
        }
      }
    `,
  ],
})
export class RegistroEmpresaComponent {
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);
  protected readonly temaLogin = inject(TemaLoginService);

  protected readonly dominioBase = DOMINIO_BASE;
  protected readonly editandoSlug = signal(false);
  protected readonly paso = signal<1 | 2>(1);
  protected readonly opciones = OPCIONES_TEMA;

  // Si el usuario edita el slug a mano, dejamos de regenerarlo automáticamente
  // a partir del nombre (mismo comportamiento que el campo de subdominio de Odoo).
  private slugTocadoManualmente = false;

  protected readonly form = this.fb.nonNullable.group({
    nombreEmpresa: ['', Validators.required],
    slug: ['', [Validators.required, Validators.pattern(/^[a-z0-9-]+$/)]],
  });

  onCambiarNombre(): void {
    if (this.slugTocadoManualmente) {
      return;
    }
    const slug = generarSlug(this.form.controls.nombreEmpresa.value);
    this.form.controls.slug.setValue(slug, { emitEvent: false });
  }

  onEditarSlugManual(): void {
    this.slugTocadoManualmente = true;
    const normalizado = generarSlug(this.form.controls.slug.value);
    this.form.controls.slug.setValue(normalizado, { emitEvent: false });
  }

  irAPaso2(): void {
    if (this.form.invalid) {
      return;
    }
    this.paso.set(2);
  }

  finalizar(): void {
    // TODO: conectar con el endpoint real de creación de empresas cuando exista
    // (hoy no hay ningún POST /api/v1/empresas en el backend). El diseño de login
    // elegido aquí ya queda guardado por TemaLoginService (localStorage); cuando
    // haya backend, este paso debe enviarlo junto con el resto del registro.
    this.snackBar.open(
      `Próximamente: crearemos "${this.form.controls.slug.value}.${this.dominioBase}" para tu empresa.`,
      'Cerrar',
      { duration: 4000 }
    );
  }
}
