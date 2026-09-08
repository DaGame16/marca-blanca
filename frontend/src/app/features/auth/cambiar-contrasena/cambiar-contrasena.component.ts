import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../../core/auth/auth.service';

function contrasenasCoincidenValidator(control: AbstractControl): ValidationErrors | null {
  const nueva = control.get('contrasenaNueva')?.value;
  const confirmacion = control.get('confirmacion')?.value;
  return nueva && confirmacion && nueva !== confirmacion ? { noCoinciden: true } : null;
}

// Pantalla obligatoria despues del primer login con la contraseña temporal
// enviada por correo (backend: LoginResponse.debeCambiarContrasena). Mientras
// no se llame POST /auth/cambiar-contrasena, JwtAuthFilter bloquea cualquier
// otra ruta protegida -- por eso el guard (cambiar-contrasena.guard.ts) manda
// aqui antes que a cualquier otra pantalla.
@Component({
  selector: 'app-cambiar-contrasena',
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
        <p class="subtitulo">
          Iniciaste sesión con la contraseña temporal que te enviamos por correo. Por seguridad,
          antes de continuar debes reemplazarla por una propia.
        </p>

        <form [formGroup]="form" (ngSubmit)="submit()">
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Contraseña temporal</mat-label>
            <input matInput type="password" formControlName="contrasenaActual" autocomplete="current-password" />
            <mat-icon matPrefix>lock_outline</mat-icon>
          </mat-form-field>

          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Contraseña nueva</mat-label>
            <input matInput type="password" formControlName="contrasenaNueva" autocomplete="new-password" />
            <mat-icon matPrefix>lock</mat-icon>
          </mat-form-field>
          <p class="campo-hint">Mínimo 8 caracteres.</p>

          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Confirmar contraseña nueva</mat-label>
            <input matInput type="password" formControlName="confirmacion" autocomplete="new-password" />
            <mat-icon matPrefix>lock</mat-icon>
          </mat-form-field>

          @if (form.errors?.['noCoinciden'] && form.controls.confirmacion.touched) {
            <p class="error-texto">Las contraseñas no coinciden.</p>
          }
          @if (errorMensaje()) {
            <p class="error-texto">{{ errorMensaje() }}</p>
          }

          <button
            mat-flat-button
            color="primary"
            class="full-width submit-btn"
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
        background: radial-gradient(circle at 20% -10%, #1e293b 0%, #0f172a 55%, #0b1120 100%);
      }

      .tarjeta {
        width: 100%;
        max-width: 420px;
        padding: 32px 36px 40px;
        background: #fff;
        border-radius: 16px;
        box-shadow: 0 1px 2px rgba(0, 0, 0, 0.2), 0 24px 60px rgba(0, 0, 0, 0.45);
        text-align: center;
      }

      .icono {
        font-size: 40px;
        width: 40px;
        height: 40px;
        color: #2563eb;
        margin-bottom: 8px;
      }

      h1 {
        font-size: 1.4rem;
        font-weight: 700;
        margin: 0 0 8px;
        color: #0f172a;
      }

      .subtitulo {
        color: #64748b;
        font-size: 0.9rem;
        line-height: 1.5;
        margin: 0 0 24px;
        text-align: left;
      }

      form {
        text-align: left;
      }

      .full-width {
        width: 100%;
      }

      .campo-hint {
        margin: -12px 0 12px;
        font-size: 0.78rem;
        color: #94a3b8;
      }

      .error-texto {
        margin: 0 0 12px;
        font-size: 0.82rem;
        color: #dc2626;
      }

      .submit-btn {
        margin-top: 8px;
        height: 44px;
        font-size: 15px;
      }
    `,
  ],
})
export class CambiarContrasenaComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly enviando = signal(false);
  protected readonly errorMensaje = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group(
    {
      contrasenaActual: ['', [Validators.required]],
      contrasenaNueva: ['', [Validators.required, Validators.minLength(8)]],
      confirmacion: ['', [Validators.required]],
    },
    { validators: contrasenasCoincidenValidator },
  );

  submit(): void {
    if (this.form.invalid) {
      return;
    }
    this.enviando.set(true);
    this.errorMensaje.set(null);

    const { contrasenaActual, contrasenaNueva } = this.form.getRawValue();
    this.auth.cambiarContrasena({ contrasenaActual, contrasenaNueva }).subscribe({
      next: () => this.router.navigateByUrl('/tareas'),
      error: (error: HttpErrorResponse) => {
        this.enviando.set(false);
        this.errorMensaje.set(
          error.status === 401 || error.status === 400
            ? 'La contraseña temporal no es correcta.'
            : 'No se pudo cambiar la contraseña. Intenta de nuevo.',
        );
      },
    });
  }
}
