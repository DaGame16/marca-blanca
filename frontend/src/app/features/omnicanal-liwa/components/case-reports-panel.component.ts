import { ChangeDetectorRef, Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { LiwaService } from '../data/liwa.service';
import { LiwaAnalisisItem, LiwaCasoResumenAnalisis, LiwaChat, LiwaResumenContacto } from '../models/liwa.model';
import { LiwaAnalisisDetalleComponent } from './analisis-ia-detalle.component';

type Filtro = 'todos' | 'sentimiento' | 'soporte' | 'matriz' | 'asesor' | 'mapa' | 'ventas' | 'ads';

const COLORS: Record<string, string> = {
  positivo: '#16a34a', neutro: '#64748b', negativo: '#dc2626',
  resuelto: '#16a34a', no_resuelto: '#dc2626', escalado: '#f59e0b', abandonado: '#64748b',
  soporte: '#2563eb', ventas: '#16a34a', facturacion: '#f59e0b', reconexion: '#f97316',
  pqr: '#dc2626', cobertura: '#06b6d4', informacion: '#64748b',
  confirmadas: '#16a34a', no_confirmadas: '#64748b',
};

// Paleta propia de la dona: un color distinto por segmento, independiente
// de COLORS (que usan las barras/tarjetas). Se recicla si hay más motivos
// que colores.
const DONUT_COLORS = ['#1d4ed8', '#f59e0b', '#10b981', '#ef4444', '#a855f7', '#ec4899', '#6b7280', '#06b6d4'];
const COLUMNAS_RESULTADO = ['resuelto', 'no_resuelto', 'escalado', 'abandonado'] as const;
const NOMBRES_BOT_SET = new Set(['yo', 'bot']);

// String(valor) puede dar "[object Object]" si el backend manda algo que no
// es primitivo -- mejor un texto de respaldo que ensuciar la UI con eso.
function aTexto(v: unknown): string {
  if (typeof v === 'string' || typeof v === 'number' || typeof v === 'boolean') return String(v);
  return 'sin_clasificar';
}

interface Barra { label: string; total: number; }
interface GrupoMotivo { codigo: string; etiqueta: string; resultados: Record<string, number>; total: number; }
interface DonutSeg { codigo: string; etiqueta: string; valor: number; color: string; pct: number; }
interface CoberturaFila { municipio: string; casos: number; personas: number; }
interface MapaCalor { filas: { codigo: string; etiqueta: string }[]; columnas: string[]; valores: Record<string, number>; max: number; }

function countValue(data: Record<string, unknown> | null | undefined, keys: string[], fallback = 0): number {
  if (!data) return fallback;
  for (const key of keys) if (typeof data[key] === 'number') return data[key] as number;
  return fallback;
}

// Traducción de components/liwa/CaseReportsPanel.tsx. Las dos donas de
// recharts se reemplazan por una dona SVG hecha a mano (stroke-dasharray por
// segmento) — este proyecto no tiene librería de gráficas instalada, igual
// que en ads-panel/asesores-panel. El export CSV se genera 100% en el
// cliente con Blob + <a download>, sin backend nuevo.
@Component({
  selector: 'app-liwa-case-reports-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, LiwaAnalisisDetalleComponent],
  template: `
    <section class="panel">
      <div class="cabecera">
        <div>
          <h2>Reportes de casos LIWA</h2>
          <p>Cada métrica permite llegar hasta el caso y sus turnos exactos.</p>
        </div>
        <div class="acciones-top">
          <input [(ngModel)]="busqueda" (ngModelChange)="pagina = 1" placeholder="Buscar contacto, asesor o resultado" />
          <button type="button" (click)="limpiar()">Limpiar</button>
          <button type="button" (click)="exportarCsv()">Exportar CSV</button>
        </div>
      </div>

      <div class="aviso info" *ngIf="cargando"><mat-icon class="spin">progress_activity</mat-icon> Cargando reportes reales de LIWA...</div>
      <div class="aviso error" *ngIf="error">{{ error }}</div>

      <div class="grid">
        <div class="tarjeta">
          <div class="fila-titulo">
            <h3>Sentimiento {{ sentimientoVista }}</h3>
            <div class="toggle">
              <button type="button" [class.activo]="sentimientoVista === 'inicial'" (click)="sentimientoVista = 'inicial'">Inicial</button>
              <button type="button" [class.activo]="sentimientoVista === 'final'" (click)="sentimientoVista = 'final'">Final</button>
            </div>
          </div>
          <div class="barras">
            <ng-container *ngIf="sentimientos.length; else sinDatos">
              <button type="button" class="barra" *ngFor="let b of sentimientos" (click)="irA('sentimiento', b.label)">
                <div class="fila"><span>{{ b.label.replace('_',' ') }}</span><strong>{{ b.total }}</strong></div>
                <div class="track"><span [style.width.%]="anchoBarra(b.total)" [style.background]="colorDe(b.label)"></span></div>
              </button>
            </ng-container>
          </div>
        </div>

        <div class="tarjeta">
          <h3>Casos por motivo</h3>
          <div class="chips">
            <button type="button" class="chip" *ngFor="let g of matrizMotivos" [class.activo]="g.etiqueta === motivoActivo"
              (click)="verMotivo(g)">
              {{ g.etiqueta }} <span>({{ g.total }})</span>
            </button>
          </div>
          <div class="sub-barras" *ngIf="motivoData; else sinDatosMotivo">
            <button type="button" class="sub-barra" *ngFor="let r of columnasResultado" [disabled]="!(motivoData!.resultados[r] || 0)"
              (click)="irA('matriz', motivoData!.codigo + '|' + r)">
              <div class="fila mini"><span>{{ r.replace('_',' ') }}</span><span>{{ motivoData!.resultados[r] || 0 }}</span></div>
              <div class="track fino"><span [style.width.%]="pctResultado(r)" [style.background]="colorDe(r)"></span></div>
            </button>
          </div>
          <ng-template #sinDatosMotivo><p class="vacio">Sin casos en el periodo.</p></ng-template>
        </div>

        <div class="tarjeta orden1">
          <h3>Oportunidades de venta</h3>
          <div class="barras">
            <button type="button" class="barra" *ngFor="let b of ventasBarras" (click)="irA('ventas', b.label === 'confirmadas' ? 'confirmadas' : b.label === 'no_confirmadas' ? 'no_confirmadas' : '')">
              <div class="fila"><span>{{ b.label.replace('_',' ') }}</span><strong>{{ b.total }}</strong></div>
              <div class="track"><span [style.width.%]="anchoBarra(b.total)" [style.background]="colorDe(b.label)"></span></div>
            </button>
          </div>
          <p class="valor">{{ totalVentas }}</p>
          <p class="sub">{{ ventasConfirmadas }} confirmadas en texto · {{ tasa.toFixed(1) }}% conversión textual</p>
          <p class="nota-mini">Incluye casos de cualquier motivo (no solo "Ventas") donde la IA detectó interés de compra — por eso puede no coincidir con el total del botón "Ventas" en Casos por motivo.</p>
          <div class="enlaces">
            <button type="button" (click)="irA('ventas', '')">Ver todas</button>
            <button type="button" class="ok" (click)="irA('ventas', 'confirmadas')">Ver confirmadas</button>
            <button type="button" class="amber" (click)="irA('ventas', 'no_confirmadas')">Ver no confirmadas</button>
          </div>
        </div>

        <div class="tarjeta orden3">
          <h3>Casos por motivo · dona</h3>
          <p class="hint">Cada segmento con su % sobre el total · clic en un segmento o en la leyenda filtra la tabla de casos</p>
          <ng-container *ngIf="motivosDonut.length; else sinDatos">
            <div class="dona-wrap">
              <svg viewBox="0 0 120 120" class="dona">
                <circle *ngFor="let seg of segmentosDona" [attr.cx]="60" [attr.cy]="60" r="45" fill="none"
                  [attr.stroke]="seg.color" stroke-width="18"
                  [attr.stroke-dasharray]="seg.dash" [attr.stroke-dashoffset]="seg.offset"
                  transform="rotate(-90 60 60)" style="cursor:pointer" (click)="irA('matriz', seg.codigo + '|')">
                  <title>{{ seg.etiqueta }}: {{ seg.valor }} ({{ seg.pct }}%)</title>
                </circle>
              </svg>
              <div class="dona-centro">
                <p class="total">{{ totalCasosMotivos }}</p>
                <p class="label">casos · 100%</p>
              </div>
            </div>
            <div class="leyenda">
              <button type="button" class="item-leyenda" *ngFor="let d of motivosDonut" (click)="irA('matriz', d.codigo + '|')">
                <span class="punto" [style.background]="d.color"></span>
                <span class="nombre">{{ d.etiqueta }}</span>
                <span class="dato">{{ d.valor }} · {{ d.pct }}%</span>
              </button>
            </div>
          </ng-container>
        </div>

        <div class="tarjeta orden4">
          <h3>Cobertura preguntada en cada municipio</h3>
          <p class="hint">Municipios que el bot interpretó · personas que preguntaron por cobertura</p>
          <div class="barras" *ngIf="coberturaPorMunicipio.length; else sinDatosCobertura">
            <button type="button" class="barra" *ngFor="let g of coberturaPorMunicipio" (click)="irA('mapa', 'cobertura|' + g.municipio)">
              <div class="fila">
                <span>{{ g.municipio }}</span>
                <span class="nowrap"><strong>{{ g.personas }}</strong> persona{{ g.personas === 1 ? '' : 's' }} · {{ g.casos }} caso{{ g.casos === 1 ? '' : 's' }}</span>
              </div>
              <div class="track"><span [style.width.%]="pctCobertura(g)" [style.background]="'#06b6d4'"></span></div>
            </button>
          </div>
          <ng-template #sinDatosCobertura><p class="vacio">Sin casos de cobertura con municipio identificado en el periodo.</p></ng-template>
        </div>

        <div class="tarjeta orden2">
          <h3>Mapa de calor · Motivo × Municipio</h3>
          <ng-container *ngIf="matrizMapa.filas.length && matrizMapa.columnas.length; else sinDatos">
            <div class="tabla-wrap">
              <table class="mapa">
                <thead>
                  <tr>
                    <th>Motivo</th>
                    <th *ngFor="let c of matrizMapa.columnas">{{ c }}</th>
                  </tr>
                </thead>
                <tbody>
                  <tr *ngFor="let fila of matrizMapa.filas">
                    <td><button type="button" class="motivo-link" (click)="irA('matriz', fila.codigo + '|')">{{ fila.etiqueta }}</button></td>
                    <td *ngFor="let m of matrizMapa.columnas" [style.background]="fondoCelda(fila.codigo, m)">
                      <button type="button" [disabled]="!valorCelda(fila.codigo, m)" (click)="irA('mapa', fila.codigo + '|' + m)">
                        {{ valorCelda(fila.codigo, m) || '' }}
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <p class="hint">Clic en una celda filtra los casos por motivo y municipio · clic en el motivo para ver todos sus casos. Solo se pintan casos con municipio identificado por la IA.</p>
          </ng-container>
        </div>
      </div>
      <ng-template #sinDatos><p class="vacio">Sin casos en el periodo.</p></ng-template>

      <div class="seccion-casos" id="seccion-casos">
        <div class="titulo-casos">
          <h3>Casos encontrados <span class="contador">{{ visibles.length }}</span></h3>
          <span class="chip-filtro" *ngIf="filtro !== 'todos' || segmento">Filtro: <strong>{{ filtro }}{{ segmento ? ' · ' + segmento : '' }}</strong></span>
        </div>

        <div class="tabla-card">
          <div class="tabla-wrap">
            <table>
              <thead>
                <tr>
                  <th>Archivada</th><th>Conversación</th><th>Contacto</th><th>Asesor</th><th>Motivo</th>
                  <th>Sentimiento</th><th>Resultado</th><th>Abandono</th><th></th>
                </tr>
              </thead>
              <tbody>
                <tr *ngIf="!cargando && !visibles.length">
                  <td colspan="9" class="vacio-tabla">No se encontraron casos para los filtros seleccionados.</td>
                </tr>
                <tr *ngFor="let c of casosPagina">
                  <td class="nowrap">{{ (c.archivadaEn || c.procesadoEn || c.createdAt || '').slice(0, 10) || '—' }}</td>
                  <td class="nowrap">{{ (c.tsPrimerMensaje || c.tsPrimeraRespuesta) ? (c.tsPrimerMensaje || c.tsPrimeraRespuesta)!.slice(0, 10) : '—' }}</td>
                  <td>
                    <span class="contacto" *ngIf="c.idContacto" (click)="abrirResumenContacto(c.idContacto!)">{{ c.idContacto }}</span>
                    <span *ngIf="!c.idContacto">—</span>
                    <button type="button" class="badge meta" *ngIf="c.vieneDeAds === true" title="Este contacto llegó desde una pauta de Meta Ads. Clic para filtrar todos los casos de Ads." (click)="irA('ads', '')">META</button>
                  </td>
                  <td>
                    <span *ngIf="!nombreAsesor(c).nombre">—</span>
                    <span class="badge violeta" *ngIf="nombreAsesor(c).nombre && nombreAsesor(c).esBot">🤖 Bot</span>
                    <span class="asesor" [class.estimado]="nombreAsesor(c).esEstimado" *ngIf="nombreAsesor(c).nombre && !nombreAsesor(c).esBot">
                      {{ nombreAsesor(c).nombre }}
                      <span class="tag-estimado" *ngIf="nombreAsesor(c).esEstimado">(estimado)</span>
                    </span>
                  </td>
                  <td class="truncar" [title]="c.motivoContacto || ''">{{ c.motivoContacto || '—' }}</td>
                  <td>
                    <span class="badge" [class]="'sent-' + (c.sentimientoFinal || '')" *ngIf="c.sentimientoFinal">{{ c.sentimientoFinal }}</span>
                    <span *ngIf="!c.sentimientoFinal">—</span>
                  </td>
                  <td>
                    <span class="badge res-ok" *ngIf="c.resultado === 'resuelto'">Resuelto</span>
                    <span class="badge res-no" *ngIf="c.resultado === 'no_resuelto'">No resuelto</span>
                    <span class="badge res-amber" *ngIf="c.resultado === 'escalado'">Escalado</span>
                    <span class="badge res-sin" *ngIf="!c.resultado">Sin clasificar</span>
                  </td>
                  <td>
                    <span class="badge aband-asesor" *ngIf="c.abandono && c.abandonadoPor === 'asesor'" title="El lado de la empresa (asesor o bot) dejó de responder o incumplió el siguiente paso.">↳ Asesor</span>
                    <span class="badge aband-cliente" *ngIf="c.abandono && c.abandonadoPor === 'cliente'" title="La empresa respondió y el cliente no volvió a escribir.">↳ Cliente</span>
                    <span class="badge aband-no" *ngIf="c.abandono !== true">No abandonado</span>
                  </td>
                  <td class="acciones"><button type="button" (click)="detalle = c">Ver caso</button></td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="paginacion" *ngIf="totalPaginas > 0">
            <span>Página <strong>{{ pagina }}</strong> de <strong>{{ totalPaginas }}</strong></span>
            <div class="botones">
              <button type="button" [disabled]="pagina <= 1" (click)="pagina = pagina - 1">Anterior</button>
              <button type="button" [disabled]="pagina >= totalPaginas" (click)="pagina = pagina + 1">Siguiente</button>
            </div>
          </div>
        </div>
      </div>

      <div class="modal-overlay" *ngIf="resumenContacto" (click)="resumenContacto = null">
        <div class="modal" (click)="$event.stopPropagation()">
          <header>
            <div>
              <h3>{{ resumenContacto!.nombreContacto || 'Contacto sin nombre' }}</h3>
              <p>{{ resumenContacto!.idContacto }} · {{ resumenContacto!.totalCasos }} caso{{ resumenContacto!.totalCasos === 1 ? '' : 's' }}</p>
            </div>
            <button type="button" (click)="resumenContacto = null">Cerrar</button>
          </header>
          <div class="modal-body">
            <div class="caso-resumen" *ngFor="let caso of resumenContacto!.casos">
              <div class="fila-caso">
                <span class="fecha">{{ (caso.archivadaEn || '').slice(0, 10) || '—' }}</span>
                <span class="badge" [class.res-ok]="caso.procesada" [class.res-amber]="!caso.procesada">
                  {{ caso.procesada ? (caso.analisis?.resultado || 'Analizado') : 'Pendiente de análisis' }}
                </span>
              </div>
              <ng-container *ngIf="caso.analisis as a">
                <p class="motivo-texto">{{ a.resumenMotivo }}</p>
                <p class="sub-caso">{{ a.motivoContacto || 'Sin motivo' }} {{ a.sentimientoFinal ? '· ' + a.sentimientoFinal : '' }}</p>
                <button type="button" class="ver-caso" (click)="verDesdeResumen(a)">Ver caso</button>
              </ng-container>
            </div>
            <p class="vacio" *ngIf="!resumenContacto!.casos.length">Sin casos registrados.</p>
          </div>
        </div>
      </div>

      <app-liwa-analisis-detalle *ngIf="detalle" [item]="detalle" (close)="detalle = null" />
    </section>
  `,
  styles: [`
    .panel { display: flex; flex-direction: column; gap: 16px; background: #fff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 16px; }
    .cabecera { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 10px; }
    .cabecera h2 { margin: 0; font-size: 1rem; font-weight: 700; color: #1e293b; }
    .cabecera p { margin: 2px 0 0; font-size: 0.72rem; color: #64748b; }
    .acciones-top { display: flex; flex-wrap: wrap; gap: 8px; }
    .acciones-top input { border: 1px solid #e2e8f0; border-radius: 8px; padding: 6px 10px; font-size: 0.72rem; min-width: 200px; }
    .acciones-top button { border: 1px solid #e2e8f0; background: #fff; border-radius: 8px; padding: 6px 10px; font-size: 0.72rem; font-weight: 600; color: #475569; cursor: pointer; }
    .acciones-top button:hover { background: #f8fafc; }
    .aviso { display: flex; align-items: center; gap: 8px; border-radius: 12px; padding: 8px 12px; font-size: 0.72rem; }
    .aviso.info { background: #eff6ff; border: 1px solid #bfdbfe; color: #1d4ed8; }
    .aviso.error { background: #fff1f2; border: 1px solid #fecdd3; color: #be123c; }
    .spin { animation: spin 1s linear infinite; }
    @keyframes spin { to { transform: rotate(360deg); } }
    .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
    @media (max-width: 860px) { .grid { grid-template-columns: 1fr; } }
    .tarjeta { border: 1px solid #f1f5f9; border-radius: 14px; padding: 12px; }
    .orden1 { order: 1; } .orden2 { order: 2; } .orden3 { order: 3; } .orden4 { order: 4; }
    .tarjeta h3 { margin: 0; font-size: 0.82rem; font-weight: 700; color: #1e293b; }
    .fila-titulo { display: flex; align-items: center; justify-content: space-between; }
    .toggle { display: flex; gap: 4px; }
    .toggle button { border: 1px solid #e2e8f0; background: #fff; border-radius: 6px; padding: 3px 8px; font-size: 0.6rem; cursor: pointer; color: #475569; }
    .toggle button.activo { background: #1e293b; border-color: #1e293b; color: #fff; }
    .barras { margin-top: 10px; }
    .barra { display: block; width: 100%; border: none; background: transparent; text-align: left; cursor: pointer; margin-bottom: 8px; padding: 0; }
    .barra .fila { display: flex; justify-content: space-between; font-size: 0.7rem; margin-bottom: 3px; color: #475569; }
    .barra .fila span:first-child { text-transform: capitalize; }
    .track { height: 8px; border-radius: 999px; background: #f1f5f9; overflow: hidden; }
    .track.fino { height: 6px; }
    .track span { display: block; height: 100%; border-radius: 999px; }
    .chips { display: flex; flex-wrap: wrap; gap: 4px; margin-top: 10px; }
    .chip { border: 1px solid #e2e8f0; background: #fff; color: #475569; font-size: 0.62rem; font-weight: 600; padding: 4px 8px; border-radius: 6px; cursor: pointer; }
    .chip span { color: #94a3b8; }
    .chip.activo { background: #1e293b; border-color: #1e293b; color: #fff; }
    .chip.activo span { color: #cbd5e1; }
    .sub-barras { margin-top: 10px; display: flex; flex-direction: column; gap: 6px; }
    .sub-barra { border: none; background: transparent; text-align: left; cursor: pointer; padding: 0; }
    .sub-barra:disabled { opacity: 0.4; cursor: default; }
    .fila.mini { font-size: 0.62rem; color: #94a3b8; }
    .fila.mini span:first-child { text-transform: capitalize; }
    .valor { margin: 8px 0 0; font-size: 1.6rem; font-weight: 800; color: #1e293b; }
    .sub { margin: 2px 0 0; font-size: 0.7rem; color: #64748b; }
    .nota-mini { margin: 4px 0 0; font-size: 0.6rem; color: #94a3b8; }
    .enlaces { display: flex; flex-wrap: wrap; gap: 10px; margin-top: 10px; }
    .enlaces button { border: none; background: transparent; font-size: 0.68rem; font-weight: 600; color: #1d4ed8; cursor: pointer; padding: 0; }
    .enlaces button.ok { color: #047857; }
    .enlaces button.amber { color: #b45309; }
    .hint { margin: 2px 0 0; font-size: 0.6rem; color: #94a3b8; }
    .dona-wrap { position: relative; height: 220px; display: flex; align-items: center; justify-content: center; margin-top: 8px; }
    .dona { width: 200px; height: 200px; }
    .dona-centro { position: absolute; inset: 0; display: flex; flex-direction: column; align-items: center; justify-content: center; pointer-events: none; }
    .dona-centro .total { margin: 0; font-size: 1.5rem; font-weight: 800; color: #0f172a; line-height: 1; }
    .dona-centro .label { margin: 4px 0 0; font-size: 0.62rem; color: #94a3b8; }
    .leyenda { display: flex; flex-wrap: wrap; gap: 8px 14px; margin-top: 10px; }
    .item-leyenda { display: inline-flex; align-items: center; gap: 6px; border: none; background: transparent; cursor: pointer; font-size: 0.7rem; color: #475569; padding: 0; }
    .item-leyenda:hover { color: #1d4ed8; }
    .punto { width: 10px; height: 10px; border-radius: 999px; flex-shrink: 0; }
    .nombre { font-weight: 600; text-transform: capitalize; }
    .dato { color: #94a3b8; }
    .vacio { text-align: center; color: #94a3b8; font-size: 0.75rem; padding: 16px; }
    .tabla-wrap { overflow-x: auto; margin-top: 10px; }
    table.mapa { border-collapse: separate; border-spacing: 3px; width: 100%; font-size: 0.66rem; }
    table.mapa th { color: #94a3b8; font-weight: 700; text-transform: uppercase; font-size: 0.58rem; padding: 4px 6px; text-align: center; }
    table.mapa th:first-child { text-align: left; }
    table.mapa td { text-align: center; border-radius: 6px; padding: 2px; }
    .motivo-link { border: none; background: transparent; font-weight: 700; color: #475569; cursor: pointer; white-space: nowrap; padding: 4px 6px 4px 0; }
    .motivo-link:hover { color: #1d4ed8; }
    table.mapa td button { width: 100%; border: none; background: transparent; font-weight: 700; color: #334155; padding: 8px 6px; border-radius: 6px; cursor: pointer; }
    table.mapa td button:disabled { cursor: default; }
    .seccion-casos { margin-top: 4px; }
    .titulo-casos { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 8px; margin-bottom: 10px; }
    .titulo-casos h3 { margin: 0; display: flex; align-items: center; gap: 8px; font-size: 0.85rem; font-weight: 700; color: #1e293b; }
    .contador { background: #f1f5f9; color: #475569; font-size: 0.6rem; font-weight: 700; padding: 2px 8px; border-radius: 999px; }
    .chip-filtro { border: 1px solid #e2e8f0; background: #fff; border-radius: 8px; padding: 4px 8px; font-size: 0.65rem; color: #64748b; }
    .tabla-card { border: 1px solid #e2e8f0; border-radius: 14px; overflow: hidden; background: #fff; }
    .tabla-card .tabla-wrap { margin: 0; }
    table { width: 100%; border-collapse: collapse; font-size: 0.76rem; min-width: 850px; }
    th { text-align: left; padding: 10px 14px; font-size: 0.64rem; color: #94a3b8; font-weight: 600; background: #f8fafc; border-bottom: 1px solid #e2e8f0; }
    td { padding: 8px 14px; border-bottom: 1px solid #f1f5f9; color: #475569; }
    .nowrap { white-space: nowrap; }
    .truncar { max-width: 180px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .contacto { font-weight: 600; color: #1d4ed8; cursor: pointer; }
    .contacto:hover { text-decoration: underline; }
    .asesor.estimado { color: #94a3b8; }
    .tag-estimado { color: #f59e0b; font-size: 0.58rem; margin-left: 4px; }
    .badge { display: inline-flex; padding: 2px 8px; border-radius: 999px; font-size: 0.62rem; font-weight: 600; background: #f1f5f9; color: #64748b; }
    .badge.violeta { background: #f5f3ff; color: #7c3aed; }
    .badge.meta { background: #2563eb; color: #fff; font-weight: 700; margin-left: 6px; cursor: pointer; border: none; }
    .badge.sent-positivo { background: #ecfdf5; color: #047857; }
    .badge.sent-negativo { background: #fff1f2; color: #be123c; }
    .badge.sent-neutro, .badge.sent-neutral { background: #f1f5f9; color: #475569; }
    .badge.res-ok { background: #ecfdf5; color: #047857; }
    .badge.res-no { background: #fff1f2; color: #be123c; }
    .badge.res-amber { background: #fffbeb; color: #92400e; }
    .badge.res-sin { background: #f8fafc; color: #94a3b8; }
    .badge.aband-asesor { background: #fff7ed; color: #c2410c; }
    .badge.aband-cliente { background: #f8fafc; color: #475569; }
    .badge.aband-no { background: #ecfdf5; color: #059669; }
    .vacio-tabla { text-align: center; color: #94a3b8; padding: 40px !important; }
    .acciones { text-align: right; }
    .acciones button { border: none; background: #eff6ff; color: #1d4ed8; font-size: 0.68rem; font-weight: 600; padding: 6px 12px; border-radius: 8px; cursor: pointer; }
    .acciones button:hover { background: #dbeafe; }
    .paginacion { display: flex; justify-content: space-between; align-items: center; padding: 10px 16px; border-top: 1px solid #f1f5f9; background: #f8fafc; font-size: 0.7rem; color: #64748b; }
    .botones { display: flex; gap: 6px; }
    .botones button { border: 1px solid #e2e8f0; background: #fff; padding: 6px 12px; border-radius: 8px; font-size: 0.7rem; font-weight: 600; color: #475569; cursor: pointer; }
    .botones button:disabled { opacity: 0.4; cursor: not-allowed; }
    .modal-overlay { position: fixed; inset: 0; z-index: 95; background: rgba(2,6,23,0.4); display: flex; align-items: center; justify-content: center; padding: 16px; }
    .modal { width: 100%; max-width: 640px; max-height: 85vh; overflow: auto; background: #fff; border-radius: 16px; padding: 18px; }
    .modal header { display: flex; justify-content: space-between; align-items: flex-start; gap: 10px; }
    .modal header h3 { margin: 0; font-size: 0.95rem; font-weight: 700; color: #1e293b; }
    .modal header p { margin: 2px 0 0; font-size: 0.7rem; color: #64748b; }
    .modal header button { border: none; background: transparent; color: #64748b; cursor: pointer; font-size: 0.72rem; }
    .modal-body { margin-top: 14px; display: flex; flex-direction: column; gap: 8px; }
    .caso-resumen { border: 1px solid #f1f5f9; border-radius: 12px; padding: 10px; }
    .fila-caso { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 8px; font-size: 0.72rem; }
    .fila-caso .fecha { font-weight: 700; color: #334155; }
    .motivo-texto { margin: 8px 0 0; font-size: 0.72rem; color: #475569; }
    .sub-caso { margin: 4px 0 0; font-size: 0.65rem; color: #94a3b8; }
    .ver-caso { margin-top: 8px; border: none; background: transparent; color: #1d4ed8; font-size: 0.68rem; font-weight: 600; cursor: pointer; padding: 0; }
  `],
})
export class LiwaCaseReportsPanelComponent implements OnChanges {
  @Input() desde?: string;
  @Input() hasta?: string;
  // Ver comentario en ads-panel.component.ts.
  @Input() autoRefreshTick?: number;

  sentimiento: Record<string, unknown> | null = null;
  ventas: Record<string, unknown> | null = null;
  sentimientoVista: 'inicial' | 'final' = 'final';
  casos: LiwaAnalisisItem[] = [];
  agentes: Record<string, string> = {};
  filtro: Filtro = 'todos';
  segmento = '';
  motivoVista = '';
  busqueda = '';
  pagina = 1;
  detalle: LiwaAnalisisItem | null = null;
  resumenContacto: LiwaResumenContacto | null = null;
  error: string | null = null;
  cargando = true;
  readonly columnasResultado = COLUMNAS_RESULTADO;
  private peticion = 0;
  readonly POR_PAGINA = 25;

  constructor(private readonly liwa: LiwaService, private readonly cdr: ChangeDetectorRef) {}

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
      const [s, a, conversaciones, , ven] = await Promise.all([
        this.liwa.obtenerReporteSentimiento({ desde: this.desde, hasta: this.hasta }).catch(() => null),
        this.liwa.obtenerTodosLosAnalisis({ desde: this.desde, hasta: this.hasta }),
        this.liwa.obtenerConversacionesChat({ desde: this.desde, hasta: this.hasta }).catch(() => [] as LiwaChat[]),
        this.liwa.obtenerReporteSoporte({ desde: this.desde, hasta: this.hasta }).catch(() => null),
        this.liwa.obtenerReporteVentas({ desde: this.desde, hasta: this.hasta }).catch(() => null),
      ]);
      if (id !== this.peticion) return;
      const agentesPorContacto: Record<string, string> = {};
      for (const chat of conversaciones) {
        if (chat.idContacto && chat.agente) agentesPorContacto[chat.idContacto] = chat.agente;
      }
      this.sentimiento = s as Record<string, unknown> | null;
      this.casos = a;
      this.agentes = agentesPorContacto;
      this.ventas = ven as Record<string, unknown> | null;
    } catch {
      if (id === this.peticion) this.error = 'No fue posible cargar los reportes LIWA.';
    } finally {
      if (id === this.peticion) {
        this.cargando = false;
        // Ver comentario en ads-panel.component.ts.
        this.cdr.markForCheck();
      }
    }
  }

  private esNombreBot(n: string): boolean {
    return NOMBRES_BOT_SET.has(n.trim().toLowerCase());
  }

  // asesor/asesorId es el dato real de ESTE caso puntual; si viene vacío se
  // cae al agente del contacto completo (puede pertenecer a otro caso de la
  // misma persona) — por eso se marca "estimado" en la UI.
  nombreAsesor(caso: LiwaAnalisisItem): { nombre: string; esEstimado: boolean; esBot: boolean } {
    const real = caso.asesor || '';
    if (real) return { nombre: real, esEstimado: false, esBot: this.esNombreBot(real) };
    const estimado = caso.idContacto ? this.agentes[caso.idContacto] : '';
    if (estimado) return { nombre: estimado, esEstimado: !this.esNombreBot(estimado), esBot: this.esNombreBot(estimado) };
    return { nombre: '', esEstimado: false, esBot: false };
  }

  // Mismo filtro extra que DashboardCalidad/asesores-panel (analisisEnRango)
  // — evita que este panel y el de Gráficas muestren totales distintos para
  // el mismo rango, porque /analisis filtra por procesadoEn en vez de la
  // fecha real del caso.
  get casosFiltrados(): LiwaAnalisisItem[] {
    return this.casos.filter((c) => this.liwa.analisisEnRango(c, this.desde, this.hasta));
  }

  etiqueta(motivo: string | null | undefined): string {
    return this.liwa.etiquetaMotivo(motivo);
  }

  private pasaFiltroSentimiento(c: LiwaAnalisisItem): boolean {
    return c.sentimientoFinal === (this.segmento || 'negativo');
  }

  private pasaFiltroSoporte(c: LiwaAnalisisItem): boolean {
    return c.motivoContacto === 'soporte' && (!this.segmento || c.resultado === this.segmento);
  }

  private pasaFiltroMatriz(c: LiwaAnalisisItem): boolean {
    const [codigoF, resultadoF] = this.partirSegmento();
    if (codigoF && c.motivoContacto !== codigoF) return false;
    return !resultadoF || c.resultado === resultadoF;
  }

  private pasaFiltroAsesor(c: LiwaAnalisisItem): boolean {
    const [asesorF, resultadoF] = this.partirSegmento();
    if (asesorF && this.nombreAsesor(c).nombre !== asesorF) return false;
    return !resultadoF || c.resultado === resultadoF;
  }

  private pasaFiltroMapa(c: LiwaAnalisisItem): boolean {
    const [mCodigo, muni] = this.partirSegmento();
    if (mCodigo && c.motivoContacto !== mCodigo) return false;
    return !muni || (c.municipio || '').trim() === muni;
  }

  private pasaFiltroVentas(c: LiwaAnalisisItem): boolean {
    if (this.segmento === 'confirmadas') return c.oportunidadVenta === true && c.ventaConfirmadaEnTexto === true;
    if (this.segmento === 'no_confirmadas') return c.oportunidadVenta === true && c.ventaConfirmadaEnTexto !== true;
    return c.oportunidadVenta === true;
  }

  private pasaFiltroActivo(c: LiwaAnalisisItem): boolean {
    const porFiltro: Partial<Record<Filtro, (c: LiwaAnalisisItem) => boolean>> = {
      sentimiento: (x) => this.pasaFiltroSentimiento(x),
      soporte: (x) => this.pasaFiltroSoporte(x),
      matriz: (x) => this.pasaFiltroMatriz(x),
      asesor: (x) => this.pasaFiltroAsesor(x),
      mapa: (x) => this.pasaFiltroMapa(x),
      ventas: (x) => this.pasaFiltroVentas(x),
      ads: (x) => x.vieneDeAds === true,
    };
    return (porFiltro[this.filtro] ?? (() => true))(c);
  }

  private pasaBusqueda(c: LiwaAnalisisItem): boolean {
    const q = this.busqueda.trim().toLowerCase();
    if (!q) return true;
    const texto = [c.idContacto, this.nombreAsesor(c).nombre, c.motivoContacto, c.resultado, c.sentimientoFinal].filter(Boolean).join(' ').toLowerCase();
    return texto.includes(q);
  }

  get visibles(): LiwaAnalisisItem[] {
    return this.casosFiltrados
      .filter((c) => this.pasaFiltroActivo(c))
      .filter((c) => this.pasaBusqueda(c));
  }

  private partirSegmento(): [string, string] {
    const sep = this.segmento.indexOf('|');
    if (sep === -1) return [this.segmento, ''];
    return [this.segmento.slice(0, sep), this.segmento.slice(sep + 1)];
  }

  get totalPaginas(): number {
    return Math.max(1, Math.ceil(this.visibles.length / this.POR_PAGINA));
  }

  get casosPagina(): LiwaAnalisisItem[] {
    return this.visibles.slice((this.pagina - 1) * this.POR_PAGINA, this.pagina * this.POR_PAGINA);
  }

  irA(filtro: Filtro, segmento: string): void {
    this.filtro = filtro;
    this.segmento = segmento;
    this.pagina = 1;
    setTimeout(() => document.getElementById('seccion-casos')?.scrollIntoView({ behavior: 'smooth', block: 'start' }));
  }

  limpiar(): void {
    this.filtro = 'todos';
    this.segmento = '';
    this.busqueda = '';
    this.pagina = 1;
  }

  exportarCsv(): void {
    const filas = [
      ['fecha_archivada', 'fecha_conversacion', 'contacto', 'asesor', 'motivo', 'sentimiento', 'resultado'],
      ...this.visibles.map((c) => [
        c.archivadaEn || c.procesadoEn || c.createdAt || '',
        c.tsPrimerMensaje || c.tsPrimeraRespuesta || '',
        c.idContacto || '',
        this.nombreAsesor(c).nombre,
        c.motivoContacto || '',
        c.sentimientoFinal || '',
        c.resultado || '',
      ]),
    ];
    const csv = filas.map((fila) => fila.map((v) => `"${String(v).replaceAll('"', '""')}"`).join(',')).join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const enlace = document.createElement('a');
    enlace.href = URL.createObjectURL(blob);
    enlace.download = `reportes-liwa-${this.desde || 'todo'}-${this.hasta || 'hoy'}.csv`;
    enlace.click();
    URL.revokeObjectURL(enlace.href);
  }

  colorDe(clave: string): string {
    return COLORS[clave] || '#2563eb';
  }

  anchoBarra(valor: number): number {
    return Math.max(4, valor);
  }

  get sentimientos(): Barra[] {
    const s = this.sentimiento as Record<string, any> | null;
    const clave = this.sentimientoVista === 'inicial' ? 'sentimientoInicial' : 'sentimientoFinal';
    const raw = s?.[this.sentimientoVista] ?? s?.[clave] ?? s?.['distribucion'] ?? s?.['items'];
    return this.normalizarDistribucion(raw);
  }

  private normalizarDistribucion(value: unknown): Barra[] {
    if (!value) return [];
    if (Array.isArray(value)) {
      return value.map((item) => {
        const row = item as Record<string, unknown>;
        const label = aTexto(row['sentimiento'] ?? row['resultado'] ?? row['estado'] ?? row['label'] ?? 'sin_clasificar');
        const total = Number(row['total'] ?? row['cantidad'] ?? row['count'] ?? 0);
        return { label, total: Number.isFinite(total) ? total : 0 };
      });
    }
    if (typeof value === 'object') {
      return Object.entries(value as Record<string, number>).map(([label, total]) => ({ label, total }));
    }
    return [];
  }

  get matrizMotivos(): GrupoMotivo[] {
    const porMotivo = new Map<string, GrupoMotivo>();
    this.casosFiltrados.forEach((c) => {
      const codigo = c.motivoContacto || '';
      const etiqueta = this.etiqueta(codigo);
      let g = porMotivo.get(etiqueta);
      if (!g) {
        g = { codigo, etiqueta, resultados: { resuelto: 0, no_resuelto: 0, escalado: 0, abandonado: 0 }, total: 0 };
        porMotivo.set(etiqueta, g);
      }
      const r = c.resultado || '';
      if ((COLUMNAS_RESULTADO as readonly string[]).includes(r)) g.resultados[r] += 1;
      g.total += 1;
    });
    return Array.from(porMotivo.values()).sort((a, b) => b.total - a.total);
  }

  get motivoActivo(): string {
    return this.motivoVista && this.matrizMotivos.some((g) => g.etiqueta === this.motivoVista)
      ? this.motivoVista : (this.matrizMotivos[0]?.etiqueta || '');
  }

  get motivoData(): GrupoMotivo | undefined {
    return this.matrizMotivos.find((g) => g.etiqueta === this.motivoActivo);
  }

  pctResultado(r: string): number {
    const d = this.motivoData;
    if (!d?.total) return 0;
    return Math.max(2, ((d.resultados[r] || 0) / d.total) * 100);
  }

  verMotivo(g: GrupoMotivo): void {
    this.motivoVista = g.etiqueta;
    this.irA('matriz', `${g.codigo}|`);
  }

  get soporteCasos(): LiwaAnalisisItem[] {
    return this.casos.filter((c) => c.motivoContacto === 'soporte');
  }

  // Cobertura por municipio: personas distintas (idContacto) que preguntaron
  // por cobertura en cada municipio; si la misma persona preguntó varias
  // veces cuenta una sola.
  get coberturaPorMunicipio(): CoberturaFila[] {
    const porMuni = new Map<string, { municipio: string; casos: number; personas: Set<string> }>();
    this.casosFiltrados.forEach((c) => {
      if (c.motivoContacto !== 'cobertura') return;
      const muni = (c.municipio || '').trim();
      if (!muni) return;
      let g = porMuni.get(muni);
      if (!g) { g = { municipio: muni, casos: 0, personas: new Set() }; porMuni.set(muni, g); }
      g.casos += 1;
      if (c.idContacto) g.personas.add(c.idContacto);
    });
    return Array.from(porMuni.values())
      .map((g) => ({ municipio: g.municipio, casos: g.casos, personas: g.personas.size }))
      .sort((a, b) => b.personas - a.personas || b.casos - a.casos);
  }

  pctCobertura(g: CoberturaFila): number {
    const max = this.coberturaPorMunicipio[0]?.personas || 1;
    return Math.max(4, (g.personas / max) * 100);
  }

  // Mapa de calor motivo × municipio: cada municipio identificado por la IA
  // (de La Guajira o de cualquier otro lugar) aparece con su nombre real, sin
  // agrupar bajo "Otros". Casos sin municipio no se pintan.
  get matrizMapa(): MapaCalor {
    const motivosPorCodigo = new Map<string, { codigo: string; etiqueta: string }>();
    const municipios = new Set<string>();
    const valores: Record<string, number> = {};
    let max = 0;
    this.casosFiltrados.forEach((c) => {
      const muni = (c.municipio || '').trim();
      if (!muni) return;
      const codigo = c.motivoContacto || '';
      motivosPorCodigo.set(codigo, { codigo, etiqueta: this.etiqueta(codigo) });
      municipios.add(muni);
      const clave = `${codigo}|${muni}`;
      valores[clave] = (valores[clave] || 0) + 1;
      if (valores[clave] > max) max = valores[clave];
    });
    return { filas: Array.from(motivosPorCodigo.values()), columnas: Array.from(municipios).sort((a, b) => a.localeCompare(b)), valores, max };
  }

  valorCelda(codigo: string, municipio: string): number {
    return this.matrizMapa.valores[`${codigo}|${municipio}`] || 0;
  }

  fondoCelda(codigo: string, municipio: string): string {
    const mapa = this.matrizMapa;
    const v = this.valorCelda(codigo, municipio);
    const intensidad = mapa.max === 0 ? 0 : v / mapa.max;
    return `rgba(139,92,246,${0.06 + intensidad * 0.7})`;
  }

  get ventasOportunidad(): LiwaAnalisisItem[] {
    return this.casosFiltrados.filter((c) => c.oportunidadVenta === true);
  }

  get ventasConfirmadasLocal(): number {
    return this.ventasOportunidad.filter((c) => c.ventaConfirmadaEnTexto === true).length;
  }

  get totalVentas(): number {
    const totalReporte = countValue(this.ventas, ['totalOportunidades', 'oportunidades', 'total']);
    return totalReporte > 0 ? totalReporte : this.ventasOportunidad.length;
  }

  get ventasConfirmadas(): number {
    const totalReporte = countValue(this.ventas, ['totalOportunidades', 'oportunidades', 'total']);
    if (totalReporte > 0) return countValue(this.ventas, ['ventasConfirmadas', 'confirmadas', 'oportunidadesConvertidas']);
    return this.ventasConfirmadasLocal;
  }

  get tasa(): number {
    return this.totalVentas ? (this.ventasConfirmadas / this.totalVentas) * 100 : 0;
  }

  get ventasBarras(): Barra[] {
    return [
      { label: 'oportunidades', total: this.totalVentas },
      { label: 'confirmadas', total: this.ventasConfirmadas },
      { label: 'no_confirmadas', total: Math.max(0, this.totalVentas - this.ventasConfirmadas) },
    ];
  }

  // Dona de "Casos por motivo" (mismo conteo del card, en versión dona
  // clicable) construida como arcos SVG con stroke-dasharray.
  get motivosDonut(): DonutSeg[] {
    const total = this.matrizMotivos.reduce((a, g) => a + g.total, 0);
    return this.matrizMotivos.map((g, i) => ({
      codigo: g.codigo,
      etiqueta: g.etiqueta,
      valor: g.total,
      color: DONUT_COLORS[i % DONUT_COLORS.length],
      pct: total > 0 ? Math.round((g.total / total) * 100) : 0,
    }));
  }

  get totalCasosMotivos(): number {
    return this.motivosDonut.reduce((a, d) => a + d.valor, 0);
  }

  get segmentosDona(): { codigo: string; etiqueta: string; valor: number; pct: number; color: string; dash: string; offset: number }[] {
    const total = this.totalCasosMotivos;
    const circunferencia = 2 * Math.PI * 45;
    let acumulado = 0;
    return this.motivosDonut.map((d) => {
      const fraccion = total > 0 ? d.valor / total : 0;
      const largo = fraccion * circunferencia;
      const seg = { ...d, dash: `${largo} ${circunferencia - largo}`, offset: -acumulado * circunferencia };
      acumulado += fraccion;
      return seg;
    });
  }

  async abrirResumenContacto(idContacto: string): Promise<void> {
    if (!idContacto) return;
    try {
      this.resumenContacto = await this.liwa.obtenerResumenContacto(idContacto);
    } catch {
      this.error = 'No fue posible cargar el resumen del contacto.';
    }
  }

  verDesdeResumen(a: LiwaCasoResumenAnalisis): void {
    this.resumenContacto = null;
    this.detalle = a;
  }
}
