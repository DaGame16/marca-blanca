import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-omnicanal-detalle',
  standalone: true,
  imports: [RouterLink, MatButtonModule, MatIconModule],
  template: `
    <div class="module-detail">
      <a routerLink="/" class="back-link">
        <mat-icon>arrow_back</mat-icon>
        Volver al inicio
      </a>

      <header class="module-hero" style="--accent: #7c3aed">
        <div class="module-icon-lg">
          <mat-icon>support_agent</mat-icon>
        </div>
        <h1>Omnicanal</h1>
        <p>Gestiona la comunicación con tus clientes desde todos los canales, en un solo lugar.</p>
      </header>

      <section class="module-body">
        <h2>¿Qué incluye?</h2>
        <div class="feature-list">
          <div class="feature-item">
            <mat-icon>check_circle</mat-icon>
            <div>
              <h3>Atención unificada</h3>
              <p>Todas las conversaciones de tus clientes centralizadas en una sola bandeja de entrada.</p>
            </div>
          </div>
          <div class="feature-item">
            <mat-icon>check_circle</mat-icon>
            <div>
              <h3>Múltiples canales</h3>
              <p>WhatsApp, chat web, correo y más, conectados a un mismo flujo de atención.</p>
            </div>
          </div>
          <div class="feature-item">
            <mat-icon>check_circle</mat-icon>
            <div>
              <h3>Historial centralizado</h3>
              <p>El historial completo de cada cliente disponible para todo tu equipo de atención.</p>
            </div>
          </div>
        </div>

        <a mat-flat-button color="primary" routerLink="/login" class="cta-btn">
          Iniciar Sesión
          <mat-icon>arrow_forward</mat-icon>
        </a>
      </section>
    </div>
  `,
  styles: [`
    .module-detail {
      max-width: 760px;
      margin: 0 auto;
      padding: 48px 24px 80px;
    }

    .back-link {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      font-size: 0.9rem;
      color: #64748b;
      text-decoration: none;
      margin-bottom: 32px;
    }

    .back-link:hover {
      color: #2563eb;
    }

    .back-link mat-icon {
      font-size: 18px;
      width: 18px;
      height: 18px;
    }

    .module-hero {
      text-align: center;
      margin-bottom: 48px;
    }

    .module-icon-lg {
      width: 72px;
      height: 72px;
      margin: 0 auto 20px;
      border-radius: 20px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: color-mix(in srgb, var(--accent) 12%, white);
    }

    .module-icon-lg mat-icon {
      font-size: 36px;
      width: 36px;
      height: 36px;
      color: var(--accent);
    }

    .module-hero h1 {
      font-size: 2.2rem;
      font-weight: 800;
      margin: 0 0 12px;
      color: #0f172a;
    }

    .module-hero p {
      font-size: 1.1rem;
      color: #64748b;
      max-width: 520px;
      margin: 0 auto;
    }

    .module-body h2 {
      font-size: 1.4rem;
      font-weight: 700;
      margin: 0 0 24px;
      color: #0f172a;
    }

    .feature-list {
      display: flex;
      flex-direction: column;
      gap: 24px;
      margin-bottom: 40px;
    }

    .feature-item {
      display: flex;
      gap: 16px;
    }

    .feature-item mat-icon {
      color: #7c3aed;
      flex-shrink: 0;
    }

    .feature-item h3 {
      font-size: 1.05rem;
      font-weight: 700;
      margin: 0 0 4px;
      color: #0f172a;
    }

    .feature-item p {
      margin: 0;
      color: #64748b;
      line-height: 1.5;
    }

    .cta-btn {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      height: 46px;
      padding: 0 28px !important;
    }
  `],
})
export class OmnicanalDetalleComponent {}
