import { Component, inject, signal } from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../../core/auth/auth.service';
import { TemaLoginService } from '../../../core/temas/tema-login.service';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    NgTemplateOutlet,
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <!-- El formulario es idéntico en los 3 diseños; solo cambia el layout
         que lo rodea. Se define una sola vez y se reutiliza con
         ngTemplateOutlet para no triplicar los bindings del form. -->
    <ng-template #formularioTpl>
      <form [formGroup]="form" (ngSubmit)="submit()">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Empresa</mat-label>
          <input matInput formControlName="identificadorEmpresa" autocomplete="organization" />
          <mat-icon matPrefix>apartment</mat-icon>
          <mat-hint>El identificador que te dio tu administrador</mat-hint>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Correo electrónico</mat-label>
          <input matInput type="email" formControlName="correo" autocomplete="email" />
          <mat-icon matPrefix>mail_outline</mat-icon>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Contraseña</mat-label>
          <input
            matInput
            [type]="hidePassword() ? 'password' : 'text'"
            formControlName="contrasena"
            autocomplete="current-password"
          />
          <mat-icon matPrefix>lock_outline</mat-icon>
          <button
            mat-icon-button
            matSuffix
            type="button"
            (click)="hidePassword.set(!hidePassword())"
            [attr.aria-label]="'Mostrar contraseña'"
          >
            <mat-icon>{{ hidePassword() ? 'visibility_off' : 'visibility' }}</mat-icon>
          </button>
        </mat-form-field>

        @if (errorMessage()) {
          <p class="error">
            <mat-icon>error_outline</mat-icon>
            {{ errorMessage() }}
          </p>
        }

        <button
          mat-flat-button
          color="primary"
          class="full-width submit-btn"
          type="submit"
          [disabled]="form.invalid || loading()"
        >
          @if (loading()) {
            <mat-spinner diameter="20" />
          } @else {
            Entrar
          }
        </button>

        <p class="registro-link">
          ¿Tu empresa aún no tiene cuenta?
          <a routerLink="/registro">Regístrala aquí</a>
        </p>
      </form>
    </ng-template>

    @switch (temaLogin.tema()) {
      @case ('centrado') {
        <div class="login-page tema-centrado">
          <div class="tarjeta-centrada">
            <div class="logo-centrado">
              <mat-icon class="brand-logo-icon">hub</mat-icon>
              <span>Marca Blanca</span>
            </div>
            <h2>Iniciar sesión</h2>
            <p class="form-subtitle">Ingresa tus credenciales para continuar</p>
            <ng-container [ngTemplateOutlet]="formularioTpl"></ng-container>
          </div>
        </div>
      }
      @case ('fondo') {
        <div class="login-page tema-fondo">
          <div class="fondo-overlay"></div>
          <div class="tarjeta-flotante">
            <div class="logo-centrado">
              <mat-icon class="brand-logo-icon">hub</mat-icon>
              <span>Marca Blanca</span>
            </div>
            <h2>Iniciar sesión</h2>
            <p class="form-subtitle">Ingresa tus credenciales para continuar</p>
            <ng-container [ngTemplateOutlet]="formularioTpl"></ng-container>
          </div>
        </div>
      }
      @default {
        <div class="login-page tema-lateral">
          <section class="brand-panel">
            <div class="brand-shape shape-a"></div>
            <div class="brand-shape shape-b"></div>

            <div class="brand-content">
              <div class="brand-logo">
                <mat-icon class="brand-logo-icon">hub</mat-icon>
                <span>Marca Blanca</span>
              </div>

              <h1>Gestiona tu empresa desde un solo lugar</h1>
              <p class="brand-tagline">
                Usuarios, empresas, tareas y comunicación omnicanal en una sola plataforma.
              </p>

              <ul class="brand-highlights">
                <li>
                  <mat-icon>verified_user</mat-icon>
                  <span>Autenticación segura con JWT</span>
                </li>
                <li>
                  <mat-icon>apartment</mat-icon>
                  <span>Multi-empresa, multi-tenant</span>
                </li>
                <li>
                  <mat-icon>bolt</mat-icon>
                  <span>Arquitectura lista para escalar</span>
                </li>
              </ul>
            </div>
          </section>

          <section class="form-panel">
            <div class="form-wrapper">
              <a routerLink="/" class="back-link">
                <mat-icon>arrow_back</mat-icon>
                Volver al inicio
              </a>

              <h2>Iniciar sesión</h2>
              <p class="form-subtitle">Ingresa tus credenciales para continuar</p>

              <ng-container [ngTemplateOutlet]="formularioTpl"></ng-container>
            </div>
          </section>
        </div>
      }
    }
  `,
  styles: [
    `
      :host {
        --brand-dark: #1e3a5f;
        --brand-light: #2563eb;
        display: block;
      }

      .full-width {
        width: 100%;
      }

      .submit-btn {
        margin-top: 8px;
        height: 44px;
        font-size: 15px;
        background-color: var(--brand-light) !important;
      }

      .error {
        display: flex;
        align-items: center;
        gap: 8px;
        color: #b3261e;
        font-size: 13px;
        margin: 4px 0 16px;
      }

      .error mat-icon {
        font-size: 18px;
        width: 18px;
        height: 18px;
      }

      .registro-link {
        text-align: center;
        margin: 20px 0 0;
        font-size: 0.85rem;
        color: #64748b;
      }

      .registro-link a {
        color: var(--brand-light);
        font-weight: 600;
        text-decoration: none;
      }

      .registro-link a:hover {
        text-decoration: underline;
      }

      .form-subtitle {
        color: #64748b;
        margin: 0 0 32px;
        font-size: 0.95rem;
      }

      .brand-logo-icon {
        font-size: 28px;
        width: 28px;
        height: 28px;
      }

      /* ---------- Tema lateral (el original) ---------- */
      .tema-lateral {
        min-height: 100vh;
        display: grid;
        grid-template-columns: 1.1fr 1fr;
      }

      .tema-lateral .brand-panel {
        position: relative;
        overflow: hidden;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 48px;
        background: linear-gradient(135deg, var(--brand-dark) 0%, var(--brand-light) 100%);
        color: #fff;
      }

      .tema-lateral .brand-shape {
        position: absolute;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.08);
      }

      .tema-lateral .shape-a {
        width: 420px;
        height: 420px;
        top: -120px;
        left: -140px;
      }

      .tema-lateral .shape-b {
        width: 300px;
        height: 300px;
        bottom: -100px;
        right: -80px;
        background: rgba(255, 255, 255, 0.06);
      }

      .tema-lateral .brand-content {
        position: relative;
        z-index: 1;
        max-width: 420px;
      }

      .tema-lateral .brand-logo {
        display: flex;
        align-items: center;
        gap: 10px;
        font-size: 20px;
        font-weight: 700;
        margin-bottom: 48px;
        letter-spacing: 0.2px;
      }

      .tema-lateral .brand-content h1 {
        font-size: 2.2rem;
        line-height: 1.25;
        font-weight: 700;
        margin: 0 0 16px;
      }

      .tema-lateral .brand-tagline {
        font-size: 1.05rem;
        line-height: 1.6;
        opacity: 0.9;
        margin: 0 0 40px;
      }

      .tema-lateral .brand-highlights {
        list-style: none;
        margin: 0;
        padding: 0;
        display: flex;
        flex-direction: column;
        gap: 16px;
      }

      .tema-lateral .brand-highlights li {
        display: flex;
        align-items: center;
        gap: 12px;
        font-size: 0.95rem;
        opacity: 0.95;
      }

      .tema-lateral .form-panel {
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 48px 24px;
        background: #f8fafc;
      }

      .tema-lateral .form-wrapper {
        width: 100%;
        max-width: 380px;
      }

      .tema-lateral .back-link {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        font-size: 0.85rem;
        color: #64748b;
        text-decoration: none;
        margin-bottom: 32px;
      }

      .tema-lateral .back-link:hover {
        color: var(--brand-light);
      }

      .tema-lateral .back-link mat-icon {
        font-size: 18px;
        width: 18px;
        height: 18px;
      }

      .tema-lateral .form-wrapper h2 {
        font-size: 1.75rem;
        font-weight: 700;
        margin: 0 0 8px;
        color: #0f172a;
      }

      @media (max-width: 900px) {
        .tema-lateral {
          grid-template-columns: 1fr;
        }

        .tema-lateral .brand-panel {
          display: none;
        }

        .tema-lateral .form-panel {
          padding: 32px 20px;
        }
      }

      /* ---------- Tema centrado ---------- */
      .tema-centrado {
        min-height: 100vh;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #f1f5f9;
        padding: 24px;
      }

      .tarjeta-centrada {
        width: 100%;
        max-width: 400px;
        background: white;
        border-radius: 16px;
        box-shadow: 0 20px 45px rgba(15, 23, 42, 0.08);
        padding: 40px 36px;
      }

      .logo-centrado {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 10px;
        font-size: 20px;
        font-weight: 700;
        color: var(--brand-dark);
        margin-bottom: 28px;
      }

      .tarjeta-centrada h2 {
        text-align: center;
        font-size: 1.6rem;
        font-weight: 700;
        margin: 0 0 6px;
        color: #0f172a;
      }

      .tarjeta-centrada .form-subtitle {
        text-align: center;
      }

      /* ---------- Tema fondo ---------- */
      .tema-fondo {
        position: relative;
        min-height: 100vh;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 24px;
        background: linear-gradient(160deg, #0f172a 0%, var(--brand-dark) 45%, var(--brand-light) 100%);
      }

      .fondo-overlay {
        position: absolute;
        inset: 0;
        background: radial-gradient(circle at 20% 20%, rgba(255, 255, 255, 0.08), transparent 55%),
          radial-gradient(circle at 80% 80%, rgba(255, 255, 255, 0.06), transparent 50%);
      }

      .tarjeta-flotante {
        position: relative;
        z-index: 1;
        width: 100%;
        max-width: 400px;
        background: rgba(255, 255, 255, 0.97);
        backdrop-filter: blur(6px);
        border-radius: 20px;
        box-shadow: 0 25px 60px rgba(0, 0, 0, 0.35);
        padding: 40px 36px;
      }

      .tarjeta-flotante h2 {
        text-align: center;
        font-size: 1.6rem;
        font-weight: 700;
        margin: 0 0 6px;
        color: #0f172a;
      }

      .tarjeta-flotante .form-subtitle {
        text-align: center;
      }
    `,
  ],
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  protected readonly temaLogin = inject(TemaLoginService);

  protected readonly loading = signal(false);
  protected readonly hidePassword = signal(true);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    correo: ['', [Validators.required, Validators.email]],
    contrasena: ['', [Validators.required]],
    identificadorEmpresa: [environment.identificadorEmpresaPorDefecto, [Validators.required]],
  });

  submit(): void {
    if (this.form.invalid) {
      return;
    }
    this.loading.set(true);
    this.errorMessage.set(null);

    this.auth.login(this.form.getRawValue()).subscribe({
      next: () =>
        this.router.navigateByUrl(this.auth.debeCambiarContrasena() ? '/cambiar-contrasena' : '/tareas'),
      error: () => {
        this.errorMessage.set('Email o contraseña incorrectos');
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }
}
