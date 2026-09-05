import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MarcaService } from '../../../../core/identidad-visual/marca.service';
import { MarcaDeEmpresa } from '../../../../core/identidad-visual/models';

const FORMATO_HEX = /^#[0-9A-Fa-f]{6}$/;

@Component({
  selector: 'app-mi-marca',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatIconModule,
    MatButtonModule,
    MatInputModule,
    MatFormFieldModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
  ],
  template: `
    <div class="marca-page">
      <header class="marca-header">
        <h1>Mi marca</h1>
        <p>Personaliza el logo, los colores y el dominio con los que tus clientes ven la plataforma.</p>
      </header>

      @if (cargando()) {
        <div class="state-container">
          <mat-spinner diameter="44"></mat-spinner>
          <p>Cargando tu marca...</p>
        </div>
      } @else if (errorCarga()) {
        <div class="state-container">
          <mat-icon color="warn">error_outline</mat-icon>
          <p>{{ errorCarga() }}</p>
          <button mat-stroked-button (click)="cargar()">Reintentar</button>
        </div>
      } @else {
        <div class="marca-layout">
          <form class="marca-form" [formGroup]="form" (ngSubmit)="guardar()">
            <mat-form-field appearance="outline">
              <mat-label>URL del logo</mat-label>
              <input matInput formControlName="urlLogo" placeholder="https://mi-empresa.com/logo.png" />
              <mat-icon matPrefix>image</mat-icon>
            </mat-form-field>

            <div class="color-field">
              <mat-form-field appearance="outline">
                <mat-label>Color primario</mat-label>
                <input matInput formControlName="colorPrimario" placeholder="#2563EB" />
                <mat-icon matPrefix>palette</mat-icon>
              </mat-form-field>
              <span
                class="swatch"
                [style.background]="esHexValido(form.value.colorPrimario) ? form.value.colorPrimario : '#e2e8f0'"
              ></span>
            </div>
            @if (form.get('colorPrimario')?.invalid && form.get('colorPrimario')?.touched) {
              <p class="field-error">Formato inválido. Usa un hexadecimal de 6 dígitos, ej: #2563EB</p>
            }

            <div class="color-field">
              <mat-form-field appearance="outline">
                <mat-label>Color secundario</mat-label>
                <input matInput formControlName="colorSecundario" placeholder="#1E3A5F" />
                <mat-icon matPrefix>palette</mat-icon>
              </mat-form-field>
              <span
                class="swatch"
                [style.background]="esHexValido(form.value.colorSecundario) ? form.value.colorSecundario : '#e2e8f0'"
              ></span>
            </div>
            @if (form.get('colorSecundario')?.invalid && form.get('colorSecundario')?.touched) {
              <p class="field-error">Formato inválido. Usa un hexadecimal de 6 dígitos, ej: #1E3A5F</p>
            }

            <mat-form-field appearance="outline">
              <mat-label>Dominio propio</mat-label>
              <input matInput formControlName="dominioPropio" placeholder="app.mi-empresa.com" />
              <mat-icon matPrefix>public</mat-icon>
            </mat-form-field>

            <button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || guardando()">
              @if (guardando()) {
                <mat-spinner diameter="18"></mat-spinner>
              } @else {
                <span>Guardar cambios</span>
              }
            </button>
          </form>

          <aside class="marca-preview" [style.--color-primario]="previewPrimario()" [style.--color-secundario]="previewSecundario()">
            <p class="preview-label">Vista previa</p>
            <div class="preview-card">
              <div class="preview-header">
                @if (form.value.urlLogo) {
                  <img [src]="form.value.urlLogo" alt="Logo de la empresa" (error)="logoConError.set(true)" />
                } @else {
                  <mat-icon>image</mat-icon>
                }
              </div>
              <button class="preview-btn" type="button">Botón de ejemplo</button>
              <p class="preview-domain">
                <mat-icon inline>public</mat-icon>
                {{ form.value.dominioPropio || 'app.tu-empresa.com' }}
              </p>
            </div>
          </aside>
        </div>
      }
    </div>
  `,
  styles: [`
    .marca-page {
      min-height: 100%;
      padding: 40px 24px 64px;
      max-width: 960px;
      margin: 0 auto;
    }

    .marca-header h1 {
      margin: 0 0 6px;
      font-size: 28px;
      font-weight: 700;
      color: #1e3a5f;
    }

    .marca-header p {
      margin: 0 0 32px;
      color: #64748b;
    }

    .state-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12px;
      padding: 64px 0;
      color: #64748b;
    }

    .marca-layout {
      display: grid;
      grid-template-columns: 1fr 320px;
      gap: 32px;
      align-items: start;
    }

    @media (max-width: 720px) {
      .marca-layout {
        grid-template-columns: 1fr;
      }
    }

    .marca-form {
      display: flex;
      flex-direction: column;
      gap: 4px;
      background: #fff;
      border: 1px solid #e2e8f0;
      border-radius: 16px;
      padding: 28px;
    }

    .marca-form mat-form-field {
      width: 100%;
    }

    .color-field {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .color-field mat-form-field {
      flex: 1;
    }

    .swatch {
      width: 36px;
      height: 36px;
      border-radius: 8px;
      border: 1px solid #e2e8f0;
      flex-shrink: 0;
      margin-bottom: 20px;
    }

    .field-error {
      margin: -8px 0 8px;
      font-size: 12px;
      color: #dc2626;
    }

    button[type='submit'] {
      align-self: flex-start;
      margin-top: 12px;
      min-width: 160px;
    }

    .marca-preview {
      position: sticky;
      top: 24px;
    }

    .preview-label {
      margin: 0 0 8px;
      font-size: 13px;
      font-weight: 600;
      color: #64748b;
      text-transform: uppercase;
      letter-spacing: 0.04em;
    }

    .preview-card {
      background: linear-gradient(135deg, var(--color-secundario, #1e3a5f), var(--color-primario, #2563eb));
      border-radius: 16px;
      padding: 24px;
      display: flex;
      flex-direction: column;
      gap: 20px;
      color: #fff;
    }

    .preview-header {
      height: 48px;
      display: flex;
      align-items: center;
    }

    .preview-header img {
      max-height: 48px;
      max-width: 160px;
      object-fit: contain;
    }

    .preview-btn {
      align-self: flex-start;
      background: #fff;
      color: var(--color-primario, #2563eb);
      border: none;
      border-radius: 8px;
      padding: 10px 18px;
      font-weight: 600;
      cursor: default;
    }

    .preview-domain {
      display: flex;
      align-items: center;
      gap: 6px;
      margin: 0;
      font-size: 13px;
      opacity: 0.85;
    }
  `],
})
export class MiMarcaComponent implements OnInit {
  private readonly marcaService = inject(MarcaService);
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly cargando = signal(true);
  protected readonly guardando = signal(false);
  protected readonly errorCarga = signal<string | null>(null);
  protected readonly logoConError = signal(false);

  protected readonly form = this.fb.nonNullable.group({
    urlLogo: [''],
    colorPrimario: ['', [Validators.pattern(FORMATO_HEX)]],
    colorSecundario: ['', [Validators.pattern(FORMATO_HEX)]],
    dominioPropio: [''],
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.errorCarga.set(null);
    this.marcaService.obtener().subscribe({
      next: (marca) => {
        this.form.patchValue({
          urlLogo: marca.urlLogo ?? '',
          colorPrimario: marca.colorPrimario ?? '',
          colorSecundario: marca.colorSecundario ?? '',
          dominioPropio: marca.dominioPropio ?? '',
        });
        this.cargando.set(false);
      },
      error: () => {
        this.errorCarga.set('No pudimos cargar tu marca. Intenta de nuevo.');
        this.cargando.set(false);
      },
    });
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valores = this.form.getRawValue();
    const marca: MarcaDeEmpresa = {
      urlLogo: valores.urlLogo || null,
      colorPrimario: valores.colorPrimario || null,
      colorSecundario: valores.colorSecundario || null,
      dominioPropio: valores.dominioPropio || null,
    };

    this.guardando.set(true);
    this.marcaService.actualizar(marca).subscribe({
      next: () => {
        this.guardando.set(false);
        this.snackBar.open('Marca actualizada', 'Cerrar', { duration: 3000 });
      },
      error: () => {
        this.guardando.set(false);
        this.snackBar.open('No pudimos guardar los cambios. Intenta de nuevo.', 'Cerrar', { duration: 4000 });
      },
    });
  }

  protected esHexValido(valor: string | null | undefined): boolean {
    return !!valor && FORMATO_HEX.test(valor);
  }

  protected previewPrimario(): string {
    const valor = this.form.value.colorPrimario;
    return this.esHexValido(valor) ? (valor as string) : '#2563eb';
  }

  protected previewSecundario(): string {
    const valor = this.form.value.colorSecundario;
    return this.esHexValido(valor) ? (valor as string) : '#1e3a5f';
  }
}
