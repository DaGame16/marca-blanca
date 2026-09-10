import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { LiwaService, formatearNumero, rangoConHoras } from '../data/liwa.service';
import { LiwaAnalisisItem, LiwaChat, LiwaEstadisticas } from '../models/liwa.model';
import {
  KpiCalidad, LiwaAnilloSentimientoComponent, LiwaBarrasAreaComponent, LiwaBarrasCategoriasComponent,
  LiwaBurbujasDispersionComponent, LiwaKpiGridCalidadComponent, LiwaLineaTendenciaComponent, LiwaMapaCalorComponent,
} from './calidad-charts.components';
import { LiwaTablaAnalisisIaComponent } from './analisis-ia-tabla.component';
import { LiwaConversationDrawerComponent } from './conversation-drawer.component';

type DetalleKpi = 'conversaciones' | 'analizadas' | null;

// Municipios que pidió el cliente — solo estos salen con nombre propio en
// las gráficas por municipio; todo lo demás se agrupa bajo "Otros".
const MUNICIPIOS_PEDIDOS = ['albania', 'hatonuevo', 'fonseca', 'san juan', 'villa martin', 'mushaisa', 'molino'];
const normalizarZona = (z: string) => z.toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '').trim();
const municipioPedidoDe = (z: string): string | null => {
  const n = normalizarZona(z);
  return MUNICIPIOS_PEDIDOS.find((p) => n === p || n.includes(p) || p.includes(n)) || null;
};

// Traducción de components/liwa/DashboardCalidad.tsx ("vista Gráficas").
// Toda la lógica de cálculo (motivos, sentimiento, mapa de calor, municipio,
// tendencia por día) es 1:1 con el original; las gráficas en sí se
// simplificaron a widgets propios (ver calidad-charts.components.ts).
@Component({
  selector: 'app-liwa-dashboard-calidad',
  standalone: true,
  imports: [
    CommonModule, MatIconModule, LiwaKpiGridCalidadComponent, LiwaBarrasCategoriasComponent,
    LiwaAnilloSentimientoComponent, LiwaMapaCalorComponent, LiwaBurbujasDispersionComponent,
    LiwaLineaTendenciaComponent, LiwaBarrasAreaComponent, LiwaTablaAnalisisIaComponent, LiwaConversationDrawerComponent,
  ],
  template: `
    <div class="dashboard">
      <h2 class="titulo">Dashboard de calidad del servicio</h2>

      <app-liwa-kpi-grid-calidad [kpis]="kpis" />

      <div class="modal-overlay" *ngIf="detalleKpi" (click)="detalleKpi = null">
        <div class="modal" (click)="$event.stopPropagation()">
          <header>
            <h3>{{ detalleKpi === 'conversaciones' ? ('Conversaciones archivadas (' + chats.length + ')') : ('Conversaciones analizadas por IA (' + analisisVisibles.length + ')') }}</h3>
            <button type="button" (click)="detalleKpi = null"><mat-icon>close</mat-icon></button>
          </header>
          <div class="modal-body">
            <div class="lista" *ngIf="detalleKpi === 'conversaciones'">
              <button type="button" class="chat-item" *ngFor="let chat of chats" (click)="detalleKpi = null; chatSeleccionado = chat">
                <div>
                  <p class="nombre">{{ chat.nombre || formatear(chat.numero) }}</p>
                  <p class="sub">{{ chat.nombre ? formatear(chat.numero) : 'Cliente de WhatsApp' }} · {{ chat.cantidadMensajes }} mensajes</p>
                </div>
                <mat-icon>chevron_right</mat-icon>
              </button>
              <p class="vacio" *ngIf="!chats.length">No hay conversaciones en el periodo.</p>
            </div>
            <app-liwa-tabla-analisis-ia *ngIf="detalleKpi === 'analizadas'" [items]="analisisVisibles" [cargando]="cargandoAnalisis" [mostrarResumen]="false" />
          </div>
        </div>
      </div>

      <div class="grid-2">
        <div class="tarjeta">
          <div class="tarjeta-header"><h3>Motivos de contacto</h3><span>barras · análisis IA</span></div>
          <div class="cargando" *ngIf="cargandoAnalisis"><mat-icon class="spin">progress_activity</mat-icon> Cargando análisis…</div>
          <app-liwa-barras-categorias *ngIf="!cargandoAnalisis" [data]="motivosData" />
        </div>
        <div class="tarjeta">
          <div class="tarjeta-header"><h3>Sentimiento del cliente</h3><span>anillo · sentimiento final · IA</span></div>
          <div class="cargando" *ngIf="cargandoAnalisis"><mat-icon class="spin">progress_activity</mat-icon> Cargando análisis…</div>
          <app-liwa-anillo-sentimiento *ngIf="!cargandoAnalisis" [data]="sentimientoData" [total]="analisisVisibles.length" />
        </div>
      </div>

      <div class="grid-2">
        <div class="tarjeta">
          <div class="tarjeta-header"><h3>Frecuencia por motivo y municipio</h3><span>mapa de calor · IA{{ mapaCalor.sinMunicipio > 0 ? (' · ' + mapaCalor.sinMunicipio + ' sin municipio identificado (no incluidos)') : '' }}</span></div>
          <app-liwa-mapa-calor [filas]="mapaCalor.filas" [columnas]="mapaCalor.columnas" [valores]="mapaCalor.valores" [max]="mapaCalor.max" />
        </div>
        <div class="tarjeta">
          <div class="tarjeta-header"><h3>Satisfacción vs. efectividad</h3><span>CSAT vs FCR por motivo · IA</span></div>
          <div class="cargando" *ngIf="cargandoAnalisis"><mat-icon class="spin">progress_activity</mat-icon> Cargando análisis…</div>
          <app-liwa-burbujas-dispersion *ngIf="!cargandoAnalisis" [data]="burbujasData" />
        </div>
      </div>

      <div class="tarjeta">
        <div class="tarjeta-header"><h3>Tendencia de actividad por municipio</h3><span>por día · fecha real de cada mensaje</span></div>
        <app-liwa-linea-tendencia [data]="tendenciaData.filas" [series]="tendenciaData.series" />
      </div>

      <div class="grid-2">
        <div class="tarjeta">
          <div class="tarjeta-header"><h3>Volumen por municipio</h3><span>barras · IA</span></div>
          <app-liwa-barras-area [data]="municipioData" />
        </div>
        <div class="tarjeta">
          <div class="tarjeta-header"><h3>Respuestas por asesor</h3><span>conversaciones atendidas por un asesor humano</span></div>
          <app-liwa-barras-categorias [data]="agentesData" />
        </div>
      </div>

      <app-liwa-conversation-drawer [chat]="chatSeleccionado" (onClose)="chatSeleccionado = null" />
    </div>
  `,
  styles: [`
    .dashboard { display: flex; flex-direction: column; gap: 14px; }
    .titulo { margin: 0; font-size: 1.05rem; font-weight: 800; color: #0f172a; }
    .grid-2 { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 12px; }
    .tarjeta { background: #fff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 14px; }
    .tarjeta-header { display: flex; flex-wrap: wrap; justify-content: space-between; align-items: baseline; gap: 6px; margin-bottom: 10px; }
    .tarjeta-header h3 { margin: 0; font-size: 0.82rem; font-weight: 700; color: #1e293b; }
    .tarjeta-header span { font-size: 0.62rem; color: #94a3b8; }
    .cargando { display: flex; align-items: center; justify-content: center; gap: 8px; padding: 24px; color: #94a3b8; font-size: 0.75rem; }
    .spin { animation: spin 1s linear infinite; }
    @keyframes spin { to { transform: rotate(360deg); } }
    .modal-overlay { position: fixed; inset: 0; z-index: 90; background: rgba(2,6,23,0.45); display: flex; align-items: center; justify-content: center; padding: 16px; }
    .modal { width: 100%; max-width: 760px; max-height: 88vh; background: #fff; border-radius: 16px; overflow: hidden; display: flex; flex-direction: column; }
    .modal header { display: flex; justify-content: space-between; align-items: center; padding: 14px 18px; border-bottom: 1px solid #f1f5f9; }
    .modal header h3 { margin: 0; font-size: 0.85rem; font-weight: 700; color: #1e293b; }
    .modal header button { border: none; background: transparent; color: #94a3b8; cursor: pointer; padding: 6px; border-radius: 8px; }
    .modal-body { overflow-y: auto; padding: 14px 18px; }
    .lista { display: flex; flex-direction: column; gap: 8px; }
    .chat-item { display: flex; align-items: center; justify-content: space-between; gap: 8px; border: 1px solid #e2e8f0; border-radius: 12px; padding: 10px 12px; background: #fff; cursor: pointer; text-align: left; }
    .chat-item:hover { background: #ecfdf5; border-color: #a7f3d0; }
    .chat-item .nombre { margin: 0; font-size: 0.8rem; font-weight: 700; color: #1e293b; }
    .chat-item .sub { margin: 2px 0 0; font-size: 0.68rem; color: #94a3b8; }
    .vacio { text-align: center; color: #94a3b8; padding: 24px; }
  `],
})
export class LiwaDashboardCalidadComponent implements OnChanges {
  @Input() estadisticas: LiwaEstadisticas | null = null;
  @Input() chats: LiwaChat[] = [];
  @Input() desde?: string;
  @Input() hasta?: string;

  analisis: LiwaAnalisisItem[] = [];
  cargandoAnalisis = false;
  detalleKpi: DetalleKpi = null;
  chatSeleccionado: LiwaChat | null = null;
  private peticion = 0;

  constructor(private readonly liwa: LiwaService) {}

  ngOnChanges(): void {
    void this.cargar();
  }

  formatear(numero: string): string {
    return formatearNumero(numero);
  }

  private async cargar(): Promise<void> {
    const id = ++this.peticion;
    this.cargandoAnalisis = true;
    try {
      const items = await this.liwa.obtenerTodosLosAnalisis({ desde: this.desde, hasta: this.hasta });
      if (id !== this.peticion) return;
      this.analisis = items || [];
    } catch {
      if (id === this.peticion) this.analisis = [];
    } finally {
      if (id === this.peticion) this.cargandoAnalisis = false;
    }
  }

  get analisisVisibles(): LiwaAnalisisItem[] {
    return this.analisis.filter((a) => this.liwa.analisisEnRango(a, this.desde, this.hasta));
  }

  get motivosData(): { etiqueta: string; total: number }[] {
    const m = new Map<string, number>();
    this.analisisVisibles.forEach((a) => {
      const etiqueta = this.liwa.etiquetaMotivo(a.motivoContacto);
      m.set(etiqueta, (m.get(etiqueta) || 0) + 1);
    });
    return Array.from(m, ([etiqueta, total]) => ({ etiqueta, total })).sort((a, b) => b.total - a.total);
  }

  get sentimientoData(): { etiqueta: string; valor: number; color: string }[] {
    const m = new Map<string, number>();
    this.analisisVisibles.forEach((a) => {
      const s = a.sentimientoFinal || 'neutral';
      const etiqueta = s === 'positivo' ? 'Positivo' : s === 'negativo' ? 'Negativo' : 'Neutral';
      m.set(etiqueta, (m.get(etiqueta) || 0) + 1);
    });
    const colores: Record<string, string> = { Positivo: '#10b981', Neutral: '#f59e0b', Negativo: '#ef4444' };
    return Array.from(m, ([etiqueta, valor]) => ({ etiqueta, valor, color: colores[etiqueta] || '#94a3b8' }));
  }

  get burbujasData(): { motivo: string; csat: number; fcr: number; volumen: number }[] {
    const porMotivo = new Map<string, { total: number; positivos: number; fcr: number }>();
    this.analisisVisibles.forEach((a) => {
      const m = this.liwa.etiquetaMotivo(a.motivoContacto);
      const g = porMotivo.get(m) || { total: 0, positivos: 0, fcr: 0 };
      g.total += 1;
      if (a.sentimientoFinal === 'positivo') g.positivos += 1;
      if (a.fcr) g.fcr += 1;
      porMotivo.set(m, g);
    });
    return Array.from(porMotivo, ([motivo, g]) => ({
      motivo,
      csat: Math.round((g.positivos / g.total) * 100),
      fcr: Math.round((g.fcr / g.total) * 100),
      volumen: g.total,
    }));
  }

  get mapaCalor(): { filas: string[]; columnas: string[]; valores: Record<string, number>; max: number; sinMunicipio: number } {
    const valores: Record<string, number> = {};
    const filasSet = new Set<string>();
    const etiquetasPedidas = new Map<string, string>();
    let sinMunicipio = 0;
    let max = 0;
    this.analisisVisibles.forEach((a) => {
      const zona = (a.municipio || '').trim();
      if (!zona) { sinMunicipio++; return; }
      const m = this.liwa.etiquetaMotivo(a.motivoContacto);
      filasSet.add(m);
      const pedido = municipioPedidoDe(zona);
      if (pedido && !etiquetasPedidas.has(pedido)) etiquetasPedidas.set(pedido, zona);
      const clave = `${m}|${pedido ? zona : 'Otros'}`;
      valores[clave] = (valores[clave] || 0) + 1;
      if (valores[clave] > max) max = valores[clave];
    });
    const columnas = MUNICIPIOS_PEDIDOS.map((p) => etiquetasPedidas.get(p)).filter((l): l is string => !!l);
    const hayOtros = Object.keys(valores).some((clave) => clave.endsWith('|Otros'));
    if (hayOtros) columnas.push('Otros');
    return { filas: Array.from(filasSet).sort(), columnas, valores, max, sinMunicipio };
  }

  get municipioData(): { area: string; total: number }[] {
    const m = new Map<string, number>();
    const etiquetasPedidas = new Map<string, string>();
    this.analisisVisibles.forEach((a) => {
      const zona = (a.municipio || '').trim();
      if (!zona) return;
      const pedido = municipioPedidoDe(zona);
      if (pedido) {
        if (!etiquetasPedidas.has(pedido)) etiquetasPedidas.set(pedido, zona);
        m.set(`pedido|${pedido}`, (m.get(`pedido|${pedido}`) || 0) + 1);
      } else {
        m.set('Otros', (m.get('Otros') || 0) + 1);
      }
    });
    const datos = MUNICIPIOS_PEDIDOS
      .map((p) => (etiquetasPedidas.has(p) ? { area: etiquetasPedidas.get(p) as string, total: m.get(`pedido|${p}`) || 0 } : null))
      .filter((d): d is { area: string; total: number } => !!d);
    if ((m.get('Otros') || 0) > 0) datos.push({ area: 'Otros', total: m.get('Otros') as number });
    return datos;
  }

  get agentesData(): { etiqueta: string; total: number }[] {
    const m = new Map<string, number>();
    this.chats.forEach((c) => {
      if (!c.agente) return;
      m.set(c.agente, (m.get(c.agente) || 0) + 1);
    });
    return Array.from(m, ([etiqueta, total]) => ({ etiqueta, total })).sort((a, b) => b.total - a.total);
  }

  get tendenciaData(): { filas: Record<string, string | number>[]; series: { clave: string; color: string }[] } {
    const { desde: dI, hasta: hI } = rangoConHoras(this.desde, this.hasta);
    const ini = dI ? new Date(dI).getTime() : -Infinity;
    const fin = hI ? new Date(hI).getTime() : Infinity;
    const municipioPorContacto = new Map<string, string>();
    this.analisisVisibles.forEach((a) => {
      const zona = (a.municipio || '').trim();
      if (a.idContacto && zona && !municipioPorContacto.has(a.idContacto)) municipioPorContacto.set(a.idContacto, zona);
    });
    const porDiaTotal = new Map<string, number>();
    const porDiaMunicipio = new Map<string, Map<string, number>>();
    this.chats.forEach((c) => {
      const zona = municipioPorContacto.get(c.idContacto) || 'Sin municipio';
      c.mensajes.forEach((m) => {
        const d = new Date(m.fecha);
        if (isNaN(d.getTime())) return;
        const t = d.getTime();
        if (t < ini || t > fin) return;
        const dia = d.toISOString().slice(0, 10);
        porDiaTotal.set(dia, (porDiaTotal.get(dia) || 0) + 1);
        const pd = porDiaMunicipio.get(dia) || new Map<string, number>();
        pd.set(zona, (pd.get(zona) || 0) + 1);
        porDiaMunicipio.set(dia, pd);
      });
    });
    const totalesZona = new Map<string, number>();
    porDiaMunicipio.forEach((pd) => pd.forEach((n, z) => totalesZona.set(z, (totalesZona.get(z) || 0) + n)));
    const topZonas = Array.from(totalesZona.entries()).sort((a, b) => b[1] - a[1]).slice(0, 5).map(([z]) => z);
    const setDias = new Set<string>();
    porDiaTotal.forEach((_, k) => setDias.add(k));
    porDiaMunicipio.forEach((_, k) => setDias.add(k));
    const filas = Array.from(setDias).sort().map((dia) => {
      const fila: Record<string, number | string> = { periodo: new Date(`${dia}T00:00:00`).toLocaleDateString('es-CO', { day: '2-digit', month: 'short' }), total: porDiaTotal.get(dia) || 0 };
      porDiaMunicipio.get(dia)?.forEach((n, z) => {
        const clave = topZonas.includes(z) ? z : 'Otros';
        fila[clave] = (Number(fila[clave]) || 0) + n;
      });
      return fila;
    });
    const hayOtros = filas.some((f) => (Number(f['Otros']) || 0) > 0);
    const colores = ['#16a34a', '#f59e0b', '#dc2626', '#8b5cf6', '#06b6d4', '#64748b'];
    const claves = [...topZonas, ...(hayOtros ? ['Otros'] : [])];
    const series = claves.map((clave, i) => ({ clave, color: colores[i % colores.length] }));
    return { filas, series };
  }

  get totalEventos(): number {
    return this.chats.length;
  }

  get analizadas(): number {
    return this.analisisVisibles.length;
  }

  get promedioMensajes(): number {
    if (this.chats.length === 0) return 0;
    return Math.round((this.chats.reduce((s, c) => s + c.cantidadMensajes, 0) / this.chats.length) * 10) / 10;
  }

  get kpis(): KpiCalidad[] {
    return [
      { label: 'Conversaciones', valor: `${this.totalEventos}`, sub: 'archivadas en el periodo', icon: 'chat', onClick: () => { this.detalleKpi = 'conversaciones'; } },
      { label: 'Analizadas por IA', valor: `${this.analizadas}`, sub: `de ${this.totalEventos} conversaciones`, icon: 'psychology', onClick: () => { this.detalleKpi = 'analizadas'; } },
      { label: 'Mensajes por chat', valor: `${this.promedioMensajes}`, sub: 'interactividad promedio', icon: 'format_list_bulleted' },
    ];
  }
}
