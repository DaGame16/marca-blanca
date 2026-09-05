import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AdminService } from '../../../../core/admin/admin.service';
import { Modulo, ModuloDeEmpresa } from '../../../../core/admin/models';

@Component({
  selector: 'app-modulos-admin',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatSlideToggleModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  template: `
    <div class="admin-container">
      <h1>Administración de Módulos</h1>

      <!-- Sección: Catálogo de Módulos -->
      <mat-card class="section-card">
        <mat-card-header>
          <mat-card-title>
            <mat-icon>apps</mat-icon>
            Catálogo de Módulos Disponibles
          </mat-card-title>
        </mat-card-header>
        <mat-card-content>
          @if (loadingModulos()) {
            <div class="loading-container">
              <mat-spinner diameter="50"></mat-spinner>
              <p>Cargando módulos...</p>
            </div>
          } @else if (errorModulos()) {
            <div class="error-container">
              <mat-icon color="warn">error</mat-icon>
              <p>{{ errorModulos() }}</p>
              <button mat-raised-button color="primary" (click)="cargarModulos()">
                Reintentar
              </button>
            </div>
          } @else {
            <div class="modulos-grid">
              @for (modulo of modulos(); track modulo.id) {
                <mat-card class="modulo-card">
                  <mat-card-header>
                    <mat-card-title>{{ modulo.nombre }}</mat-card-title>
                    <mat-card-subtitle>{{ modulo.codigo }}</mat-card-subtitle>
                  </mat-card-header>
                  <mat-card-content>
                    <p>{{ modulo.descripcion }}</p>
                  </mat-card-content>
                </mat-card>
              }
            </div>
          }
        </mat-card-content>
      </mat-card>

      <!-- Sección: Módulos por Empresa (Demo) -->
      <mat-card class="section-card">
        <mat-card-header>
          <mat-card-title>
            <mat-icon>business</mat-icon>
            Gestión de Módulos por Empresa (Demo)
          </mat-card-title>
          <mat-card-subtitle>
            Empresa de demostración - ID: {{ empresaDemoId }}
          </mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          @if (loadingModulosEmpresa()) {
            <div class="loading-container">
              <mat-spinner diameter="50"></mat-spinner>
              <p>Cargando módulos de la empresa...</p>
            </div>
          } @else if (errorModulosEmpresa()) {
            <div class="error-container">
              <mat-icon color="warn">error</mat-icon>
              <p>{{ errorModulosEmpresa() }}</p>
              <button mat-raised-button color="primary" (click)="cargarModulosDeEmpresa()">
                Reintentar
              </button>
            </div>
          } @else {
            <div class="info-box">
              <mat-icon>info</mat-icon>
              <p>Activa o desactiva los módulos disponibles para esta empresa</p>
            </div>

            <table mat-table [dataSource]="modulosEmpresa()" class="modulos-table">
              <!-- Columna: Nombre -->
              <ng-container matColumnDef="nombre">
                <th mat-header-cell *matHeaderCellDef>Módulo</th>
                <td mat-cell *matCellDef="let modulo">
                  <div class="modulo-info">
                    <strong>{{ modulo.nombre }}</strong>
                    <span class="codigo">{{ modulo.codigo }}</span>
                  </div>
                </td>
              </ng-container>

              <!-- Columna: Descripción -->
              <ng-container matColumnDef="descripcion">
                <th mat-header-cell *matHeaderCellDef>Descripción</th>
                <td mat-cell *matCellDef="let modulo">{{ modulo.descripcion }}</td>
              </ng-container>

              <!-- Columna: Estado -->
              <ng-container matColumnDef="activo">
                <th mat-header-cell *matHeaderCellDef>Estado</th>
                <td mat-cell *matCellDef="let modulo">
                  <mat-slide-toggle
                    [checked]="modulo.activo"
                    (change)="toggleModulo(modulo)"
                    [disabled]="procesando()"
                    color="primary">
                    {{ modulo.activo ? 'Activo' : 'Inactivo' }}
                  </mat-slide-toggle>
                </td>
              </ng-container>

              <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
              <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
            </table>
          }
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .admin-container {
      padding: 24px;
      max-width: 1400px;
      margin: 0 auto;
    }

    h1 {
      margin: 0 0 24px;
      color: #333;
    }

    .section-card {
      margin-bottom: 24px;
    }

    .section-card mat-card-header {
      margin-bottom: 16px;
    }

    .section-card mat-card-title {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .loading-container,
    .error-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 48px 24px;
      gap: 16px;
    }

    .error-container mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
    }

    .modulos-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: 16px;
    }

    .modulo-card {
      transition: transform 0.2s ease, box-shadow 0.2s ease;
    }

    .modulo-card:hover {
      transform: translateY(-4px);
      box-shadow: 0 4px 16px rgba(0,0,0,0.1);
    }

    .info-box {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 16px;
      background: #e3f2fd;
      border-radius: 4px;
      margin-bottom: 24px;
    }

    .info-box mat-icon {
      color: #1976d2;
    }

    .info-box p {
      margin: 0;
      color: #1565c0;
    }

    .modulos-table {
      width: 100%;
    }

    .modulo-info {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .codigo {
      font-size: 12px;
      color: #666;
      font-family: monospace;
    }

    @media (max-width: 768px) {
      .admin-container {
        padding: 16px;
      }

      .modulos-grid {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class ModulosAdminComponent implements OnInit {
  private readonly adminService = inject(AdminService);
  private readonly snackBar = inject(MatSnackBar);

  // Catálogo de módulos
  readonly modulos = signal<Modulo[]>([]);
  readonly loadingModulos = signal(false);
  readonly errorModulos = signal<string | null>(null);

  // Módulos de empresa (demo)
  readonly modulosEmpresa = signal<ModuloDeEmpresa[]>([]);
  readonly loadingModulosEmpresa = signal(false);
  readonly errorModulosEmpresa = signal<string | null>(null);
  readonly procesando = signal(false);

  // Obtener el ID de la empresa desde el contexto o parámetros de ruta
  readonly empresaDemoId = '00000000-0000-0000-0000-000000000001';

  readonly displayedColumns = ['nombre', 'descripcion', 'activo'];

  ngOnInit(): void {
    this.cargarModulos();
    this.cargarModulosDeEmpresa();
  }

  cargarModulos(): void {
    this.loadingModulos.set(true);
    this.errorModulos.set(null);

    this.adminService.getModulos().subscribe({
      next: (modulos) => {
        this.modulos.set(modulos);
        this.loadingModulos.set(false);
      },
      error: (error) => {
        console.error('Error al cargar módulos:', error);
        this.errorModulos.set('No se pudieron cargar los módulos. Verifica que el backend esté corriendo y la clave de admin sea correcta.');
        this.loadingModulos.set(false);
      }
    });
  }

  cargarModulosDeEmpresa(): void {
    this.loadingModulosEmpresa.set(true);
    this.errorModulosEmpresa.set(null);

    this.adminService.getModulosDeEmpresa(this.empresaDemoId).subscribe({
      next: (modulos) => {
        this.modulosEmpresa.set(modulos);
        this.loadingModulosEmpresa.set(false);
      },
      error: (error) => {
        console.error('Error al cargar módulos de empresa:', error);
        this.errorModulosEmpresa.set('No se pudieron cargar los módulos de la empresa. Asegúrate de que existan datos de prueba en la base de datos.');
        this.loadingModulosEmpresa.set(false);
      }
    });
  }

  toggleModulo(modulo: ModuloDeEmpresa): void {
    this.procesando.set(true);
    const accion = modulo.activo ? 'desactivar' : 'activar';
    const observable = modulo.activo
      ? this.adminService.desactivarModulo(this.empresaDemoId, modulo.codigo)
      : this.adminService.activarModulo(this.empresaDemoId, modulo.codigo);

    observable.subscribe({
      next: () => {
        // Actualizar el estado localmente
        const modulosActualizados = this.modulosEmpresa().map(m =>
          m.codigo === modulo.codigo ? { ...m, activo: !m.activo } : m
        );
        this.modulosEmpresa.set(modulosActualizados);

        this.snackBar.open(
          `Módulo ${modulo.nombre} ${modulo.activo ? 'desactivado' : 'activado'} exitosamente`,
          'Cerrar',
          { duration: 3000 }
        );
        this.procesando.set(false);
      },
      error: (error) => {
        console.error(`Error al ${accion} módulo:`, error);
        this.snackBar.open(
          `Error al ${accion} el módulo. Intenta nuevamente.`,
          'Cerrar',
          { duration: 5000 }
        );
        this.procesando.set(false);
      }
    });
  }
}
