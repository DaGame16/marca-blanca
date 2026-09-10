import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LiwaService } from '../../data/liwa.service';
import { LiwaAnalisisItem } from '../../models/liwa.model';
import { LiwaResumenAnalisisIaComponent } from '../analisis-ia-resumen.component';
import { LiwaTablaAnalisisIaComponent } from '../analisis-ia-tabla.component';

// Traducción de components/liwa/AnalisisIAPanel.tsx — dos piezas: el
// resumen (ResumenAnalisisPanel) que va separado arriba de la vista
// "Análisis y casos", y este panel con la tabla completa de casos
// analizados por IA para el rango de fechas elegido.
@Component({
  selector: 'app-liwa-resumen-analisis-panel',
  standalone: true,
  imports: [CommonModule, LiwaResumenAnalisisIaComponent],
  template: `<app-liwa-resumen-analisis-ia [items]="items" [cargando]="cargando" />`,
})
export class LiwaResumenAnalisisPanelComponent implements OnChanges {
  @Input() desde?: string;
  @Input() hasta?: string;

  items: LiwaAnalisisItem[] = [];
  cargando = true;
  private peticion = 0;

  constructor(private readonly liwa: LiwaService) {}

  ngOnChanges(): void {
    void this.cargar();
  }

  private async cargar(): Promise<void> {
    const id = ++this.peticion;
    this.cargando = true;
    try {
      const respuesta = await this.liwa.obtenerTodosLosAnalisis({ desde: this.desde, hasta: this.hasta });
      if (id !== this.peticion) return;
      this.items = Array.isArray(respuesta) ? respuesta : [];
    } catch {
      if (id === this.peticion) this.items = [];
    } finally {
      if (id === this.peticion) this.cargando = false;
    }
  }
}

@Component({
  selector: 'app-liwa-analisis-ia-panel',
  standalone: true,
  imports: [CommonModule, LiwaTablaAnalisisIaComponent],
  template: `
    <section class="tarjeta">
      <header>
        <div>
          <h2>Conversaciones analizadas por IA</h2>
          <p>Resultados del análisis dentro del rango de fechas seleccionado.</p>
        </div>
        <span class="total">{{ items.length }} analizadas</span>
      </header>
      <p class="error" *ngIf="error">{{ error }}</p>
      <app-liwa-tabla-analisis-ia [items]="items" [cargando]="cargando" [mostrarResumen]="mostrarResumen" />
    </section>
  `,
  styles: [`
    .tarjeta { background: #fff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 16px; }
    header { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 8px; margin-bottom: 8px; }
    header h2 { margin: 0; font-size: 1rem; font-weight: 700; color: #1e293b; }
    header p { margin: 2px 0 0; font-size: 0.75rem; color: #64748b; }
    .total { font-size: 0.72rem; color: #64748b; }
    .error { background: #fff1f2; color: #be123c; font-size: 0.72rem; padding: 8px 12px; border-radius: 8px; }
  `],
})
export class LiwaAnalisisIaPanelComponent implements OnChanges {
  @Input() desde?: string;
  @Input() hasta?: string;
  @Input() mostrarResumen = true;

  items: LiwaAnalisisItem[] = [];
  cargando = true;
  error: string | null = null;
  private peticion = 0;

  constructor(private readonly liwa: LiwaService) {}

  ngOnChanges(): void {
    void this.cargar();
  }

  private async cargar(): Promise<void> {
    const id = ++this.peticion;
    this.cargando = true;
    this.error = null;
    try {
      const respuesta = await this.liwa.obtenerTodosLosAnalisis({ desde: this.desde, hasta: this.hasta });
      if (id !== this.peticion) return;
      const todos = Array.isArray(respuesta) ? respuesta : [];
      // Mismo criterio de rango que el resto de la vista: si el backend
      // devuelve casos fuera del periodo (o sin fecha), no cuentan.
      this.items = todos.filter((a) => this.liwa.analisisEnRango(a, this.desde, this.hasta));
    } catch {
      if (id === this.peticion) this.error = 'No fue posible cargar las conversaciones analizadas por IA.';
    } finally {
      if (id === this.peticion) this.cargando = false;
    }
  }
}
