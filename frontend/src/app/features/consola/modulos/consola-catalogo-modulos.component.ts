import { Component, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { ConsolaConfigService } from '../../../core/consola/consola-config.service';
import { ModuloCatalogo } from '../../../core/consola/config-plataforma.models';
import { ConsolaNavComponent } from '../nav/consola-nav.component';

/** CRUD del catálogo de módulos de la plataforma (código, nombre, descripción, precio). */
@Component({
  selector: 'app-consola-catalogo-modulos',
  standalone: true,
  imports: [
    DecimalPipe,
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatSnackBarModule,
    MatTableModule,
    ConsolaNavComponent,
  ],
  template: `
    <div class="marco">
      <app-consola-nav />

      <section class="tarjeta">
        <div class="cab">
          <h1>Catálogo de módulos <span class="conteo">{{ modulos().length }}</span></h1>
          @if (editando()) {
            <button mat-button type="button" (click)="nuevo()">
              <mat-icon>add</mat-icon> Nuevo módulo
            </button>
          }
        </div>

        @if (cargando()) {
          <mat-progress-bar mode="indeterminate" />
        }

        <form [formGroup]="form" (ngSubmit)="guardar()" class="form">
          <div class="grid">
            <mat-form-field appearance="outline">
              <mat-label>Código</mat-label>
              <input matInput formControlName="codigo" [readonly]="editando() !== null" />
              <mat-hint>minúsculas, sin espacios</mat-hint>
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Nombre</mat-label>
              <input matInput formControlName="nombre" />
            </mat-form-field>
            <mat-form-field appearance="outline" class="ancho">
              <mat-label>Descripción</mat-label>
              <input matInput formControlName="descripcion" />
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Precio mensual</mat-label>
              <input matInput type="number" min="0" step="1000" formControlName="precio" />
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Moneda</mat-label>
              <input matInput formControlName="moneda" maxlength="3" />
            </mat-form-field>
          </div>
          <div class="acciones">
            <button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || guardando()">
              {{ editando() ? 'Guardar cambios' : 'Crear módulo' }}
            </button>
            @if (editando()) {
              <button mat-button type="button" (click)="nuevo()">Cancelar</button>
            }
          </div>
        </form>

        <div class="tabla-scroll">
          <table mat-table [dataSource]="modulos()">
            <ng-container matColumnDef="modulo">
              <th mat-header-cell *matHeaderCellDef>Módulo</th>
              <td mat-cell *matCellDef="let m">
                <span class="nombre">{{ m.nombre }}</span>
                <span class="codigo">{{ m.codigo }}</span>
              </td>
            </ng-container>
            <ng-container matColumnDef="descripcion">
              <th mat-header-cell *matHeaderCellDef>Descripción</th>
              <td mat-cell *matCellDef="let m">{{ m.descripcion || '—' }}</td>
            </ng-container>
            <ng-container matColumnDef="precio">
              <th mat-header-cell *matHeaderCellDef>Precio / mes</th>
              <td mat-cell *matCellDef="let m">{{ m.precio | number: '1.0-2' }} {{ m.moneda }}</td>
            </ng-container>
            <ng-container matColumnDef="acciones">
              <th mat-header-cell *matHeaderCellDef></th>
              <td mat-cell *matCellDef="let m">
                <button mat-icon-button type="button" (click)="editar(m)" aria-label="Editar">
                  <mat-icon>edit</mat-icon>
                </button>
                <button mat-icon-button type="button" (click)="eliminar(m)" aria-label="Eliminar">
                  <mat-icon>delete_outline</mat-icon>
                </button>
              </td>
            </ng-container>
            <tr mat-header-row *matHeaderRowDef="columnas"></tr>
            <tr mat-row *matRowDef="let fila; columns: columnas"></tr>
          </table>
        </div>
      </section>
    </div>
  `,
  styles: [
    `
      :host { display: block; min-height: 100vh; background: #f1f5f9; }
      .marco { max-width: 960px; margin: 0 auto; padding: 24px; }
      .tarjeta { background: #fff; border: 1px solid #e2e8f0; border-radius: 14px; padding: 20px 22px; }
      .cab { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
      h1 { font-size: 1.2rem; font-weight: 700; margin: 0; color: #0f172a; }
      .conteo {
        display: inline-block;
        margin-left: 8px;
        font-size: 0.8rem;
        font-weight: 600;
        color: #0e7490;
        background: #ecfeff;
        border-radius: 999px;
        padding: 2px 9px;
      }
      .form {
        border: 1px solid #e2e8f0;
        border-radius: 10px;
        padding: 16px;
        margin: 12px 0 20px;
        background: #f8fafc;
      }
      .grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 4px 16px; }
      .ancho { grid-column: 1 / -1; }
      mat-form-field { width: 100%; }
      .acciones { display: flex; gap: 8px; align-items: center; }
      .tabla-scroll { overflow-x: auto; }
      table { width: 100%; }
      .nombre { font-weight: 600; color: #0f172a; }
      .codigo { display: block; font-size: 0.75rem; color: #94a3b8; }
      @media (max-width: 620px) { .grid { grid-template-columns: 1fr; } }
    `,
  ],
})
export class ConsolaCatalogoModulosComponent {
  private readonly fb = inject(FormBuilder);
  private readonly config = inject(ConsolaConfigService);
  private readonly snack = inject(MatSnackBar);

  protected readonly columnas = ['modulo', 'descripcion', 'precio', 'acciones'];
  protected readonly modulos = signal<ModuloCatalogo[]>([]);
  protected readonly cargando = signal(false);
  protected readonly guardando = signal(false);
  protected readonly editando = signal<string | null>(null); // id del módulo en edición

  protected readonly form = this.fb.nonNullable.group({
    codigo: ['', [Validators.required, Validators.pattern(/^[a-z0-9_-]+$/)]],
    nombre: ['', [Validators.required]],
    descripcion: [''],
    precio: [0, [Validators.required, Validators.min(0)]],
    moneda: ['COP', [Validators.required, Validators.pattern(/^[A-Za-z]{3}$/)]],
  });

  constructor() {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.config.listarModulos().subscribe({
      next: (lista) => this.modulos.set(lista),
      error: (e: HttpErrorResponse) => this.snack.open(this.mensaje(e), 'Cerrar', { duration: 4000 }),
      complete: () => this.cargando.set(false),
    });
  }

  nuevo(): void {
    this.editando.set(null);
    this.form.reset({ codigo: '', nombre: '', descripcion: '', precio: 0, moneda: 'COP' });
  }

  editar(m: ModuloCatalogo): void {
    this.editando.set(m.id);
    this.form.setValue({
      codigo: m.codigo,
      nombre: m.nombre,
      descripcion: m.descripcion ?? '',
      precio: m.precio,
      moneda: m.moneda,
    });
  }

  guardar(): void {
    if (this.form.invalid) {
      return;
    }
    this.guardando.set(true);
    const v = this.form.getRawValue();
    const payload = {
      codigo: v.codigo,
      nombre: v.nombre,
      descripcion: v.descripcion.trim() || null,
      precio: Number(v.precio),
      moneda: v.moneda.toUpperCase(),
    };
    const id = this.editando();
    const op$ = id ? this.config.actualizarModulo(id, payload) : this.config.crearModulo(payload);

    op$.subscribe({
      next: () => {
        this.snack.open(id ? 'Módulo actualizado' : 'Módulo creado', 'OK', { duration: 2500 });
        this.nuevo();
        this.cargar();
      },
      error: (e: HttpErrorResponse) => this.snack.open(this.mensaje(e), 'Cerrar', { duration: 4000 }),
      complete: () => this.guardando.set(false),
    });
  }

  eliminar(m: ModuloCatalogo): void {
    if (!confirm(`¿Eliminar el módulo "${m.nombre}"?`)) {
      return;
    }
    this.config.eliminarModulo(m.id).subscribe({
      next: () => {
        this.snack.open('Módulo eliminado', 'OK', { duration: 2500 });
        if (this.editando() === m.id) {
          this.nuevo();
        }
        this.cargar();
      },
      error: (e: HttpErrorResponse) => this.snack.open(this.mensaje(e), 'Cerrar', { duration: 4000 }),
    });
  }

  private mensaje(e: HttpErrorResponse): string {
    if (e.status === 0) {
      return 'No hay conexión con el servidor.';
    }
    if (e.status === 409) {
      return (e.error as { mensaje?: string } | null)?.mensaje ?? 'Ese código ya existe o el módulo está en uso.';
    }
    const backend = (e.error as { mensaje?: string } | null)?.mensaje;
    return backend || 'No se pudo completar la operación.';
  }
}
