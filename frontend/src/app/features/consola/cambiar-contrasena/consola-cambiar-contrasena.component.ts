import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ConsolaAuthService } from '../../../core/consola/consola-auth.service';

function contrasenasCoinciden(control: AbstractControl): ValidationErrors | null {
  const nueva = control.get('contrasenaNueva')?.value;
  const confirmacion = control.get('confirmacion')?.value;
  return nueva && confirmacion && nueva !== confirmacion ? { noCoinciden: true } : null;
}

/**
 * Cambio obligatorio de la contrasena temporal del operador (primer ingreso).
 * Hasta completarlo, el backend responde 403 a cualquier ruta de consola que no
 * sea /api/v1/consola/auth/**. El minimo son 10 caracteres (regla del backend).
 */
@Component({
  selector: 'app-consola-cambiar-contrasena',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <div class="pagina">
      <div class="tarjeta">
        <mat-icon class="icono">lock_reset</mat-icon>
        <h1>Crea tu contraseña definitiva</h1>
        <p class="sub">
          Ingresaste con una contraseña temporal. Por seguridad, reemplázala antes de continuar.
        </p>

        <form [formGroup]="form" (ngSubmit)="submit()">
          <mat-form-field appearance="outline" class="ancho">
            <mat-label>Contraseña temporal</mat-label>
            <input matInput type="password" formControlName="contrasenaActual" autocomplete="current-password" />
            <mat-icon matPrefix>lock_outline</mat-icon>
          </mat-form-field>

          <mat-form-field appearance="outline" class="ancho">
            <mat-label>Contraseña nueva</mat-label>
            <input matInput type="password" formControlName="contrasenaNueva" autocomplete="new-password" />
            <mat-icon matPrefix>lock</mat-icon>
          </mat-form-field>
          <p class="hint">Mínimo 10 caracteres.</p>

          <mat-form-field appearance="outline" class="ancho">
            <mat-label>Confirmar contraseña nueva</mat-label>
            <input matInput type="password" formControlName="confirmacion" autocomplete="new-password" />
            <mat-icon matPrefix>lock</mat-icon>
          </mat-form-field>

          @if (form.errors?.['noCoinciden'] && form.controls.confirmacion.touched) {
            <p class="error">Las contraseñas no coinciden.</p>
          }
          @if (error()) {
            <p class="error">{{ error() }}</p>
          }

          <button
            mat-flat-button
            color="primary"
            class="ancho enviar"
            type="submit"
            [disabled]="form.invalid || enviando()"
          >
            @if (enviando()) {
              <mat-spinner diameter="20" />
            } @else {
              Guardar y continuar
            }
          </button>
        </form>
      </div>
    </div>
  `,
  styles: [
    `
      :host {
        display: block;
      }

      .pagina {
        min-height: 100vh;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 24px;
        background: #0f172a;
      }

      .tarjeta {
        width: 100%;
        max-width: 400px;
        background: #fff;
        border-radius: 14px;
        border: 1px solid #e2e8f0;
        box-shadow: 0 24px 60px rgba(2, 6, 23, 0.45);
        padding: 36px 32px;
      }

      .icono {
        font-size: 34px;
        width: 34px;
        height: 34px;
        color: #0e7490;
      }

      h1 {
        font-size: 1.35rem;
        font-weight: 700;
        margin: 12px 0 4px;
        color: #0f172a;
      }

      .sub {
        margin: 0 0 22px;
        font-size: 0.9rem;
        color: #64748b;
      }

      .ancho {
        width: 100%;
      }

      .hint {
        margin: -8px 0 12px;
        font-size: 0.78rem;
        color: #64748b;
      }

      .enviar {
        height: 44px;
        margin-top: 4px;
        background-color: #0e7490 !important;
      }

      .error {
        color: #b3261e;
        font-size: 13px;
        margin: 0 0 12px;
      }
    `,
  ],
})
export class ConsolaCambiarContrasenaComponent {
  private readonly fb = inject(FormBuilder);
  private readonly consolaAuth = inject(ConsolaAuthService);

  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group(
    {
      contrasenaActual: ['', [Validators.required]],
      contrasenaNueva: ['', [Validators.required, Validators.minLength(10)]],
      confirmacion: ['', [Validators.required]],
    },
    { validators: contrasenasCoinciden },
  );

  submit(): void {
    if (this.form.invalid) {
      return;
    }
    this.enviando.set(true);
    this.error.set(null);

    const { contrasenaActual, contrasenaNueva } = this.form.getRawValue();
    this.consolaAuth.cambiarContrasena({ contrasenaActual, contrasenaNueva }).subscribe({
      // El token todavia lleva pwd_temp=true (el backend no reemite en este paso).
      // Se cierra la sesion y se vuelve a entrar con un token limpio -- mas simple
      // y seguro que arrastrar un token con una claim ya obsoleta.
      next: () => this.consolaAuth.logout(),
      error: (e: HttpErrorResponse) => {
        this.error.set(this.mensaje(e));
        this.enviando.set(false);
      },
      complete: () => this.enviando.set(false),
    });
  }

  private mensaje(e: HttpErrorResponse): string {
    if (e.status === 401) {
      return 'La contraseña temporal no es correcta.';
    }
    const backend = (e.error as { mensaje?: string } | null)?.mensaje;
    return backend || 'No se pudo cambiar la contraseña.';
  }
}
