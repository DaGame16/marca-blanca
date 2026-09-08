import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MarcaService } from '../../../../core/identidad-visual/marca.service';
import type { MarcaDeEmpresa } from '../../../../core/identidad-visual/models';

type CodigoTema = 'lateral' | 'centrado' | 'fondo';

interface OpcionTema {
  codigo: CodigoTema;
  numero: number;
  nombre: string;
  descripcion: string;
}

const OPCIONES: OpcionTema[] = [
  {
    codigo: 'lateral',
    numero: 1,
    nombre: 'Panel lateral',
    descripcion: 'Panel de marca a un lado y el formulario al otro. El diseño actual.',
  },
  {
    codigo: 'centrado',
    numero: 2,
    nombre: 'Centrado',
    descripcion: 'Tarjeta centrada con el logo arriba, sin panel lateral. Minimalista.',
  },
  {
    codigo: 'fondo',
    numero: 3,
    nombre: 'Fondo completo',
    descripcion: 'Fondo degradado a pantalla completa con el formulario flotando en el centro.',
  },
];

@Component({
  selector: 'app-selector-tema-login',
  standalone: true,
  imports: [RouterLink, MatIconModule, MatButtonModule, MatProgressSpinnerModule, MatSnackBarModule],
  template: `
    <div class="temas-page">
      <header class="temas-header">
        <h1>Diseño de inicio de sesión</h1>
        <p>
          Elige cómo se ve la pantalla de login de tu empresa. Se guarda en el servidor: todos los
          que inicien sesión en tu empresa lo van a ver así.
        </p>
      </header>

      @if (cargando()) {
        <div class="estado-carga">
          <mat-spinner diameter="32"></mat-spinner>
        </div>
      }

      <div class="temas-grid">
        @for (opcion of opciones; track opcion.codigo) {
          <div class="tema-card" [class.tema-card-activa]="codigoActivo() === opcion.codigo">
            <div class="preview" [class]="'preview-' + opcion.codigo">
              @switch (opcion.codigo) {
                @case ('lateral') {
                  <div class="preview-lateral">
                    <div class="preview-panel"></div>
                    <div class="preview-form">
                      <div class="preview-linea corta"></div>
                      <div class="preview-linea"></div>
                      <div class="preview-linea"></div>
                      <div class="preview-boton"></div>
                    </div>
                  </div>
                }
                @case ('centrado') {
                  <div class="preview-centrado">
                    <div class="preview-tarjeta">
                      <div class="preview-linea corta centrada"></div>
                      <div class="preview-linea"></div>
                      <div class="preview-linea"></div>
                      <div class="preview-boton"></div>
                    </div>
                  </div>
                }
                @case ('fondo') {
                  <div class="preview-fondo">
                    <div class="preview-tarjeta preview-tarjeta-flotante">
                      <div class="preview-linea corta centrada"></div>
                      <div class="preview-linea"></div>
                      <div class="preview-linea"></div>
                      <div class="preview-boton"></div>
                    </div>
                  </div>
                }
              }
            </div>

            <h3>{{ opcion.nombre }}</h3>
            <p class="tema-desc">{{ opcion.descripcion }}</p>

            @if (codigoActivo() === opcion.codigo) {
              <button mat-flat-button disabled class="btn-activo">
                <mat-icon>check_circle</mat-icon>
                En uso
              </button>
            } @else {
              <button mat-stroked-button [disabled]="guardando()" (click)="elegir(opcion)">Usar este diseño</button>
            }
          </div>
        }
      </div>

      <a routerLink="/login" class="ver-login-link" target="_blank">
        <mat-icon inline>open_in_new</mat-icon>
        Ver el login en una pestaña nueva
      </a>
    </div>
  `,
  styles: [`
    .temas-page {
      max-width: 1000px;
      margin: 0 auto;
      padding: 40px 24px 80px;
    }

    .temas-header h1 {
      font-size: 1.9rem;
      font-weight: 800;
      margin: 0 0 6px;
      color: #0f172a;
    }

    .temas-header p {
      margin: 0 0 32px;
      color: #64748b;
      max-width: 560px;
    }

    .temas-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
      gap: 24px;
    }

    .tema-card {
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 16px;
      padding: 18px;
      display: flex;
      flex-direction: column;
    }

    .tema-card-activa {
      border-color: #93c5fd;
      box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
    }

    .preview {
      height: 140px;
      border-radius: 10px;
      overflow: hidden;
      margin-bottom: 14px;
      background: #f1f5f9;
    }

    .preview-lateral {
      display: grid;
      grid-template-columns: 1fr 1fr;
      height: 100%;
    }

    .preview-panel {
      background: linear-gradient(135deg, #1e3a5f 0%, #2563eb 100%);
    }

    .preview-form {
      display: flex;
      flex-direction: column;
      justify-content: center;
      gap: 8px;
      padding: 16px;
      background: #f8fafc;
    }

    .preview-centrado {
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #e2e8f0;
    }

    .preview-fondo {
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(160deg, #0f172a 0%, #1e3a5f 45%, #2563eb 100%);
    }

    .preview-tarjeta {
      width: 70%;
      background: white;
      border-radius: 8px;
      padding: 12px;
      display: flex;
      flex-direction: column;
      gap: 8px;
      box-shadow: 0 4px 12px rgba(15, 23, 42, 0.12);
    }

    .preview-tarjeta-flotante {
      box-shadow: 0 10px 24px rgba(0, 0, 0, 0.35);
    }

    .preview-linea {
      height: 6px;
      border-radius: 3px;
      background: #cbd5e1;
    }

    .preview-linea.corta {
      width: 50%;
      height: 8px;
    }

    .preview-linea.centrada {
      align-self: center;
    }

    .preview-boton {
      height: 10px;
      border-radius: 3px;
      background: #2563eb;
      margin-top: 4px;
    }

    .tema-card h3 {
      font-size: 1rem;
      font-weight: 700;
      margin: 0 0 4px;
      color: #0f172a;
    }

    .tema-desc {
      font-size: 0.85rem;
      color: #64748b;
      line-height: 1.4;
      margin: 0 0 16px;
      min-height: 40px;
    }

    .tema-card button {
      margin-top: auto;
    }

    .btn-activo {
      background: #f0fdf4 !important;
      color: #16a34a !important;
    }

    .ver-login-link {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      margin-top: 32px;
      font-size: 0.85rem;
      font-weight: 600;
      color: #2563eb;
      text-decoration: none;
    }

    .ver-login-link:hover {
      text-decoration: underline;
    }

    .estado-carga {
      display: flex;
      justify-content: center;
      padding: 40px 0;
    }
  `],
})
export class SelectorTemaLoginComponent implements OnInit {
  private readonly marcaService = inject(MarcaService);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly opciones = OPCIONES;
  protected readonly cargando = signal(true);
  protected readonly guardando = signal(false);
  private marcaActual: MarcaDeEmpresa | null = null;

  protected readonly codigoActivo = signal<CodigoTema>('lateral');

  ngOnInit(): void {
    this.marcaService.obtener().subscribe({
      next: (marca) => {
        this.marcaActual = marca;
        const opcion = OPCIONES.find((o) => o.numero === marca.tipoLogin);
        this.codigoActivo.set(opcion?.codigo ?? 'lateral');
        this.cargando.set(false);
      },
      error: () => this.cargando.set(false),
    });
  }

  elegir(opcion: OpcionTema): void {
    this.guardando.set(true);
    const marca: MarcaDeEmpresa = {
      urlLogo: this.marcaActual?.urlLogo ?? null,
      colorPrimario: this.marcaActual?.colorPrimario ?? null,
      colorSecundario: this.marcaActual?.colorSecundario ?? null,
      dominioPropio: this.marcaActual?.dominioPropio ?? null,
      tipoLogin: opcion.numero,
      tipoPantallaPrincipal: this.marcaActual?.tipoPantallaPrincipal ?? null,
    };
    this.marcaService.actualizar(marca).subscribe({
      next: () => {
        this.marcaActual = marca;
        this.codigoActivo.set(opcion.codigo);
        this.guardando.set(false);
        this.snackBar.open(`Diseño "${opcion.nombre}" activado`, 'Cerrar', { duration: 2500 });
      },
      error: () => {
        this.guardando.set(false);
        this.snackBar.open('No se pudo guardar el diseño. Intenta de nuevo.', 'Cerrar', { duration: 4000 });
      },
    });
  }
}
