import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { LiwaService } from '../data/liwa.service';
import { LiwaAnalisisItem } from '../models/liwa.model';
import { LiwaResumenAnalisisIaComponent } from './analisis-ia-resumen.component';
import { LiwaAnalisisDetalleComponent } from './analisis-ia-detalle.component';

const CLAVES_FECHA: (keyof LiwaAnalisisItem)[] = ['procesadoEn', 'creadoEn', 'fechaAnalisis', 'createdAt'];

function fechaDeAnalisis(item: LiwaAnalisisItem): Date | null {
  for (const clave of CLAVES_FECHA) {
    const v = item[clave] as string | undefined;
    if (!v) continue;
    const d = new Date(v);
    if (!isNaN(d.getTime())) return d;
  }
  for (const v of [item.tsPrimerMensaje, item.tsPrimeraRespuesta, item.tsCierre]) {
    if (!v) continue;
    const d = new Date(v);
    if (!isNaN(d.getTime())) return d;
  }
  return null;
}

// Traducción de TablaAnalisisIA en components/liwa/AnalisisIA.tsx: tabla de
// casos analizados por IA con búsqueda, filtros y paginación cliente.
@Component({
  selector: 'app-liwa-tabla-analisis-ia',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, LiwaResumenAnalisisIaComponent, LiwaAnalisisDetalleComponent],
  template: `
    <div>
      <app-liwa-resumen-analisis-ia *ngIf="mostrarResumen" [items]="items" [cargando]="cargando" />

      <div class="filtros">
        <div class="buscador">
          <mat-icon>search</mat-icon>
          <input [(ngModel)]="busqueda" (ngModelChange)="pagina = 1" placeholder="Buscar por motivo, desenlace, contacto…" />
        </div>
        <select [(ngModel)]="filtroResultado" (ngModelChange)="pagina = 1">
          <option value="TODOS">Resultado: todos</option>
          <option value="resuelto">Resuelto</option>
          <option value="no_resuelto">No resuelto</option>
          <option value="escalado">Escalado</option>
          <option value="">Sin clasificar</option>
        </select>
        <select [(ngModel)]="filtroSentimiento" (ngModelChange)="pagina = 1">
          <option value="TODOS">Sentimiento: todos</option>
          <option value="positivo">Positivo</option>
          <option value="neutral">Neutral</option>
          <option value="negativo">Negativo</option>
        </select>
        <select [(ngModel)]="filtroFcr" (ngModelChange)="pagina = 1">
          <option value="TODOS">FCR: todos</option>
          <option value="SI">Resueltos a la primera</option>
          <option value="NO">Sin resolver a la primera</option>
        </select>
        <select [(ngModel)]="filtroAbandonadoPor" (ngModelChange)="pagina = 1">
          <option value="TODOS">Abandono: todos</option>
          <option value="asesor">↳ Abandonado por asesor</option>
          <option value="cliente">↳ Abandonado por cliente</option>
          <option value="ninguno">Sin abandono</option>
        </select>
        <span class="contador">{{ filtrados.length }} de {{ items.length }} conversaciones</span>
      </div>

      <div class="tabla-wrap">
        <table>
          <thead>
            <tr>
              <th>Motivo</th><th>Contacto</th><th>Resultado</th><th>Abandono</th><th>Sentimiento</th><th>FCR</th><th>Fecha</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let a of visibles" (click)="seleccionado = a" class="fila">
              <td>
                <p class="motivo">{{ a.resumenMotivo || etiqueta(a.motivoContacto) || 'Sin resumen' }}</p>
                <p class="desenlace" *ngIf="a.resumenDesenlace">{{ a.resumenDesenlace }}</p>
              </td>
              <td>{{ a.idContacto || '—' }}</td>
              <td>
                <span class="badge" [class]="'res-' + (a.resultado || 'na')">{{ a.resultado || 'Sin clasificar' }}</span>
              </td>
              <td>
                <span class="badge" *ngIf="a.abandono && a.abandonadoPor === 'asesor'" class="badge orange">↳ Asesor</span>
                <span class="badge" *ngIf="a.abandono && a.abandonadoPor === 'cliente'" class="badge">↳ Cliente</span>
                <span class="badge ok" *ngIf="!a.abandono">No abandonado</span>
              </td>
              <td><span class="badge" [class]="'sent-' + a.sentimientoFinal">{{ a.sentimientoFinal }}</span></td>
              <td>{{ a.fcr ? 'Sí' : 'No' }}</td>
              <td>{{ fecha(a) | date: 'dd/MM HH:mm' }}</td>
            </tr>
            <tr *ngIf="!cargando && !filtrados.length">
              <td colspan="7" class="vacio">{{ items.length === 0 ? 'Sin conversaciones analizadas con IA en el periodo.' : 'Ninguna conversación coincide con los filtros.' }}</td>
            </tr>
          </tbody>
        </table>
        <div class="cargando" *ngIf="cargando"><mat-icon class="spin">progress_activity</mat-icon> Cargando análisis IA…</div>
      </div>

      <div class="paginacion" *ngIf="!cargando && filtrados.length">
        <p>Página {{ paginaActual }} de {{ totalPaginas }} · {{ filtrados.length }} análisis en total</p>
        <div class="botones">
          <button type="button" [disabled]="paginaActual <= 1" (click)="pagina = paginaActual - 1">Anterior</button>
          <span class="actual">{{ paginaActual }}</span>
          <button type="button" [disabled]="paginaActual >= totalPaginas" (click)="pagina = paginaActual + 1">Siguiente</button>
        </div>
      </div>

      <app-liwa-analisis-detalle *ngIf="seleccionado" [item]="seleccionado" (onClose)="seleccionado = null" />
    </div>
  `,
  styles: [`
    .filtros { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; margin: 12px 0; }
    .buscador { position: relative; flex: 1; min-width: 200px; }
    .buscador mat-icon { position: absolute; left: 8px; top: 50%; transform: translateY(-50%); font-size: 15px; width: 15px; height: 15px; color: #94a3b8; }
    .buscador input { width: 100%; padding: 7px 10px 7px 28px; font-size: 0.75rem; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; box-sizing: border-box; }
    select { padding: 7px 8px; font-size: 0.72rem; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; }
    .contador { font-size: 0.68rem; color: #94a3b8; white-space: nowrap; }
    .tabla-wrap { overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; font-size: 0.78rem; min-width: 720px; }
    th { text-align: left; padding: 8px 12px; font-size: 0.62rem; font-weight: 700; text-transform: uppercase; color: #94a3b8; border-bottom: 1px solid #f1f5f9; }
    td { padding: 8px 12px; border-bottom: 1px solid #f8fafc; vertical-align: top; }
    .fila { cursor: pointer; }
    .fila:hover { background: #faf5ff; }
    .motivo { margin: 0; font-weight: 600; font-size: 0.75rem; color: #1e293b; }
    .desenlace { margin: 2px 0 0; font-size: 0.65rem; color: #94a3b8; }
    .badge { display: inline-flex; padding: 2px 8px; border-radius: 6px; font-size: 0.62rem; font-weight: 600; background: #f1f5f9; color: #64748b; text-transform: capitalize; }
    .badge.res-resuelto, .badge.ok, .badge.sent-positivo { background: #ecfdf5; color: #047857; }
    .badge.res-no_resuelto, .badge.sent-negativo { background: #fff1f2; color: #be123c; }
    .badge.res-escalado, .badge.sent-neutral { background: #fffbeb; color: #92400e; }
    .badge.orange { background: #fff7ed; color: #c2410c; }
    .vacio { text-align: center; color: #94a3b8; padding: 32px; }
    .cargando { display: flex; align-items: center; justify-content: center; gap: 8px; padding: 24px; color: #94a3b8; font-size: 0.75rem; }
    .spin { animation: spin 1s linear infinite; }
    @keyframes spin { to { transform: rotate(360deg); } }
    .paginacion { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 8px; margin-top: 10px; }
    .paginacion p { margin: 0; font-size: 0.68rem; color: #94a3b8; }
    .botones { display: flex; align-items: center; gap: 6px; }
    .botones button { border: none; background: #f1f5f9; color: #475569; font-size: 0.7rem; font-weight: 600; padding: 6px 12px; border-radius: 8px; cursor: pointer; }
    .botones button:disabled { opacity: 0.4; cursor: not-allowed; }
    .actual { padding: 6px 10px; background: #f5f3ff; color: #7c3aed; border: 1px solid #ede9fe; border-radius: 8px; font-size: 0.7rem; font-weight: 700; }
  `],
})
export class LiwaTablaAnalisisIaComponent implements OnChanges {
  @Input() items: LiwaAnalisisItem[] = [];
  @Input() cargando = false;
  @Input() mostrarResumen = true;

  seleccionado: LiwaAnalisisItem | null = null;
  busqueda = '';
  filtroResultado = 'TODOS';
  filtroSentimiento = 'TODOS';
  filtroFcr = 'TODOS';
  filtroAbandonadoPor = 'TODOS';
  pagina = 1;
  readonly POR_PAGINA = 15;

  constructor(private readonly liwa: LiwaService) {}

  ngOnChanges(): void {
    this.pagina = 1;
  }

  etiqueta(motivo: string | null | undefined): string {
    return this.liwa.etiquetaMotivo(motivo);
  }

  fecha(a: LiwaAnalisisItem): Date | null {
    return fechaDeAnalisis(a);
  }

  private fechaMs(a: LiwaAnalisisItem): number {
    const ms = fechaDeAnalisis(a)?.getTime() ?? new Date(a.createdAt || 0).getTime();
    return Number.isFinite(ms) ? ms : 0;
  }

  get filtrados(): LiwaAnalisisItem[] {
    const q = this.busqueda.trim().toLowerCase();
    return [...this.items]
      .filter((a) => {
        if (this.filtroResultado !== 'TODOS' && (a.resultado || '') !== this.filtroResultado) return false;
        if (this.filtroSentimiento !== 'TODOS' && (a.sentimientoFinal || '') !== this.filtroSentimiento) return false;
        if (this.filtroFcr === 'SI' && !a.fcr) return false;
        if (this.filtroFcr === 'NO' && a.fcr) return false;
        if (this.filtroAbandonadoPor === 'asesor' && a.abandonadoPor !== 'asesor') return false;
        if (this.filtroAbandonadoPor === 'cliente' && a.abandonadoPor !== 'cliente') return false;
        if (this.filtroAbandonadoPor === 'ninguno' && a.abandono === true) return false;
        if (q) {
          const texto = `${a.resumenMotivo || ''} ${a.resumenDesenlace || ''} ${a.motivoContacto || ''} ${this.etiqueta(a.motivoContacto)} ${a.idContacto || ''}`.toLowerCase();
          if (!texto.includes(q)) return false;
        }
        return true;
      })
      .sort((a, b) => this.fechaMs(b) - this.fechaMs(a));
  }

  get totalPaginas(): number {
    return Math.max(1, Math.ceil(this.filtrados.length / this.POR_PAGINA));
  }

  get paginaActual(): number {
    return Math.min(this.pagina, this.totalPaginas);
  }

  get visibles(): LiwaAnalisisItem[] {
    const p = this.paginaActual;
    return this.filtrados.slice((p - 1) * this.POR_PAGINA, p * this.POR_PAGINA);
  }
}
