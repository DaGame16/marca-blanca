import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { LiwaService } from '../data/liwa.service';
import {
  BANDERAS_CALIDAD_LABELS, LiwaAnalisisItem, LiwaChat, LiwaMotivoIA, LiwaResultadoIA,
  MOTIVO_IA_LABELS, RESULTADO_LABELS,
} from '../models/liwa.model';
import { LiwaConversationDrawerComponent } from './conversation-drawer.component';

function formatTiempo(ms: number | null): string {
  if (ms == null || !Number.isFinite(ms)) return '—';
  const minutos = Math.round(ms / 60000);
  if (minutos < 1) return '<1 min';
  if (minutos < 60) return `${minutos} min`;
  const h = Math.floor(minutos / 60);
  const m = minutos % 60;
  return `${h} h ${m.toString().padStart(2, '0')} min`;
}

function texto(v: unknown): string {
  if (v == null) return '';
  if (typeof v === 'string') return v;
  if (typeof v === 'number' || typeof v === 'boolean') return String(v);
  // Nunca deberia llegar un objeto hasta aca -- si pasa, mejor un string
  // vacio que "[object Object]" en pantalla.
  return '';
}

interface StatsAsesor {
  clave: string;
  nombre: string;
  casosFiltrados: LiwaAnalisisItem[];
  chatsDebajo: number;
  pendientes: number;
  resueltos: number;
  noResueltos: number;
  escalados: number;
  abandonados: number;
  fcrOk: number;
  positivos: number;
  conVenta: number;
  ventasConfirmadas: number;
  banderas: Map<string, number>;
  motivos: Map<string, number>;
  sumPrimeraResp: number;
  nPrimeraResp: number;
  sumCierre: number;
  nCierre: number;
}

// Traducción de components/liwa/AsesoresPanel.tsx. La sección original "A
// cargo en el ERP (tareas asignadas)" consulta /users y /tasks del backend
// PHP del ERP de guajiranet (api.get) — marca-blanca no tiene todavía un
// módulo de tareas/usuarios ERP equivalente para consultar ahí, así que esa
// parte queda marcada "no disponible" en vez de inventar un endpoint.
@Component({
  selector: 'app-liwa-asesores-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, LiwaConversationDrawerComponent],
  template: `
    <section class="tarjeta">
      <header>
        <div>
          <h2>Asesores</h2>
          <p>Todo lo que hacen y lo que no, por cada asesor — dentro del rango de fechas.</p>
        </div>
        <span class="total">{{ asesores.length }} asesores · {{ totalCasos }} casos</span>
      </header>

      <div class="ranking">
        <p class="etiqueta">Casos por asesor</p>
        <p class="vacio" *ngIf="!asesores.length">Sin asesores en el periodo.</p>
        <div class="chips" *ngIf="asesores.length">
          <button type="button" class="chip" *ngFor="let s of asesores" [class.activo]="detalleAsesor === s.clave" (click)="detalleAsesor = s.clave; filtroDetalle = 'TODOS'">
            {{ s.nombre }} <span>({{ s.casosFiltrados.length }})</span>
          </button>
        </div>
        <div class="totales">
          <div class="t"><p class="l">Casos</p><p class="v">{{ totalCasosGlobal }}</p></div>
          <div class="t"><p class="l">Resueltos</p><p class="v ok">{{ tResueltos }}</p></div>
          <div class="t"><p class="l">No resueltos</p><p class="v no">{{ tNoResueltos }}</p></div>
          <div class="t"><p class="l">Escalados</p><p class="v amber">{{ tEscalados }}</p></div>
          <div class="t"><p class="l">Abandonados</p><p class="v">{{ tAbandonados }}</p></div>
        </div>
      </div>

      <p class="error" *ngIf="error">{{ error }}</p>

      <div class="filtros">
        <div class="buscador">
          <mat-icon>search</mat-icon>
          <input [(ngModel)]="busqueda" placeholder="Buscar asesor…" />
        </div>
        <select [(ngModel)]="filtroMotivo">
          <option value="TODOS">Motivo: todos</option>
          <option *ngFor="let m of motivoEntries" [value]="m[0]">{{ m[1] }}</option>
        </select>
        <select [(ngModel)]="filtroResultado">
          <option value="TODOS">Resultado: todos</option>
          <option *ngFor="let r of resultadoEntries" [value]="r[0]">{{ r[1] }}</option>
        </select>
        <label class="check"><input type="checkbox" [(ngModel)]="soloPendientes" /> Solo con pendientes</label>
      </div>

      <div class="cargando" *ngIf="cargando"><mat-icon class="spin">progress_activity</mat-icon> Cargando asesores…</div>
      <p class="vacio" *ngIf="!cargando && !visiblesFiltrados.length">Sin asesores en el periodo.</p>

      <div class="tabla-wrap" *ngIf="!cargando && visiblesFiltrados.length">
        <table>
          <thead>
            <tr>
              <th>Asesor</th><th class="c">Casos</th><th class="c">Resueltos</th><th class="c">No resueltos</th>
              <th class="c">Escalados</th><th class="c">Abandonados</th><th class="c">FCR</th><th class="c">CSAT</th>
              <th class="c">1ra resp.</th><th class="c">Cierre</th><th class="c">Ventas</th><th class="c">Pendientes</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let s of visiblesFiltrados" (click)="abrirAsesor(s.clave)" class="fila">
              <td>
                <div class="asesor-cell">
                  <span class="avatar"><mat-icon>person</mat-icon></span>
                  <div>
                    <p class="nombre">{{ s.nombre }}</p>
                    <p class="sub">{{ s.chatsDebajo }} conversaciones atendidas</p>
                  </div>
                </div>
              </td>
              <td class="c fuerte">{{ s.casosFiltrados.length }}</td>
              <td class="c">
                <button type="button" class="mini" [class.ok]="pct(s.resueltos, s.casosFiltrados.length) >= 80" [class.amber]="pct(s.resueltos, s.casosFiltrados.length) >= 50 && pct(s.resueltos, s.casosFiltrados.length) < 80" [class.no]="pct(s.resueltos, s.casosFiltrados.length) < 50" (click)="$event.stopPropagation(); abrirResultado(s.clave, 'resuelto')">{{ pct(s.resueltos, s.casosFiltrados.length) }}%</button>
              </td>
              <td class="c"><button type="button" class="mini no" (click)="$event.stopPropagation(); abrirResultado(s.clave, 'no_resuelto')">{{ s.noResueltos }}</button></td>
              <td class="c"><button type="button" class="mini amber" (click)="$event.stopPropagation(); abrirResultado(s.clave, 'escalado')">{{ s.escalados }}</button></td>
              <td class="c">{{ s.abandonados }}</td>
              <td class="c">{{ pct(s.fcrOk, s.casosFiltrados.length) }}%</td>
              <td class="c">{{ pct(s.positivos, s.casosFiltrados.length) }}%</td>
              <td class="c">{{ s.nPrimeraResp > 0 ? formatTiempo(s.sumPrimeraResp / s.nPrimeraResp) : '—' }}</td>
              <td class="c">{{ s.nCierre > 0 ? formatTiempo(s.sumCierre / s.nCierre) : '—' }}</td>
              <td class="c ok-text">{{ s.conVenta ? (s.ventasConfirmadas + '/' + s.conVenta) : '—' }}</td>
              <td class="c">
                <span class="pendiente" *ngIf="s.pendientes > 0"><mat-icon>warning</mat-icon>{{ s.pendientes }}</span>
                <span class="cero" *ngIf="!s.pendientes">0</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <p class="nota">
        FCR = resueltas a la primera · CSAT = % sentimiento final positivo · Pendientes = conversaciones archivadas sin
        análisis IA todavía (lo que falta por hacer). Clic en una fila para ver los casos del asesor; clic en un conteo
        (no resueltos, abandonados…) para ver solo esos casos.
      </p>

      <div class="modal-overlay" *ngIf="asesorDetalle" (click)="detalleAsesor = null">
        <div class="modal" (click)="$event.stopPropagation()">
          <header>
            <div class="asesor-cell">
              <span class="avatar"><mat-icon>person</mat-icon></span>
              <div>
                <h3>{{ asesorDetalle.nombre }}</h3>
                <p>{{ asesorDetalle.casosFiltrados.length }} casos · {{ asesorDetalle.chatsDebajo }} conversaciones · {{ asesorDetalle.pendientes }} pendientes</p>
              </div>
            </div>
            <button type="button" (click)="detalleAsesor = null"><mat-icon>close</mat-icon></button>
          </header>
          <div class="modal-body">
            <div class="grid-mini">
              <button type="button" class="mini-card" [class.activo]="filtroDetalle === 'resuelto'" (click)="toggleDetalle('resuelto')">
                <p class="l">Resueltos</p><p class="v ok">{{ asesorDetalle.resueltos }}</p>
              </button>
              <button type="button" class="mini-card" [class.activo]="filtroDetalle === 'no_resuelto'" (click)="toggleDetalle('no_resuelto')">
                <p class="l">No resueltos</p><p class="v no">{{ asesorDetalle.noResueltos }}</p>
              </button>
              <button type="button" class="mini-card" [class.activo]="filtroDetalle === 'escalado'" (click)="toggleDetalle('escalado')">
                <p class="l">Escalados</p><p class="v amber">{{ asesorDetalle.escalados }}</p>
              </button>
              <div class="mini-card sin-clic"><p class="l">Abandonados</p><p class="v">{{ asesorDetalle.abandonados }}</p></div>
              <div class="mini-card sin-clic"><p class="l">FCR</p><p class="v">{{ pct(asesorDetalle.fcrOk, asesorDetalle.casosFiltrados.length) }}%</p></div>
              <div class="mini-card sin-clic"><p class="l">CSAT</p><p class="v">{{ pct(asesorDetalle.positivos, asesorDetalle.casosFiltrados.length) }}%</p></div>
            </div>

            <div class="pendiente-aviso" *ngIf="asesorDetalle.pendientes > 0">
              <p class="titulo-aviso">Lo que falta por hacer</p>
              <p>{{ asesorDetalle.pendientes }} conversaciones archivadas sin análisis IA todavía.</p>
            </div>

            <div class="erp-box">
              <p class="etiqueta"><mat-icon>assignment</mat-icon> A cargo en el ERP (tareas asignadas)</p>
              <p class="erp-nota">No disponible: marca-blanca todavía no tiene un módulo de tareas/usuarios ERP conectado para consultar esta información (en guajiranet salía de /users y /tasks del ERP propio).</p>
            </div>

            <div class="grid2">
              <div class="box">
                <p class="etiqueta">Motivos</p>
                <div class="tags">
                  <span class="tag indigo" *ngFor="let m of motivosArray(asesorDetalle)">{{ m[0] }} · {{ m[1] }}</span>
                </div>
              </div>
              <div class="box">
                <p class="etiqueta">Banderas de calidad</p>
                <div class="tags" *ngIf="asesorDetalle.banderas.size">
                  <span class="tag amber" *ngFor="let b of banderasArray(asesorDetalle)">{{ etiquetaBandera(b[0]) }} · {{ b[1] }}</span>
                </div>
                <p class="sin-banderas" *ngIf="!asesorDetalle.banderas.size">Sin banderas — todo en orden</p>
              </div>
            </div>

            <div class="filtro-casos">
              <button type="button" [class.activo]="filtroDetalle === 'TODOS'" (click)="filtroDetalle = 'TODOS'">Todos ({{ asesorDetalle.casosFiltrados.length }})</button>
              <button type="button" [class.activo]="filtroDetalle === 'resuelto'" (click)="filtroDetalle = 'resuelto'">Resueltos ({{ contarResultado(asesorDetalle, 'resuelto') }})</button>
              <button type="button" [class.activo]="filtroDetalle === 'no_resuelto'" (click)="filtroDetalle = 'no_resuelto'">No resueltos ({{ contarResultado(asesorDetalle, 'no_resuelto') }})</button>
              <button type="button" [class.activo]="filtroDetalle === 'escalado'" (click)="filtroDetalle = 'escalado'">Escalados ({{ contarResultado(asesorDetalle, 'escalado') }})</button>
            </div>

            <div class="lista-casos">
              <p class="vacio" *ngIf="!casosDetalle.length">Sin casos con los filtros actuales.</p>
              <button type="button" class="caso" *ngFor="let a of casosDetalle.slice(0, 60)" [disabled]="!tieneChat(a)" (click)="abrirChatCaso(a)">
                <div class="min0">
                  <p class="motivo">{{ a.resumenMotivo || 'Sin resumen' }}</p>
                  <p class="sub">{{ etiquetaMotivo(a.motivoContacto) }} · {{ a.municipio || 'sin municipio' }} · {{ a.archivadaEn ? (a.archivadaEn | date: 'dd/MM/yyyy') : '' }}</p>
                </div>
                <span class="badge" [class]="'res-' + (a.resultado || 'na')">{{ a.resultado ? RESULTADO_LABELS[a.resultado] : 'Sin clasificar' }}</span>
              </button>
              <p class="mostrando" *ngIf="casosDetalle.length > 60">Mostrando 60 de {{ casosDetalle.length }} casos.</p>
            </div>
          </div>
        </div>
      </div>

      <app-liwa-conversation-drawer [chat]="chatAbierto" (close)="chatAbierto = null" />
    </section>
  `,
  styles: [`
    .tarjeta { background: #fff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 16px; }
    header { display: flex; flex-wrap: wrap; justify-content: space-between; align-items: center; gap: 8px; margin-bottom: 14px; }
    header h2 { margin: 0; font-size: 1rem; font-weight: 700; color: #1e293b; }
    header p { margin: 2px 0 0; font-size: 0.75rem; color: #64748b; }
    .total { font-size: 0.72rem; color: #64748b; }
    .ranking { border: 1px solid #f1f5f9; border-radius: 12px; padding: 12px; margin-bottom: 14px; }
    .etiqueta { margin: 0 0 8px; font-size: 0.6rem; font-weight: 700; color: #94a3b8; text-transform: uppercase; }
    .chips { display: flex; gap: 6px; overflow-x: auto; padding-bottom: 4px; }
    .chip { flex-shrink: 0; border: 1px solid #e2e8f0; background: #fff; color: #475569; font-size: 0.62rem; font-weight: 600; padding: 4px 8px; border-radius: 6px; cursor: pointer; }
    .chip span { color: #94a3b8; }
    .chip.activo { background: #1e293b; border-color: #1e293b; color: #fff; }
    .chip.activo span { color: #cbd5e1; }
    .totales { display: grid; grid-template-columns: repeat(auto-fit, minmax(100px, 1fr)); gap: 8px; margin-top: 10px; }
    .t { background: #f8fafc; border: 1px solid #f1f5f9; border-radius: 10px; padding: 8px 10px; }
    .t .l { margin: 0; font-size: 0.58rem; font-weight: 700; color: #94a3b8; text-transform: uppercase; }
    .t .v { margin: 2px 0 0; font-size: 1.05rem; font-weight: 800; color: #1e293b; }
    .t .v.ok { color: #059669; } .t .v.no { color: #e11d48; } .t .v.amber { color: #d97706; }
    .error { background: #fff1f2; color: #be123c; font-size: 0.72rem; padding: 8px 12px; border-radius: 8px; margin-bottom: 10px; }
    .filtros { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; margin-bottom: 12px; }
    .buscador { position: relative; min-width: 180px; }
    .buscador mat-icon { position: absolute; left: 8px; top: 50%; transform: translateY(-50%); font-size: 15px; width: 15px; height: 15px; color: #94a3b8; }
    .buscador input { padding: 7px 10px 7px 28px; font-size: 0.75rem; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; }
    select { padding: 7px 8px; font-size: 0.72rem; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; }
    .check { display: inline-flex; align-items: center; gap: 6px; font-size: 0.72rem; color: #475569; }
    .cargando { display: flex; align-items: center; justify-content: center; gap: 8px; padding: 36px; color: #94a3b8; font-size: 0.75rem; }
    .spin { animation: spin 1s linear infinite; }
    @keyframes spin { to { transform: rotate(360deg); } }
    .vacio { text-align: center; color: #94a3b8; font-size: 0.8rem; padding: 24px; }
    .tabla-wrap { overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; font-size: 0.75rem; }
    th { text-align: left; padding: 8px 6px; font-size: 0.6rem; font-weight: 700; text-transform: uppercase; color: #94a3b8; border-bottom: 1px solid #f1f5f9; }
    th.c, td.c { text-align: center; }
    td { padding: 8px 6px; border-bottom: 1px solid #f8fafc; }
    .fila { cursor: pointer; }
    .fila:hover { background: #ecfdf5; }
    .asesor-cell { display: flex; align-items: center; gap: 8px; }
    .avatar { width: 28px; height: 28px; border-radius: 8px; background: #eef2ff; color: #4f46e5; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
    .avatar mat-icon { font-size: 15px; width: 15px; height: 15px; }
    .nombre { margin: 0; font-size: 0.75rem; font-weight: 700; color: #1e293b; }
    .sub { margin: 0; font-size: 0.62rem; color: #94a3b8; }
    .fuerte { font-weight: 700; color: #1e293b; }
    .mini { border: none; background: transparent; cursor: pointer; font-weight: 700; font-size: 0.7rem; padding: 2px 6px; border-radius: 6px; }
    .mini.ok { color: #059669; } .mini.amber { color: #d97706; } .mini.no { color: #e11d48; }
    .mini:hover { background: #f1f5f9; }
    .pendiente { display: inline-flex; align-items: center; gap: 3px; color: #d97706; font-weight: 700; font-size: 0.7rem; }
    .pendiente mat-icon { font-size: 13px; width: 13px; height: 13px; }
    .cero { color: #cbd5e1; font-size: 0.7rem; }
    .ok-text { color: #047857; font-weight: 700; }
    .nota { margin: 14px 0 0; font-size: 0.63rem; color: #94a3b8; line-height: 1.4; }
    .modal-overlay { position: fixed; inset: 0; z-index: 95; background: rgba(2,6,23,0.45); display: flex; align-items: center; justify-content: center; padding: 16px; }
    .modal { width: 100%; max-width: 860px; max-height: 88vh; background: #fff; border-radius: 16px; overflow: hidden; display: flex; flex-direction: column; }
    .modal header { padding: 14px 18px; border-bottom: 1px solid #f1f5f9; margin: 0; }
    .modal header h3 { margin: 0; font-size: 0.95rem; font-weight: 800; color: #1e293b; }
    .modal header p { margin: 2px 0 0; font-size: 0.68rem; color: #94a3b8; }
    .modal header button { border: none; background: transparent; color: #94a3b8; cursor: pointer; padding: 6px; border-radius: 8px; }
    .modal-body { overflow-y: auto; padding: 14px 18px; }
    .grid-mini { display: grid; grid-template-columns: repeat(auto-fit, minmax(110px, 1fr)); gap: 8px; margin-bottom: 12px; }
    .mini-card { text-align: left; border: 1px solid #f1f5f9; background: #f8fafc; border-radius: 10px; padding: 8px 10px; cursor: pointer; }
    .mini-card.sin-clic { cursor: default; }
    .mini-card.activo { border-color: #6ee7b7; background: #ecfdf5; }
    .mini-card .l { margin: 0; font-size: 0.58rem; font-weight: 700; color: #94a3b8; text-transform: uppercase; }
    .mini-card .v { margin: 2px 0 0; font-size: 1rem; font-weight: 800; color: #1e293b; }
    .mini-card .v.ok { color: #059669; } .mini-card .v.no { color: #e11d48; } .mini-card .v.amber { color: #d97706; }
    .pendiente-aviso { background: #fffbeb; border: 1px solid #fde68a; border-radius: 12px; padding: 10px 12px; margin-bottom: 12px; }
    .pendiente-aviso .titulo-aviso { margin: 0; font-size: 0.7rem; font-weight: 700; color: #92400e; }
    .pendiente-aviso p:last-child { margin: 2px 0 0; font-size: 0.68rem; color: #b45309; }
    .erp-box { background: #f8fafc; border: 1px dashed #cbd5e1; border-radius: 12px; padding: 10px 12px; margin-bottom: 12px; }
    .erp-box .etiqueta { display: flex; align-items: center; gap: 6px; margin: 0 0 4px; font-size: 0.6rem; font-weight: 700; color: #94a3b8; text-transform: uppercase; }
    .erp-box .etiqueta mat-icon { font-size: 13px; width: 13px; height: 13px; }
    .erp-nota { margin: 0; font-size: 0.68rem; color: #64748b; }
    .grid2 { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 10px; margin-bottom: 12px; }
    .box { border: 1px solid #f1f5f9; border-radius: 12px; padding: 10px; }
    .tags { display: flex; flex-wrap: wrap; gap: 4px; }
    .tag { font-size: 0.62rem; font-weight: 700; padding: 2px 8px; border-radius: 999px; }
    .tag.indigo { background: #eef2ff; color: #4338ca; }
    .tag.amber { background: #fffbeb; color: #92400e; }
    .sin-banderas { margin: 0; font-size: 0.68rem; color: #94a3b8; }
    .filtro-casos { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 10px; }
    .filtro-casos button { border: 1px solid #e2e8f0; background: #fff; color: #475569; font-size: 0.65rem; font-weight: 700; padding: 5px 10px; border-radius: 999px; cursor: pointer; }
    .filtro-casos button.activo { background: #1e293b; border-color: #1e293b; color: #fff; }
    .lista-casos { display: flex; flex-direction: column; gap: 6px; }
    .caso { display: flex; align-items: center; justify-content: space-between; gap: 10px; border: 1px solid #f1f5f9; background: #f8fafc; border-radius: 10px; padding: 8px 10px; cursor: pointer; text-align: left; }
    .caso:hover:not(:disabled) { background: #f1f5f9; }
    .caso:disabled { opacity: 0.6; cursor: default; }
    .caso .motivo { margin: 0; font-size: 0.72rem; font-weight: 600; color: #334155; }
    .caso .sub { margin: 0; font-size: 0.62rem; color: #94a3b8; }
    .badge { flex-shrink: 0; padding: 2px 8px; border-radius: 999px; font-size: 0.62rem; font-weight: 700; background: #f1f5f9; color: #64748b; }
    .badge.res-resuelto { background: #ecfdf5; color: #047857; }
    .badge.res-no_resuelto { background: #fff1f2; color: #be123c; }
    .badge.res-escalado { background: #fffbeb; color: #92400e; }
    .mostrando { text-align: center; font-size: 0.62rem; color: #94a3b8; }
  `],
})
export class LiwaAsesoresPanelComponent implements OnChanges {
  @Input() desde?: string;
  @Input() hasta?: string;
  // Ver comentario en ads-panel.component.ts.
  @Input() autoRefreshTick?: number;

  readonly MOTIVO_IA_LABELS = MOTIVO_IA_LABELS;
  readonly RESULTADO_LABELS = RESULTADO_LABELS;
  readonly motivoEntries = Object.entries(MOTIVO_IA_LABELS);
  readonly resultadoEntries = Object.entries(RESULTADO_LABELS);

  // motivoContacto es texto libre de la IA -- no siempre calza con una llave
  // conocida de MOTIVO_IA_LABELS (ver nota en liwa.model.ts).
  etiquetaMotivo(motivo: string | null | undefined): string {
    if (!motivo) {
      return 'Sin motivo';
    }
    return MOTIVO_IA_LABELS[motivo as LiwaMotivoIA] || motivo;
  }

  analisis: LiwaAnalisisItem[] = [];
  chats: { idContacto: string; agente: string | null }[] = [];
  conversaciones: LiwaChat[] = [];
  cargando = true;
  error: string | null = null;
  busqueda = '';
  filtroMotivo: LiwaMotivoIA | 'TODOS' = 'TODOS';
  filtroResultado: LiwaResultadoIA | 'TODOS' = 'TODOS';
  soloPendientes = false;
  detalleAsesor: string | null = null;
  filtroDetalle: string = 'TODOS';
  chatAbierto: LiwaChat | null = null;
  private peticion = 0;

  constructor(private readonly liwa: LiwaService) {}

  ngOnChanges(): void {
    void this.cargar();
  }

  formatTiempo(ms: number): string {
    return formatTiempo(ms);
  }

  private async cargar(): Promise<void> {
    const id = ++this.peticion;
    this.cargando = true;
    this.error = null;
    try {
      const [items, conversaciones] = await Promise.all([
        this.liwa.obtenerTodosLosAnalisis({ desde: this.desde, hasta: this.hasta }),
        this.liwa.obtenerConversacionesChat({ desde: this.desde, hasta: this.hasta }),
      ]);
      if (id !== this.peticion) return;
      this.analisis = Array.isArray(items) ? items : [];
      this.conversaciones = Array.isArray(conversaciones) ? conversaciones : [];
      this.chats = this.conversaciones.map((c) => ({ idContacto: texto(c.idContacto), agente: texto(c.agente) || null }));
    } catch {
      if (id === this.peticion) this.error = 'No fue posible cargar la información de asesores.';
    } finally {
      if (id === this.peticion) this.cargando = false;
    }
  }

  private get visibles(): LiwaAnalisisItem[] {
    return this.analisis.filter((a) => this.liwa.analisisEnRango(a, this.desde, this.hasta));
  }

  private get analizadosIds(): Set<string> {
    return new Set(this.visibles.map((a) => a.idContacto).filter((id): id is string => !!id));
  }

  private get casosFiltrados(): LiwaAnalisisItem[] {
    return this.visibles.filter((a) =>
      (this.filtroMotivo === 'TODOS' || a.motivoContacto === this.filtroMotivo) &&
      (this.filtroResultado === 'TODOS' || a.resultado === this.filtroResultado));
  }

  get asesores(): StatsAsesor[] {
    const agentesPorContacto = new Map<string, string>();
    this.chats.forEach((c) => { if (c.idContacto && c.agente) agentesPorContacto.set(c.idContacto, c.agente); });
    const nombreDe = (a: LiwaAnalisisItem) =>
      texto(a.asesor) || (a.idContacto ? agentesPorContacto.get(a.idContacto) || '' : '') || texto(a.asesorId);

    const map = new Map<string, StatsAsesor>();
    const nueva = (clave: string, nombre: string): StatsAsesor => ({
      clave, nombre, casosFiltrados: [], chatsDebajo: 0, pendientes: 0,
      resueltos: 0, noResueltos: 0, escalados: 0, abandonados: 0, fcrOk: 0, positivos: 0,
      conVenta: 0, ventasConfirmadas: 0, banderas: new Map(), motivos: new Map(),
      sumPrimeraResp: 0, nPrimeraResp: 0, sumCierre: 0, nCierre: 0,
    });

    this.casosFiltrados.forEach((a) => {
      const nombreA = nombreDe(a);
      const clave = texto(nombreA) || '(sin asesor)';
      const s = map.get(clave) || nueva(clave, nombreA || 'Sin asesor');
      s.casosFiltrados.push(a);
      if (a.resultado === 'resuelto') s.resueltos++;
      else if (a.resultado === 'no_resuelto') s.noResueltos++;
      else if (a.resultado === 'escalado') s.escalados++;
      if (a.abandono === true) s.abandonados++;
      if (a.fcr === true) s.fcrOk++;
      if (a.sentimientoFinal === 'positivo') s.positivos++;
      if (a.oportunidadVenta === true) s.conVenta++;
      if (a.oportunidadVenta === true && a.ventaConfirmadaEnTexto === true) s.ventasConfirmadas++;
      (a.banderasCalidad || []).forEach((b) => {
        const bb = texto(b);
        if (bb) s.banderas.set(bb, (s.banderas.get(bb) || 0) + 1);
      });
      const mk = texto(a.motivoContacto);
      if (mk) {
        const etiqueta = MOTIVO_IA_LABELS[mk as LiwaMotivoIA] || mk;
        s.motivos.set(etiqueta, (s.motivos.get(etiqueta) || 0) + 1);
      }
      if (a.tsPrimerMensaje && a.tsPrimeraRespuesta) {
        s.nPrimeraResp++;
        s.sumPrimeraResp += new Date(a.tsPrimeraRespuesta).getTime() - new Date(a.tsPrimerMensaje).getTime();
      }
      if (a.tsPrimerMensaje && a.tsCierre) {
        s.nCierre++;
        s.sumCierre += new Date(a.tsCierre).getTime() - new Date(a.tsPrimerMensaje).getTime();
      }
      map.set(clave, s);
    });

    const contador = new Map<string, number>();
    this.chats.forEach((c) => {
      const clave = texto(c.agente) || '(sin asesor)';
      contador.set(clave, (contador.get(clave) || 0) + 1);
    });
    Array.from(contador.entries()).forEach(([clave]) => {
      if (!map.has(clave)) map.set(clave, nueva(clave, clave));
    });

    const analizadosIds = this.analizadosIds;
    const pendientesPorClave = new Map<string, number>();
    this.chats.forEach((c) => {
      const clave = texto(c.agente) || '(sin asesor)';
      if (!analizadosIds.has(c.idContacto)) pendientesPorClave.set(clave, (pendientesPorClave.get(clave) || 0) + 1);
    });

    return Array.from(map.values())
      .map((s) => ({ ...s, pendientes: pendientesPorClave.get(s.clave) || 0, chatsDebajo: contador.get(s.clave) || 0 }))
      .filter((s) => s.nombre !== 'Sin asesor' && s.clave !== '(sin asesor)')
      .sort((a, b) => b.casosFiltrados.length - a.casosFiltrados.length);
  }

  get visiblesFiltrados(): StatsAsesor[] {
    return this.asesores.filter((s) => {
      if (this.busqueda && !s.nombre.toLowerCase().includes(this.busqueda.toLowerCase())) return false;
      if (this.soloPendientes && s.pendientes === 0) return false;
      return true;
    });
  }

  get totalCasos(): number {
    return this.visiblesFiltrados.reduce((n, s) => n + s.casosFiltrados.length, 0);
  }

  get totalCasosGlobal(): number {
    return this.asesores.reduce((n, s) => n + s.casosFiltrados.length, 0);
  }

  get tResueltos(): number { return this.asesores.reduce((n, s) => n + s.resueltos, 0); }
  get tNoResueltos(): number { return this.asesores.reduce((n, s) => n + s.noResueltos, 0); }
  get tEscalados(): number { return this.asesores.reduce((n, s) => n + s.escalados, 0); }
  get tAbandonados(): number { return this.asesores.reduce((n, s) => n + s.abandonados, 0); }

  pct(n: number, d: number): number {
    return d > 0 ? Math.round((n / d) * 100) : 0;
  }

  get asesorDetalle(): StatsAsesor | null {
    return this.detalleAsesor ? this.asesores.find((s) => s.clave === this.detalleAsesor) || null : null;
  }

  abrirAsesor(clave: string): void {
    this.filtroDetalle = 'TODOS';
    this.detalleAsesor = clave;
  }

  abrirResultado(clave: string, r: LiwaResultadoIA): void {
    this.filtroDetalle = r;
    this.detalleAsesor = clave;
  }

  toggleDetalle(r: string): void {
    this.filtroDetalle = this.filtroDetalle === r ? 'TODOS' : r;
  }

  contarResultado(s: StatsAsesor, r: LiwaResultadoIA): number {
    return s.casosFiltrados.filter((a) => a.resultado === r).length;
  }

  motivosArray(s: StatsAsesor): [string, number][] {
    return Array.from(s.motivos.entries());
  }

  banderasArray(s: StatsAsesor): [string, number][] {
    return Array.from(s.banderas.entries());
  }

  etiquetaBandera(b: string): string {
    return BANDERAS_CALIDAD_LABELS[b] || b;
  }

  get casosDetalle(): LiwaAnalisisItem[] {
    const s = this.asesorDetalle;
    if (!s) return [];
    return this.filtroDetalle === 'TODOS' ? s.casosFiltrados : s.casosFiltrados.filter((a) => a.resultado === this.filtroDetalle);
  }

  tieneChat(a: LiwaAnalisisItem): boolean {
    return this.conversaciones.some((c) => c.idContacto === a.idContacto);
  }

  abrirChatCaso(a: LiwaAnalisisItem): void {
    const chat = this.conversaciones.find((c) => c.idContacto === a.idContacto);
    if (!chat) return;
    this.chatAbierto = chat;
  }
}
