import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AdminService } from '../../../../core/admin/admin.service';
import { ModuloDeEmpresa } from '../../../../core/admin/models';

// Metadatos visuales por módulo (icono + color de acento). El backend solo
// conoce codigo/nombre/descripcion/activo; esto es puramente de presentación.
const APARIENCIA_MODULO: Record<string, { icono: string; color: string }> = {
  omnicanal: { icono: 'support_agent', color: '#7c3aed' },
  'pbx-3cx': { icono: 'call', color: '#2563eb' },
};

const APARIENCIA_DEFECTO = { icono: 'extension', color: '#64748b' };

@Component({
  selector: 'app-mis-modulos',
  standalone: true,
  imports: [FormsModule, RouterLink, MatIconModule, MatButtonModule, MatProgressSpinnerModule, MatSnackBarModule],
  template: `
    <div class="apps-page">
      <header class="apps-header">
        <div class="apps-header-text">
          <h1>Mis módulos</h1>
          <p>Activa los módulos que quieras usar en tu empresa. Puedes desactivarlos cuando quieras.</p>
          <a routerLink="/mi-marca" class="marca-link">
            <mat-icon inline>palette</mat-icon>
            Personalizar mi marca
          </a>
        </div>

        <div class="search-box">
          <mat-icon>search</mat-icon>
          <input
            type="text"
            placeholder="Buscar módulos..."
            [ngModel]="busqueda()"
            (ngModelChange)="busqueda.set($event)"
            aria-label="Buscar módulos"
          />
        </div>
      </header>

      @if (cargando()) {
        <div class="state-container">
          <mat-spinner diameter="44"></mat-spinner>
          <p>Cargando módulos...</p>
        </div>
      } @else if (error()) {
        <div class="state-container">
          <mat-icon color="warn">error_outline</mat-icon>
          <p>{{ error() }}</p>
          <button mat-stroked-button (click)="cargar()">Reintentar</button>
        </div>
      } @else if (modulosFiltrados().length === 0) {
        <div class="state-container">
          <mat-icon>search_off</mat-icon>
          <p>No encontramos módulos que coincidan con "{{ busqueda() }}"</p>
        </div>
      } @else {
        <div class="apps-grid">
          @for (modulo of modulosFiltrados(); track modulo.codigo) {
            <div class="app-card" [class.app-card-activo]="modulo.activo">
              <div
                class="app-icon"
                [style.background]="colorClaro(modulo.codigo)"
                [style.color]="colorAcento(modulo.codigo)"
              >
                <mat-icon>{{ icono(modulo.codigo) }}</mat-icon>
              </div>

              <h3>{{ modulo.nombre }}</h3>
              <p class="app-desc">{{ modulo.descripcion }}</p>

              <div class="app-action">
                @if (modulo.activo) {
                  <button
                    class="btn-installed"
                    [disabled]="procesando() === modulo.codigo"
                    (click)="alternar(modulo)"
                  >
                    @if (procesando() === modulo.codigo) {
                      <mat-spinner diameter="16"></mat-spinner>
                    } @else {
                      <ng-container>
                        <mat-icon class="icon-default">check_circle</mat-icon>
                        <mat-icon class="icon-hover">close</mat-icon>
                        <span class="label-default">Instalado</span>
                        <span class="label-hover">Desinstalar</span>
                      </ng-container>
                    }
                  </button>
                } @else {
                  <button
                    class="btn-install"
                    [style.--accent]="colorAcento(modulo.codigo)"
                    [disabled]="procesando() === modulo.codigo"
                    (click)="alternar(modulo)"
                  >
                    @if (procesando() === modulo.codigo) {
                      <mat-spinner diameter="16"></mat-spinner>
                    } @else {
                      Instalar
                    }
                  </button>
                }
              </div>
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .apps-page {
      max-width: 1100px;
      margin: 0 auto;
      padding: 40px 24px 80px;
    }

    .apps-header {
      display: flex;
      align-items: flex-end;
      justify-content: space-between;
      gap: 24px;
      flex-wrap: wrap;
      margin-bottom: 40px;
    }

    .apps-header-text h1 {
      font-size: 1.9rem;
      font-weight: 800;
      margin: 0 0 6px;
      color: #0f172a;
    }

    .marca-link {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      margin-top: 10px;
      font-size: 13px;
      font-weight: 600;
      color: #2563eb;
      text-decoration: none;
    }

    .marca-link:hover {
      text-decoration: underline;
    }

    .apps-header-text p {
      margin: 0;
      color: #64748b;
    }

    .search-box {
      display: flex;
      align-items: center;
      gap: 8px;
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 10px;
      padding: 8px 14px;
      min-width: 260px;
    }

    .search-box mat-icon {
      color: #94a3b8;
      font-size: 20px;
      width: 20px;
      height: 20px;
    }

    .search-box input {
      border: none;
      outline: none;
      font-size: 0.95rem;
      width: 100%;
      background: transparent;
    }

    .state-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 16px;
      padding: 80px 24px;
      color: #64748b;
      text-align: center;
    }

    .apps-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
      gap: 20px;
    }

    .app-card {
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 16px;
      padding: 24px 20px;
      display: flex;
      flex-direction: column;
      align-items: flex-start;
      transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
    }

    .app-card:hover {
      transform: translateY(-4px);
      box-shadow: 0 12px 28px rgba(15, 23, 42, 0.1);
      border-color: transparent;
    }

    .app-card-activo {
      border-color: #c7d2fe;
    }

    .app-icon {
      width: 56px;
      height: 56px;
      border-radius: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 16px;
    }

    .app-icon mat-icon {
      font-size: 28px;
      width: 28px;
      height: 28px;
    }

    .app-card h3 {
      font-size: 1.05rem;
      font-weight: 700;
      margin: 0 0 6px;
      color: #0f172a;
    }

    .app-desc {
      font-size: 0.88rem;
      color: #64748b;
      line-height: 1.45;
      margin: 0 0 20px;
      min-height: 40px;
    }

    .app-action {
      width: 100%;
      margin-top: auto;
    }

    .btn-install {
      width: 100%;
      height: 38px;
      border: none;
      border-radius: 8px;
      background: var(--accent, #2563eb);
      color: white;
      font-weight: 600;
      font-size: 0.88rem;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: opacity 0.2s ease;
    }

    .btn-install:hover {
      opacity: 0.9;
    }

    .btn-install:disabled {
      opacity: 0.6;
      cursor: default;
    }

    .btn-installed {
      width: 100%;
      height: 38px;
      border: 1px solid #bbf7d0;
      border-radius: 8px;
      background: #f0fdf4;
      color: #16a34a;
      font-weight: 600;
      font-size: 0.88rem;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
    }

    .icon-hover,
    .label-hover {
      display: none;
    }

    .btn-installed:hover {
      border-color: #fecaca;
      background: #fef2f2;
      color: #dc2626;
    }

    .btn-installed:hover .icon-default,
    .btn-installed:hover .label-default {
      display: none;
    }

    .btn-installed:hover .icon-hover,
    .btn-installed:hover .label-hover {
      display: inline-flex;
    }

    .btn-installed mat-icon {
      font-size: 18px;
      width: 18px;
      height: 18px;
    }

    .btn-installed:disabled {
      opacity: 0.6;
      cursor: default;
    }

    @media (max-width: 640px) {
      .apps-header {
        flex-direction: column;
        align-items: stretch;
      }

      .search-box {
        min-width: 0;
      }
    }
  `],
})
export class MisModulosComponent implements OnInit {
  private readonly adminService = inject(AdminService);
  private readonly snackBar = inject(MatSnackBar);

  // TODO: reemplazar por el ID de la empresa del usuario logueado
  // (pendiente de que backend defina cómo se obtiene desde el JWT/sesión,
  // en vez de un UUID fijo de demo).
  private readonly empresaId = '00000000-0000-0000-0000-000000000001';

  readonly modulos = signal<ModuloDeEmpresa[]>([]);
  readonly cargando = signal(false);
  readonly error = signal<string | null>(null);
  readonly procesando = signal<string | null>(null);
  readonly busqueda = signal('');

  readonly modulosFiltrados = computed(() => {
    const termino = this.busqueda().trim().toLowerCase();
    if (!termino) {
      return this.modulos();
    }
    return this.modulos().filter(
      (m) => m.nombre.toLowerCase().includes(termino) || m.descripcion.toLowerCase().includes(termino)
    );
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.adminService.getModulosDeEmpresa(this.empresaId).subscribe({
      next: (modulos) => {
        this.modulos.set(modulos);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No pudimos cargar tus módulos. Intenta de nuevo en unos segundos.');
        this.cargando.set(false);
      },
    });
  }

  alternar(modulo: ModuloDeEmpresa): void {
    this.procesando.set(modulo.codigo);
    const observable = modulo.activo
      ? this.adminService.desactivarModulo(this.empresaId, modulo.codigo)
      : this.adminService.activarModulo(this.empresaId, modulo.codigo);

    observable.subscribe({
      next: () => {
        this.modulos.set(
          this.modulos().map((m) => (m.codigo === modulo.codigo ? { ...m, activo: !m.activo } : m))
        );
        this.snackBar.open(
          modulo.activo ? `${modulo.nombre} desinstalado` : `${modulo.nombre} instalado`,
          'Cerrar',
          { duration: 2500 }
        );
        this.procesando.set(null);
      },
      error: () => {
        this.snackBar.open('No se pudo completar la acción. Intenta de nuevo.', 'Cerrar', { duration: 4000 });
        this.procesando.set(null);
      },
    });
  }

  icono(codigo: string): string {
    return (APARIENCIA_MODULO[codigo] ?? APARIENCIA_DEFECTO).icono;
  }

  colorAcento(codigo: string): string {
    return (APARIENCIA_MODULO[codigo] ?? APARIENCIA_DEFECTO).color;
  }

  colorClaro(codigo: string): string {
    const color = this.colorAcento(codigo);
    return `color-mix(in srgb, ${color} 14%, white)`;
  }
}
