import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MisModulosService } from './mis-modulos.service';
import { ModuloDeEmpresa } from '../../../../core/admin/models';
import { CODIGOS_EXCLUIDOS, colorAcento, colorClaro, icono } from './modulo-apariencia';

@Component({
  selector: 'app-instalar-modulos',
  standalone: true,
  imports: [FormsModule, RouterLink, MatIconModule, MatButtonModule, MatProgressSpinnerModule, MatSnackBarModule],
  template: `
    <div class="apps-page">
      <header class="apps-header">
        <div class="apps-header-text">
          <a routerLink="/mis-modulos" class="volver-link">
            <mat-icon inline>arrow_back</mat-icon>
            Volver a Mis módulos
          </a>
          <h1>Instalar módulos</h1>
          <p>Módulos disponibles que todavía no has activado en tu empresa.</p>
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
          @if (busqueda()) {
            <mat-icon>search_off</mat-icon>
            <p>No encontramos módulos que coincidan con "{{ busqueda() }}"</p>
          } @else {
            <mat-icon>check_circle</mat-icon>
            <p>Ya tienes todos los módulos disponibles instalados.</p>
          }
        </div>
      } @else {
        <div class="apps-grid">
          @for (modulo of modulosFiltrados(); track modulo.codigo) {
            <div class="app-card">
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
                <button
                  class="btn-install"
                  [style.--accent]="colorAcento(modulo.codigo)"
                  [disabled]="procesando() === modulo.codigo"
                  (click)="instalar(modulo)"
                >
                  @if (procesando() === modulo.codigo) {
                    <mat-spinner diameter="16"></mat-spinner>
                  } @else {
                    Instalar
                  }
                </button>
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

    .volver-link {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      font-weight: 600;
      color: #2563eb;
      text-decoration: none;
      margin-bottom: 10px;
    }

    .volver-link:hover {
      text-decoration: underline;
    }

    .apps-header-text h1 {
      font-size: 1.9rem;
      font-weight: 800;
      margin: 0 0 6px;
      color: #0f172a;
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
      display: flex;
      flex-direction: column;
      gap: 8px;
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
export class InstalarModulosComponent implements OnInit {
  private readonly misModulosService = inject(MisModulosService);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly icono = icono;
  protected readonly colorAcento = colorAcento;
  protected readonly colorClaro = colorClaro;

  readonly modulos = signal<ModuloDeEmpresa[]>([]);
  readonly cargando = signal(false);
  readonly error = signal<string | null>(null);
  readonly procesando = signal<string | null>(null);
  readonly busqueda = signal('');

  // Solo los modulos que la empresa todavia NO tiene activos -- una vez
  // instalado, desaparece de aca y pasa a "Mis módulos".
  readonly modulosFiltrados = computed(() => {
    const termino = this.busqueda().trim().toLowerCase();
    const disponibles = this.modulos().filter((m) => !m.activo);
    if (!termino) {
      return disponibles;
    }
    return disponibles.filter(
      (m) => m.nombre.toLowerCase().includes(termino) || m.descripcion.toLowerCase().includes(termino)
    );
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.misModulosService.listar().subscribe({
      next: (modulos: ModuloDeEmpresa[]) => {
        this.modulos.set(modulos.filter((m) => !CODIGOS_EXCLUIDOS.has(m.codigo)));
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No pudimos cargar los módulos disponibles. Intenta de nuevo en unos segundos.');
        this.cargando.set(false);
      },
    });
  }

  instalar(modulo: ModuloDeEmpresa): void {
    this.procesando.set(modulo.codigo);
    this.misModulosService.activar(modulo.codigo).subscribe({
      next: () => {
        this.modulos.set(this.modulos().map((m) => (m.codigo === modulo.codigo ? { ...m, activo: true } : m)));
        this.snackBar.open(`${modulo.nombre} instalado`, 'Cerrar', { duration: 2500 });
        this.procesando.set(null);
      },
      error: () => {
        this.snackBar.open('No se pudo completar la acción. Intenta de nuevo.', 'Cerrar', { duration: 4000 });
        this.procesando.set(null);
      },
    });
  }
}
