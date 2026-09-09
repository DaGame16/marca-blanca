import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ConsolaAuthService } from '../../../core/consola/consola-auth.service';

/** Encabezado + navegación de la consola de operación. */
@Component({
  selector: 'app-consola-nav',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, MatButtonModule, MatIconModule],
  template: `
    <header>
      <div class="titulo">
        <mat-icon>shield_person</mat-icon>
        <div>
          <strong>Consola de operación</strong>
          <span>Portal GuajiraNet</span>
        </div>
      </div>

      <nav>
        <a routerLink="/consola" routerLinkActive="activo" [routerLinkActiveOptions]="{ exact: true }">Empresas</a>
        <a routerLink="/consola/modulos" routerLinkActive="activo">Módulos</a>
        <a routerLink="/consola/config-correo" routerLinkActive="activo">Correo</a>
      </nav>

      <div class="sesion">
        @if (operador(); as op) {
          <span class="quien">{{ op.correo }} · {{ op.rol }}</span>
        }
        <button mat-stroked-button type="button" (click)="salir()">
          <mat-icon>logout</mat-icon>
          Salir
        </button>
      </div>
    </header>
  `,
  styles: [
    `
      header {
        display: flex;
        align-items: center;
        gap: 24px;
        padding: 14px 0 22px;
        flex-wrap: wrap;
      }
      .titulo { display: flex; align-items: center; gap: 12px; }
      .titulo mat-icon { color: #0e7490; font-size: 30px; width: 30px; height: 30px; }
      .titulo strong { display: block; font-size: 0.95rem; color: #0f172a; }
      .titulo span {
        font-size: 0.72rem;
        color: #64748b;
        text-transform: uppercase;
        letter-spacing: 0.08em;
      }

      nav { display: flex; gap: 4px; }
      nav a {
        font-size: 0.88rem;
        color: #475569;
        text-decoration: none;
        padding: 6px 12px;
        border-radius: 8px;
      }
      nav a:hover { background: #e2e8f0; }
      nav a.activo { background: #0e7490; color: #fff; }

      .sesion { display: flex; align-items: center; gap: 14px; margin-left: auto; }
      .quien { font-size: 0.82rem; color: #475569; }
    `,
  ],
})
export class ConsolaNavComponent {
  private readonly consolaAuth = inject(ConsolaAuthService);
  protected readonly operador = this.consolaAuth.operador;

  salir(): void {
    this.consolaAuth.logout();
  }
}
