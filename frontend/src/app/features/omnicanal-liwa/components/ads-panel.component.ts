import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { LiwaService } from '../data/liwa.service';
import { LiwaAnalisisItem, LiwaChat, LiwaReporteAds } from '../models/liwa.model';
import { LiwaAnalisisDetalleComponent } from './analisis-ia-detalle.component';

const COLOR_RESULTADO: Record<string, string> = {
  resuelto: '#16a34a', no_resuelto: '#dc2626', escalado: '#f59e0b', abandonado: '#64748b',
};
const COLOR_MOTIVO: Record<string, string> = {
  soporte: '#2563eb', ventas: '#16a34a', facturacion: '#f59e0b', reconexion: '#f97316',
  pqr: '#dc2626', cobertura: '#06b6d4', informacion: '#64748b',
};
const COLOR_SENTIMIENTO: Record<string, string> = { positivo: '#16a34a', neutral: '#64748b', negativo: '#dc2626' };
const NOMBRES_BOT_SET = new Set(['yo', 'bot']);

interface Barra { clave: string; label: string; total: number; color: string; }

// Traducción de components/liwa/AdsPanel.tsx — casos confirmados como
// provenientes de Meta Ads (campo vieneDeAds), con desglose por motivo,
// resultado y sentimiento, más la tabla filtrable/paginada de casos.
@Component({
  selector: 'app-liwa-ads-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, LiwaAnalisisDetalleComponent],
  template: `
    <section class="ads">
      <header class="hero">
        <div class="icono"><mat-icon>campaign</mat-icon></div>
        <div>
          <h2>Clientes desde Meta Ads</h2>
          <p>Conversaciones confirmadas desde pauta de Meta (Facebook / Instagram) · {{ textoRango }}</p>
        </div>
      </header>

      <div class="aviso info" *ngIf="cargando"><mat-icon class="spin">progress_activity</mat-icon> Cargando datos de Ads...</div>
      <div class="aviso error" *ngIf="error"><mat-icon>warning</mat-icon> {{ error }}</div>

      <div class="kpis">
        <div class="kpi blue">
          <span>Casos de Ads</span>
          <p class="valor">{{ totalAdsReporte }}</p>
          <p class="sub">confirmados desde Meta Ads</p>
        </div>
        <div class="kpi white">
          <span>Total del período</span>
          <p class="valor">{{ totalCasosRango }}</p>
          <p class="sub">casos analizados por IA</p>
        </div>
        <div class="kpi indigo">
          <span>% de Ads</span>
          <p class="valor">{{ pctAds }}%</p>
          <p class="sub">de las conversaciones vienen de pauta</p>
        </div>
      </div>

      <div class="graficas">
        <div class="tarjeta">
          <h3>Por motivo</h3>
          <p class="hint">Clic filtra la tabla de abajo</p>
          <ng-container *ngIf="porMotivo.length; else sinDatos">
            <button type="button" class="barra" *ngFor="let b of porMotivo" [class.activo]="filtroMotivo === b.clave" (click)="toggleMotivo(b.clave)">
              <div class="fila"><span>{{ b.label }}</span><strong>{{ b.total }}</strong></div>
              <div class="track"><span [style.width.%]="pct(b.total, casosAds.length)" [style.background]="b.color"></span></div>
            </button>
          </ng-container>
        </div>
        <div class="tarjeta">
          <h3>Por resultado</h3>
          <p class="hint">Clic filtra la tabla de abajo</p>
          <ng-container *ngIf="porResultado.length; else sinDatos">
            <button type="button" class="barra" *ngFor="let b of porResultado" [class.activo]="filtroResultado === b.clave" (click)="toggleResultado(b.clave)">
              <div class="fila"><span>{{ b.label }}</span><strong>{{ b.total }}</strong></div>
              <div class="track"><span [style.width.%]="pct(b.total, casosAds.length)" [style.background]="b.color"></span></div>
            </button>
          </ng-container>
        </div>
        <div class="tarjeta">
          <h3>Sentimiento final</h3>
          <p class="hint">Cómo termina la conversación del cliente de ads</p>
          <ng-container *ngIf="porSentimiento.length; else sinDatos">
            <div class="barra sin-clic" *ngFor="let b of porSentimiento">
              <div class="fila"><span>{{ b.label }}</span><strong>{{ b.total }}</strong></div>
              <div class="track"><span [style.width.%]="pct(b.total, casosAds.length)" [style.background]="b.color"></span></div>
            </div>
          </ng-container>
        </div>
      </div>
      <ng-template #sinDatos><p class="vacio">Sin casos en el periodo.</p></ng-template>

      <div class="tabla-card" id="ads-tabla">
        <div class="tabla-header">
          <div class="titulo-tabla">
            <h3>Casos de Meta Ads</h3>
            <span class="contador">{{ visibles.length }}</span>
            <button type="button" class="limpiar" *ngIf="hayFiltros" (click)="limpiarFiltros()">Limpiar filtros</button>
          </div>
          <input [(ngModel)]="busqueda" (ngModelChange)="pagina = 1" placeholder="Buscar por contacto, asesor, motivo o resultado" />
        </div>

        <div class="chips" *ngIf="hayFiltros">
          <span class="chip blue" *ngIf="filtroMotivo !== null">
            Motivo: {{ filtroMotivo ? etiqueta(filtroMotivo) : 'Sin clasificar' }}
            <button type="button" (click)="filtroMotivo = null">✕</button>
          </span>
          <span class="chip indigo" *ngIf="filtroResultado !== null">
            Resultado: {{ filtroResultado ? filtroResultado.replace('_',' ') : 'Sin clasificar' }}
            <button type="button" (click)="filtroResultado = null">✕</button>
          </span>
        </div>

        <div class="tabla-wrap">
          <table>
            <thead>
              <tr><th>Archivada</th><th>Contacto</th><th>Asesor / Bot</th><th>Motivo</th><th>Sentimiento</th><th>Resultado</th><th></th></tr>
            </thead>
            <tbody>
              <tr *ngIf="!cargando && !visibles.length">
                <td colspan="7" class="vacio-tabla">
                  {{ casosAds.length === 0 ? 'No se encontraron casos de Meta Ads en el período.' : 'No hay casos que coincidan con los filtros seleccionados.' }}
                </td>
              </tr>
              <tr *ngFor="let c of casosPagina">
                <td class="nowrap">{{ (c.archivadaEn || c.procesadoEn || c.createdAt || '').slice(0, 10) || '—' }}</td>
                <td class="contacto">{{ c.idContacto || '—' }}</td>
                <td>
                  <span class="asesor" *ngIf="!nombreAsesor(c).nombre">—</span>
                  <span class="badge violeta" *ngIf="nombreAsesor(c).nombre && nombreAsesor(c).esBot">🤖 Bot</span>
                  <span class="asesor" [class.estimado]="nombreAsesor(c).esEstimado" *ngIf="nombreAsesor(c).nombre && !nombreAsesor(c).esBot">
                    {{ nombreAsesor(c).nombre }}
                    <span class="tag-estimado" *ngIf="nombreAsesor(c).esEstimado">(estimado)</span>
                  </span>
                </td>
                <td class="truncar" [title]="c.motivoContacto || ''">{{ c.motivoContacto ? etiqueta(c.motivoContacto) : '—' }}</td>
                <td>
                  <span class="badge" [class]="'sent-' + c.sentimientoFinal" *ngIf="c.sentimientoFinal">{{ c.sentimientoFinal }}</span>
                  <span *ngIf="!c.sentimientoFinal">—</span>
                </td>
                <td>
                  <span class="badge indigo" *ngIf="c.resultado">{{ c.resultado.replace('_',' ') }}</span>
                  <span *ngIf="!c.resultado">—</span>
                </td>
                <td class="acciones"><button type="button" (click)="detalle = c">Ver caso</button></td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="paginacion" *ngIf="totalPaginas > 1">
          <span>Página <strong>{{ pagina }}</strong> de <strong>{{ totalPaginas }}</strong></span>
          <div class="botones">
            <button type="button" [disabled]="pagina <= 1" (click)="pagina = pagina - 1">Anterior</button>
            <button type="button" [disabled]="pagina >= totalPaginas" (click)="pagina = pagina + 1">Siguiente</button>
          </div>
        </div>
      </div>

      <div class="nota">
        <strong>Nota:</strong> el campo <code>vieneDeAds</code> se rellena automáticamente en los webhooks nuevos.
        Para contactos históricos anteriores al fix, ejecuta el backfill desde la consola del servidor.
      </div>

      <app-liwa-analisis-detalle *ngIf="detalle" [item]="detalle" (close)="detalle = null" />
    </section>
  `,
  styles: [`
    .ads { display: flex; flex-direction: column; gap: 14px; }
    .hero { display: flex; align-items: center; gap: 12px; border-radius: 16px; padding: 16px; color: #fff; background: linear-gradient(90deg,#2563eb,#4f46e5); box-shadow: 0 8px 20px rgba(37,99,235,0.25); }
    .hero .icono { width: 40px; height: 40px; border-radius: 12px; background: rgba(255,255,255,0.2); display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
    .hero h2 { margin: 0; font-size: 1rem; font-weight: 800; }
    .hero p { margin: 2px 0 0; font-size: 0.72rem; color: #dbeafe; }
    .aviso { display: flex; align-items: center; gap: 8px; border-radius: 12px; padding: 10px 14px; font-size: 0.75rem; }
    .aviso.info { background: #eff6ff; border: 1px solid #bfdbfe; color: #1d4ed8; }
    .aviso.error { background: #fff1f2; border: 1px solid #fecdd3; color: #be123c; }
    .spin { animation: spin 1s linear infinite; }
    @keyframes spin { to { transform: rotate(360deg); } }
    .kpis { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 10px; }
    .kpi { border-radius: 16px; padding: 14px; border: 1px solid #e2e8f0; }
    .kpi span { font-size: 0.62rem; font-weight: 700; text-transform: uppercase; }
    .kpi .valor { margin: 4px 0 0; font-size: 1.8rem; font-weight: 800; line-height: 1; }
    .kpi .sub { margin: 4px 0 0; font-size: 0.68rem; }
    .kpi.blue { background: #eff6ff; border-color: #bfdbfe; } .kpi.blue span, .kpi.blue .sub { color: #3b82f6; } .kpi.blue .valor { color: #1d4ed8; }
    .kpi.white { background: #fff; } .kpi.white span, .kpi.white .sub { color: #64748b; } .kpi.white .valor { color: #1e293b; }
    .kpi.indigo { background: #eef2ff; border-color: #c7d2fe; } .kpi.indigo span, .kpi.indigo .sub { color: #6366f1; } .kpi.indigo .valor { color: #4338ca; }
    .graficas { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 12px; }
    .tarjeta { background: #fff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 14px; }
    .tarjeta h3 { margin: 0 0 2px; font-size: 0.85rem; font-weight: 700; color: #1e293b; }
    .hint { margin: 0 0 10px; font-size: 0.62rem; color: #94a3b8; }
    .barra { display: block; width: 100%; text-align: left; border: none; background: transparent; border-radius: 8px; padding: 4px 6px; margin-bottom: 6px; cursor: pointer; }
    .barra:hover { background: #f8fafc; }
    .barra.activo { background: #eff6ff; }
    .barra.sin-clic { cursor: default; }
    .barra .fila { display: flex; justify-content: space-between; font-size: 0.72rem; margin-bottom: 3px; }
    .barra .fila span { text-transform: capitalize; color: #475569; }
    .barra .track { height: 8px; border-radius: 999px; background: #f1f5f9; overflow: hidden; }
    .barra .track span { display: block; height: 100%; border-radius: 999px; }
    .vacio { text-align: center; color: #94a3b8; font-size: 0.75rem; padding: 16px; }
    .tabla-card { background: #fff; border: 1px solid #e2e8f0; border-radius: 16px; overflow: hidden; }
    .tabla-header { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 10px; padding: 12px 16px; border-bottom: 1px solid #f1f5f9; }
    .titulo-tabla { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
    .titulo-tabla h3 { margin: 0; font-size: 0.85rem; font-weight: 700; color: #1e293b; }
    .contador { background: #dbeafe; color: #1d4ed8; font-size: 0.62rem; font-weight: 700; padding: 2px 8px; border-radius: 999px; }
    .limpiar { border: none; background: transparent; color: #94a3b8; font-size: 0.62rem; text-decoration: underline; cursor: pointer; }
    .tabla-header input { border: 1px solid #e2e8f0; border-radius: 8px; padding: 6px 10px; font-size: 0.72rem; min-width: 200px; }
    .chips { display: flex; gap: 6px; padding: 8px 16px; border-bottom: 1px solid #f1f5f9; background: #f8fafc; }
    .chip { display: inline-flex; align-items: center; gap: 4px; border-radius: 6px; padding: 2px 8px; font-size: 0.62rem; font-weight: 600; }
    .chip button { border: none; background: transparent; cursor: pointer; }
    .chip.blue { background: #dbeafe; color: #1d4ed8; }
    .chip.indigo { background: #e0e7ff; color: #4338ca; }
    .tabla-wrap { overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; font-size: 0.78rem; min-width: 700px; }
    th { text-align: left; padding: 10px 14px; font-size: 0.65rem; color: #94a3b8; font-weight: 600; background: #f8fafc; border-bottom: 1px solid #e2e8f0; }
    td { padding: 8px 14px; border-bottom: 1px solid #f1f5f9; color: #475569; }
    .nowrap { white-space: nowrap; }
    .contacto { font-weight: 600; color: #334155; }
    .truncar { max-width: 150px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .asesor { color: #334155; }
    .asesor.estimado { color: #94a3b8; }
    .tag-estimado { color: #f59e0b; font-size: 0.6rem; margin-left: 4px; }
    .badge { display: inline-flex; padding: 2px 8px; border-radius: 999px; font-size: 0.62rem; font-weight: 600; text-transform: capitalize; background: #f1f5f9; color: #64748b; }
    .badge.violeta { background: #f5f3ff; color: #7c3aed; }
    .badge.sent-positivo { background: #ecfdf5; color: #047857; }
    .badge.sent-negativo { background: #fff1f2; color: #be123c; }
    .badge.sent-neutral { background: #f1f5f9; color: #475569; }
    .badge.indigo { background: #e0e7ff; color: #4338ca; }
    .vacio-tabla { text-align: center; color: #94a3b8; padding: 40px; }
    .acciones { text-align: right; }
    .acciones button { border: none; background: #eff6ff; color: #1d4ed8; font-size: 0.7rem; font-weight: 600; padding: 6px 12px; border-radius: 8px; cursor: pointer; }
    .acciones button:hover { background: #dbeafe; }
    .paginacion { display: flex; justify-content: space-between; align-items: center; padding: 10px 16px; border-top: 1px solid #f1f5f9; background: #f8fafc; font-size: 0.7rem; color: #64748b; }
    .botones { display: flex; gap: 6px; }
    .botones button { border: 1px solid #e2e8f0; background: #fff; padding: 6px 12px; border-radius: 8px; font-size: 0.7rem; font-weight: 600; color: #475569; cursor: pointer; }
    .botones button:disabled { opacity: 0.4; cursor: not-allowed; }
    .nota { background: #fffbeb; border: 1px solid #fde68a; color: #92400e; font-size: 0.68rem; border-radius: 12px; padding: 10px 14px; line-height: 1.4; }
    .nota code { background: #fef3c7; padding: 1px 5px; border-radius: 4px; font-family: monospace; }
  `],
})
export class LiwaAdsPanelComponent implements OnChanges {
  @Input() desde?: string;
  @Input() hasta?: string;
  // Ticker que el panel padre incrementa cada 20s (ver
  // omnicanal-liwa-panel.component.ts) para refrescar sin que el usuario
  // tenga que cambiar de pestaña. No resetea la pagina como si haria un
  // cambio real de filtro -- solo trae datos nuevos en el fondo.
  @Input() autoRefreshTick?: number;

  reporte: LiwaReporteAds | null = null;
  casos: LiwaAnalisisItem[] = [];
  agentes: Record<string, string> = {};
  cargando = true;
  error: string | null = null;
  detalle: LiwaAnalisisItem | null = null;
  pagina = 1;
  busqueda = '';
  filtroMotivo: string | null = null;
  filtroResultado: string | null = null;
  private peticion = 0;
  readonly POR_PAGINA = 25;

  constructor(private readonly liwa: LiwaService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (!('autoRefreshTick' in changes) || Object.keys(changes).length > 1) {
      this.pagina = 1;
    }
    void this.cargar();
  }

  private async cargar(): Promise<void> {
    const id = ++this.peticion;
    this.cargando = true;
    this.error = null;
    try {
      const [rep, analisis, conversaciones] = await Promise.all([
        this.liwa.obtenerReporteAds({ desde: this.desde, hasta: this.hasta }).catch(() => null),
        this.liwa.obtenerTodosLosAnalisis({ desde: this.desde, hasta: this.hasta }),
        this.liwa.obtenerConversacionesChat({ desde: this.desde, hasta: this.hasta }).catch(() => [] as LiwaChat[]),
      ]);
      if (id !== this.peticion) return;
      const agentesPorContacto: Record<string, string> = {};
      for (const chat of conversaciones) {
        if (chat.idContacto && chat.agente) agentesPorContacto[chat.idContacto] = chat.agente;
      }
      this.reporte = rep as LiwaReporteAds | null;
      this.casos = analisis;
      this.agentes = agentesPorContacto;
    } catch {
      if (id === this.peticion) this.error = 'No fue posible cargar el reporte de Ads.';
    } finally {
      if (id === this.peticion) this.cargando = false;
    }
  }

  get textoRango(): string {
    if (!this.desde && !this.hasta) return 'todo el historial';
    if (this.desde && this.hasta) return `${this.desde} → ${this.hasta}`;
    if (this.desde) return `desde ${this.desde}`;
    return `hasta ${this.hasta}`;
  }

  etiqueta(motivo: string | null | undefined): string {
    return this.liwa.etiquetaMotivo(motivo);
  }

  private esNombreBot(n: string): boolean {
    return NOMBRES_BOT_SET.has(n.trim().toLowerCase());
  }

  nombreAsesor(caso: LiwaAnalisisItem): { nombre: string; esEstimado: boolean; esBot: boolean } {
    const real = caso.asesor || '';
    if (real) return { nombre: real, esEstimado: false, esBot: this.esNombreBot(real) };
    const estimado = caso.idContacto ? this.agentes[caso.idContacto] : '';
    if (estimado) return { nombre: estimado, esEstimado: !this.esNombreBot(estimado), esBot: this.esNombreBot(estimado) };
    return { nombre: '', esEstimado: false, esBot: false };
  }

  get casosAds(): LiwaAnalisisItem[] {
    return this.casos.filter((c) => this.liwa.analisisEnRango(c, this.desde, this.hasta) && c.vieneDeAds === true);
  }

  get totalCasosRango(): number {
    return this.casos.filter((c) => this.liwa.analisisEnRango(c, this.desde, this.hasta)).length;
  }

  get pctAds(): string {
    return this.totalCasosRango > 0 ? ((this.casosAds.length / this.totalCasosRango) * 100).toFixed(1) : '0.0';
  }

  pct(valor: number, total: number): number {
    return total > 0 ? Math.max(2, (valor / total) * 100) : 0;
  }

  get porMotivo(): Barra[] {
    const m = new Map<string, number>();
    this.casosAds.forEach((c) => { const k = c.motivoContacto || ''; m.set(k, (m.get(k) || 0) + 1); });
    return Array.from(m, ([motivo, total]) => ({
      clave: motivo, label: motivo ? this.etiqueta(motivo) : 'Sin analizar aún', total, color: COLOR_MOTIVO[motivo] || '#6b7280',
    })).sort((a, b) => b.total - a.total);
  }

  get porResultado(): Barra[] {
    const m = new Map<string, number>();
    this.casosAds.forEach((c) => { const k = c.resultado || ''; m.set(k, (m.get(k) || 0) + 1); });
    return Array.from(m, ([resultado, total]) => ({
      clave: resultado, label: resultado ? resultado.replace('_', ' ') : 'Sin analizar aún', total, color: COLOR_RESULTADO[resultado] || '#6b7280',
    })).sort((a, b) => b.total - a.total);
  }

  get porSentimiento(): Barra[] {
    const m = new Map<string, number>();
    this.casosAds.forEach((c) => { const k = c.sentimientoFinal || ''; m.set(k, (m.get(k) || 0) + 1); });
    return Array.from(m, ([sentimiento, total]) => ({
      clave: sentimiento, label: sentimiento || 'Sin analizar aún', total, color: COLOR_SENTIMIENTO[sentimiento] || '#6b7280',
    })).sort((a, b) => b.total - a.total);
  }

  toggleMotivo(clave: string): void {
    this.filtroMotivo = this.filtroMotivo === clave ? null : clave;
  }

  toggleResultado(clave: string): void {
    this.filtroResultado = this.filtroResultado === clave ? null : clave;
  }

  get visibles(): LiwaAnalisisItem[] {
    return this.casosAds.filter((c) => {
      if (this.filtroMotivo !== null && (c.motivoContacto || '') !== this.filtroMotivo) return false;
      if (this.filtroResultado !== null && (c.resultado || '') !== this.filtroResultado) return false;
      const asesorNombre = this.nombreAsesor(c).nombre;
      const texto = [c.idContacto, c.motivoContacto, c.resultado, c.sentimientoFinal, asesorNombre].filter(Boolean).join(' ').toLowerCase();
      return !this.busqueda.trim() || texto.includes(this.busqueda.trim().toLowerCase());
    });
  }

  get totalPaginas(): number {
    return Math.max(1, Math.ceil(this.visibles.length / this.POR_PAGINA));
  }

  get casosPagina(): LiwaAnalisisItem[] {
    return this.visibles.slice((this.pagina - 1) * this.POR_PAGINA, this.pagina * this.POR_PAGINA);
  }

  get totalAdsReporte(): number {
    return this.reporte?.totalCasosDeAds ?? this.casosAds.length;
  }

  get hayFiltros(): boolean {
    return this.filtroMotivo !== null || this.filtroResultado !== null || !!this.busqueda;
  }

  limpiarFiltros(): void {
    this.filtroMotivo = null;
    this.filtroResultado = null;
    this.busqueda = '';
    this.pagina = 1;
  }
}
