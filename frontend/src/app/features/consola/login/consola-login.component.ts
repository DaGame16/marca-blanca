import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ConsolaAuthService } from '../../../core/consola/consola-auth.service';

/**
 * Login de la consola de operacion de GuajiraNet. NO se tematiza por empresa: es
 * la herramienta interna, no una pantalla de cliente. Solo pide correo y
 * contrasena -- el operador no pertenece a ninguna empresa.
 */
@Component({
  selector: 'app-consola-login',
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
        <div class="marca">
          <mat-icon>shield_person</mat-icon>
          <div>
            <strong>Consola de operación</strong>
            <span>Portal GuajiraNet</span>
          </div>
        </div>

        <h1>Acceso de operador</h1>
        <p class="sub">Solo personal de la plataforma. El acceso queda registrado.</p>

        <form [formGroup]="form" (ngSubmit)="submit()">
          <mat-form-field appearance="outline" class="ancho">
            <mat-label>Correo</mat-label>
            <input matInput type="email" formControlName="correo" autocomplete="username" />
            <mat-icon matPrefix>mail_outline</mat-icon>
          </mat-form-field>

          <mat-form-field appearance="outline" class="ancho">
            <mat-label>Contraseña</mat-label>
            <input
              matInput
              [type]="verClave() ? 'text' : 'password'"
              formControlName="contrasena"
              autocomplete="current-password"
            />
            <mat-icon matPrefix>lock_outline</mat-icon>
            <button
              mat-icon-button
              matSuffix
              type="button"
              [attr.aria-label]="verClave() ? 'Ocultar contraseña' : 'Mostrar contraseña'"
              (click)="verClave.set(!verClave())"
            >
              <mat-icon>{{ verClave() ? 'visibility_off' : 'visibility' }}</mat-icon>
            </button>
          </mat-form-field>

          @if (error()) {
            <p class="error"><mat-icon>error_outline</mat-icon>{{ error() }}</p>
          }

          <button
            mat-flat-button
            color="primary"
            class="ancho enviar"
            type="submit"
            [disabled]="form.invalid || cargando()"
          >
            @if (cargando()) {
              <mat-spinner diameter="20" />
            } @else {
              Entrar
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
        max-width: 380px;
        background: #fff;
        border-radius: 14px;
        border: 1px solid #e2e8f0;
        box-shadow: 0 24px 60px rgba(2, 6, 23, 0.45);
        padding: 36px 32px;
      }

      .marca {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 28px;
      }

      .marca mat-icon {
        color: #0e7490;
        font-size: 32px;
        width: 32px;
        height: 32px;
      }

      .marca strong {
        display: block;
        font-size: 0.95rem;
        color: #0f172a;
        letter-spacing: 0.2px;
      }

      .marca span {
        font-size: 0.75rem;
        color: #64748b;
        text-transform: uppercase;
        letter-spacing: 0.08em;
      }

      h1 {
        font-size: 1.4rem;
        font-weight: 700;
        margin: 0 0 4px;
        color: #0f172a;
      }

      .sub {
        margin: 0 0 24px;
        font-size: 0.9rem;
        color: #64748b;
      }

      .ancho {
        width: 100%;
      }

      .enviar {
        height: 44px;
        margin-top: 4px;
        background-color: #0e7490 !important;
      }

      .error {
        display: flex;
        align-items: center;
        gap: 8px;
        color: #b3261e;
        font-size: 13px;
        margin: 0 0 14px;
      }

      .error mat-icon {
        font-size: 18px;
        width: 18px;
        height: 18px;
      }
    `,
  ],
})
export class ConsolaLoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly consolaAuth = inject(ConsolaAuthService);
  private readonly router = inject(Router);

  protected readonly cargando = signal(false);
  protected readonly verClave = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    correo: ['', [Validators.required, Validators.email]],
    contrasena: ['', [Validators.required]],
  });

  submit(): void {
    if (this.form.invalid) {
      return;
    }
    this.cargando.set(true);
    this.error.set(null);

    this.consolaAuth.login(this.form.getRawValue()).subscribe({
      next: (res) =>
        this.router.navigateByUrl(res.debeCambiarContrasena ? '/consola/cambiar-contrasena' : '/consola'),
      error: (e: HttpErrorResponse) => {
        this.error.set(this.mensaje(e));
        this.cargando.set(false);
      },
      complete: () => this.cargando.set(false),
    });
  }

  private mensaje(e: HttpErrorResponse): string {
    if (e.status === 0) {
      return 'No hay conexión con el servidor.';
    }
    if (e.status === 403) {
      return 'La cuenta de operador está inactiva.';
    }
    const backend = (e.error as { mensaje?: string } | null)?.mensaje;
    return backend || 'Correo o contraseña incorrectos.';
  }
}
