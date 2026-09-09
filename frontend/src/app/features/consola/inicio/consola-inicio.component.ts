import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ConsolaAuthService } from '../../../core/consola/consola-auth.service';

/**
 * Aterrizaje de la consola tras el login. Placeholder de esta fase: confirma que
 * el token de operador funciona contra el backend. La Fase 2 lo reemplaza por el
 * listado de empresas.
 */
@Component({
  selector: 'app-consola-inicio',
  standalone: true,
  imports: [MatButtonModule, MatIconModule],
  template: `
    <div class="marco">
      <header>
        <div class="titulo">
          <mat-icon>shield_person</mat-icon>
          <div>
            <strong>Consola de operación</strong>
            <span>Portal GuajiraNet</span>
          </div>
        </div>
        <button mat-stroked-button type="button" (click)="salir()">
          <mat-icon>logout</mat-icon>
          Cerrar sesión
        </button>
      </header>

      <section class="panel">
        <h1>Sesión de operador activa</h1>
        @if (operador(); as op) {
          <dl>
            <div><dt>Correo</dt><dd>{{ op.correo }}</dd></div>
            <div><dt>Rol</dt><dd>{{ op.rol }}</dd></div>
            <div><dt>ID</dt><dd class="mono">{{ op.operadorId }}</dd></div>
          </dl>
        }
        <p class="nota">
          Las pantallas de administración (empresas, correo, módulos, operadores) llegan en la
          siguiente fase. Este espacio existe para verificar el login de extremo a extremo.
        </p>
      </section>
    </div>
  `,
  styles: [
    `
      :host {
        display: block;
        min-height: 100vh;
        background: #f1f5f9;
      }

      .marco {
        max-width: 880px;
        margin: 0 auto;
        padding: 24px;
      }

      header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 16px 0 24px;
      }

      .titulo {
        display: flex;
        align-items: center;
        gap: 12px;
      }

      .titulo mat-icon {
        color: #0e7490;
        font-size: 30px;
        width: 30px;
        height: 30px;
      }

      .titulo strong {
        display: block;
        font-size: 0.95rem;
        color: #0f172a;
      }

      .titulo span {
        font-size: 0.72rem;
        color: #64748b;
        text-transform: uppercase;
        letter-spacing: 0.08em;
      }

      .panel {
        background: #fff;
        border: 1px solid #e2e8f0;
        border-radius: 14px;
        padding: 28px 26px;
      }

      h1 {
        font-size: 1.25rem;
        font-weight: 700;
        margin: 0 0 18px;
        color: #0f172a;
      }

      dl {
        display: grid;
        gap: 10px;
        margin: 0 0 20px;
      }

      dl div {
        display: grid;
        grid-template-columns: 90px 1fr;
        gap: 12px;
      }

      dt {
        font-size: 0.8rem;
        text-transform: uppercase;
        letter-spacing: 0.06em;
        color: #64748b;
      }

      dd {
        margin: 0;
        font-size: 0.92rem;
        color: #0f172a;
      }

      .mono {
        font-family: ui-monospace, "SF Mono", Menlo, monospace;
        font-size: 0.82rem;
      }

      .nota {
        margin: 0;
        font-size: 0.86rem;
        color: #64748b;
        line-height: 1.55;
      }
    `,
  ],
})
export class ConsolaInicioComponent {
  private readonly consolaAuth = inject(ConsolaAuthService);
  protected readonly operador = this.consolaAuth.operador;

  salir(): void {
    this.consolaAuth.logout();
  }
}
