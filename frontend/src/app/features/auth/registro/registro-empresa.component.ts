import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

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

          <h1>Crea el espacio de tu empresa</h1>
          <p class="brand-tagline">
            Elige un identificador único: será la dirección desde la que tu equipo entra a la plataforma.
          </p>
        </div>
      </section>

      <section class="form-panel">
        <div class="form-wrapper">
          <a routerLink="/login" class="back-link">
            <mat-icon>arrow_back</mat-icon>
            Ya tengo cuenta
          </a>

          <h2>Registra tu empresa</h2>
          <p class="form-subtitle">Así se verá la dirección de tu equipo</p>

          <form [formGroup]="form" (ngSubmit)="continuar()">
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
                Este será el identificador que tu equipo usará para iniciar sesión (y, más adelante, tu
                subdominio propio).
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

            <p class="nota-preview">
              <mat-icon>info_outline</mat-icon>
              Esta pantalla es un adelanto visual: la creación real de empresas todavía no está conectada
              al backend.
            </p>
          </form>
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
        margin: 0;
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
        max-width: 400px;
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

  protected readonly dominioBase = DOMINIO_BASE;
  protected readonly editandoSlug = signal(false);

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

  continuar(): void {
    if (this.form.invalid) {
      return;
    }
    // TODO: conectar con el endpoint real de creación de empresas cuando exista
    // (hoy no hay ningún POST /api/v1/empresas en el backend). Por ahora esta
    // pantalla solo valida el diseño del flujo de registro tipo Odoo.
    this.snackBar.open(
      `Próximamente: crearemos "${this.form.controls.slug.value}.${this.dominioBase}" para tu empresa.`,
      'Cerrar',
      { duration: 4000 }
    );
  }
}
