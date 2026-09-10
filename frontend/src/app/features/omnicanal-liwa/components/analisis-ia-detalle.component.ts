import { Component, EventEmitter, Input, OnChanges, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { LiwaService, parsearHistorial } from '../data/liwa.service';
import { ABANDONADO_POR_LABELS, LiwaAnalisisDetalle, LiwaAnalisisItem, LiwaTurnoAnalizado } from '../models/liwa.model';

const FCR_DESCRIPCION = 'FCR (resolución en el primer contacto): el problema que escribió el cliente se respondió de una vez';

function esTurnoCliente(t: LiwaTurnoAnalizado): boolean {
  if (t.autor) return t.autor === 'cliente';
  if (typeof t.esCliente === 'boolean') return t.esCliente;
  return (t.nombre || '').trim().toLowerCase() === 'usuario';
}

// Traducción de DetalleAnalisis en components/liwa/AnalisisIA.tsx — modal
// de detalle de un caso analizado: alertas de calidad, métricas y la
// conversación completa en burbujas estilo WhatsApp.
@Component({
  selector: 'app-liwa-analisis-detalle',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  template: `
    <div class="overlay" (click)="close.emit()">
      <div class="modal" (click)="$event.stopPropagation()">
        <header (click)="infoAbierta = !infoAbierta">
          <div class="titulo">
            <span class="icono"><mat-icon>psychology</mat-icon></span>
            <div class="min0">
              <span class="motivo">{{ resumenMotivo || 'Análisis IA' }}</span>
              <span class="meta">
                {{ item.idContacto ? ('Contacto ' + item.idContacto) : 'Conversación analizada' }}
                <ng-container *ngIf="municipio"> · {{ municipio }}{{ barrio ? (', ' + barrio) : '' }}</ng-container>
              </span>
            </div>
          </div>
          <button type="button" class="cerrar" (click)="$event.stopPropagation(); close.emit()"><mat-icon>close</mat-icon></button>
        </header>

        <div class="info" *ngIf="infoAbierta">
          <div class="alertas" *ngIf="sinRespuesta || esperoDemasiado || tratoInadecuado || gestionPendiente || oportunidadVenta">
            <span class="alerta amber" *ngIf="sinRespuesta"><mat-icon>warning</mat-icon> Sin respuesta</span>
            <span class="alerta amber" *ngIf="esperoDemasiado"><mat-icon>schedule</mat-icon> Esperó demasiado</span>
            <span class="alerta blue" *ngIf="gestionPendiente" title="La empresa dejó algo prometido sin cerrar"><mat-icon>event_repeat</mat-icon> Gestión pendiente</span>
            <span class="alerta rose" *ngIf="tratoInadecuado"><mat-icon>person_off</mat-icon> Trato inadecuado</span>
            <span class="alerta emerald" *ngIf="oportunidadVenta"><mat-icon>attach_money</mat-icon> Oportunidad de venta</span>
          </div>

          <div class="resumen" *ngIf="resumenMotivo || resumenDesenlace">
            <p *ngIf="resumenMotivo"><strong>Motivo: </strong>{{ resumenMotivo }}</p>
            <p *ngIf="resumenDesenlace"><strong>Desenlace: </strong>{{ resumenDesenlace }}</p>
          </div>

          <div class="metricas">
            <div class="metrica">
              <p class="m-label">Resultado</p>
              <span class="badge" [class]="'res-' + (resultado || 'na')">{{ resultado || 'Sin clasificar' }}</span>
              <p class="m-sub" *ngIf="abandono && abandonadoPor">Abandonó: {{ abandonadoInfo?.label }}</p>
            </div>
            <div class="metrica">
              <p class="m-label">Sentimiento final</p>
              <span class="badge" [class]="'sent-' + (sentimientoFinal || 'na')">{{ sentimientoFinal || 'Sin dato' }}</span>
            </div>
            <div class="metrica">
              <p class="m-label">FCR (1er contacto)</p>
              <span class="fcr" [class.si]="fcr">
                <mat-icon>{{ fcr ? 'check_circle' : 'schedule' }}</mat-icon> {{ fcr ? 'Sí' : 'No' }}
              </span>
            </div>
            <div class="metrica">
              <p class="m-label">Esfuerzo del cliente</p>
              <span class="badge" [class]="'esf-' + (esfuerzo || 'na')">{{ esfuerzo || 'Sin dato' }}</span>
            </div>
          </div>

          <p class="fcr-nota"><mat-icon>info</mat-icon> {{ FCR_DESCRIPCION }}. {{ fcr ? 'Este problema sí se resolvió a la primera.' : 'Este problema requirió más de un contacto o repeticiones.' }}</p>

          <div class="temas" *ngIf="temas?.length">
            <span class="tema" *ngFor="let t of temas">{{ t }}</span>
          </div>

          <div class="razonamiento" *ngIf="razonamiento">
            <button type="button" (click)="mostrarRazonamiento = !mostrarRazonamiento">
              {{ mostrarRazonamiento ? 'Ocultar ▴' : 'Ver razonamiento del modelo ▾' }}
            </button>
            <p *ngIf="mostrarRazonamiento">{{ razonamiento }}</p>
          </div>
        </div>

        <div class="conversacion">
          <div class="cargando" *ngIf="cargando"><mat-icon class="spin">progress_activity</mat-icon> Cargando conversación…</div>
          <p class="vacio" *ngIf="!cargando && !turnos.length">El análisis no incluye los turnos de la conversación.</p>
          <div class="burbuja-fila" *ngFor="let t of turnos" [class.cliente]="esCliente(t)">
            <div class="burbuja" [class.cliente]="esCliente(t)">
              <p class="nombre" *ngIf="!esCliente(t) && t.nombre">{{ t.nombre }}</p>
              <p class="texto">{{ t.mensaje }}</p>
              <span class="fecha">{{ t.fecha | date: 'dd/MM HH:mm' }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .overlay { position: fixed; inset: 0; z-index: 110; background: rgba(2,6,23,0.55); display: flex; align-items: center; justify-content: center; padding: 12px; }
    .modal { width: 100%; max-width: 680px; max-height: 92vh; background: #fff; border-radius: 16px; display: flex; flex-direction: column; overflow: hidden; box-shadow: 0 20px 50px rgba(0,0,0,0.3); }
    header { display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 14px 18px; border-bottom: 1px solid #f1f5f9; cursor: pointer; }
    .titulo { display: flex; align-items: center; gap: 10px; min-width: 0; }
    .icono { width: 34px; height: 34px; border-radius: 10px; background: #f5f3ff; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
    .icono mat-icon { color: #8b5cf6; font-size: 18px; width: 18px; height: 18px; }
    .min0 { min-width: 0; }
    .motivo { display: block; font-weight: 700; font-size: 0.82rem; color: #1e293b; }
    .meta { display: block; font-size: 0.68rem; color: #94a3b8; }
    .cerrar { border: none; background: transparent; color: #94a3b8; cursor: pointer; padding: 6px; border-radius: 8px; }
    .cerrar:hover { background: #f1f5f9; }
    .info { overflow-y: auto; max-height: 45vh; border-bottom: 1px solid #f1f5f9; padding: 12px 18px; }
    .alertas { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 10px; }
    .alerta { display: inline-flex; align-items: center; gap: 4px; padding: 2px 8px; border-radius: 999px; font-size: 0.62rem; font-weight: 700; }
    .alerta mat-icon { font-size: 12px; width: 12px; height: 12px; }
    .alerta.amber { background: #fffbeb; color: #92400e; }
    .alerta.blue { background: #eff6ff; color: #1d4ed8; }
    .alerta.rose { background: #fff1f2; color: #be123c; }
    .alerta.emerald { background: #ecfdf5; color: #047857; }
    .resumen { padding: 8px 0; border-bottom: 1px solid #f1f5f9; }
    .resumen p { margin: 0 0 6px; font-size: 0.72rem; color: #475569; line-height: 1.4; }
    .metricas { display: grid; grid-template-columns: repeat(auto-fit, minmax(130px, 1fr)); gap: 8px; padding: 10px 0; }
    .metrica { background: #f8fafc; border: 1px solid #f1f5f9; border-radius: 10px; padding: 8px 10px; }
    .m-label { margin: 0 0 4px; font-size: 0.58rem; font-weight: 700; color: #94a3b8; text-transform: uppercase; }
    .m-sub { margin: 4px 0 0; font-size: 0.62rem; color: #b45309; }
    .badge { display: inline-flex; padding: 2px 8px; border-radius: 999px; font-size: 0.65rem; font-weight: 700; background: #f1f5f9; color: #64748b; text-transform: capitalize; }
    .badge.res-resuelto, .badge.sent-positivo { background: #ecfdf5; color: #047857; }
    .badge.res-no_resuelto, .badge.sent-negativo { background: #fff1f2; color: #be123c; }
    .badge.res-escalado, .badge.sent-neutral, .badge.esf-medio { background: #fffbeb; color: #92400e; }
    .badge.esf-bajo { background: #ecfdf5; color: #047857; }
    .badge.esf-alto { background: #fff1f2; color: #be123c; }
    .fcr { display: inline-flex; align-items: center; gap: 4px; font-size: 0.68rem; color: #94a3b8; }
    .fcr mat-icon { font-size: 13px; width: 13px; height: 13px; }
    .fcr.si { color: #4f46e5; font-weight: 700; }
    .fcr-nota { display: flex; gap: 6px; font-size: 0.62rem; color: #94a3b8; margin: 6px 0 0; }
    .fcr-nota mat-icon { font-size: 13px; width: 13px; height: 13px; flex-shrink: 0; }
    .temas { display: flex; flex-wrap: wrap; gap: 4px; margin-top: 8px; }
    .tema { background: #f5f3ff; color: #7c3aed; font-size: 0.62rem; font-weight: 700; padding: 2px 8px; border-radius: 999px; }
    .razonamiento { margin-top: 10px; }
    .razonamiento > button { border: none; background: transparent; color: #64748b; font-size: 0.68rem; cursor: pointer; padding: 0; }
    .razonamiento p { margin-top: 6px; font-size: 0.68rem; color: #64748b; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 8px; line-height: 1.4; }
    .conversacion { flex: 1; overflow-y: auto; padding: 14px; display: flex; flex-direction: column; gap: 8px; background: #ECE5DD; }
    .cargando, .vacio { text-align: center; color: #64748b; font-size: 0.75rem; padding: 12px; }
    .spin { animation: spin 1s linear infinite; }
    @keyframes spin { to { transform: rotate(360deg); } }
    .burbuja-fila { display: flex; justify-content: flex-end; }
    .burbuja-fila.cliente { justify-content: flex-start; }
    .burbuja { max-width: 80%; padding: 8px 12px; border-radius: 14px; background: #10b981; color: #fff; border-bottom-right-radius: 4px; }
    .burbuja.cliente { background: #fff; color: #334155; border: 1px solid #e2e8f0; border-bottom-left-radius: 4px; border-bottom-right-radius: 14px; }
    .burbuja .nombre { margin: 0 0 2px; font-size: 0.62rem; font-weight: 700; opacity: 0.85; }
    .burbuja .texto { margin: 0; font-size: 0.8rem; white-space: pre-wrap; }
    .burbuja .fecha { display: block; text-align: right; font-size: 0.6rem; margin-top: 4px; opacity: 0.75; }
  `],
})
export class LiwaAnalisisDetalleComponent implements OnChanges {
  @Input({ required: true }) item!: LiwaAnalisisItem;
  @Output() close = new EventEmitter<void>();

  readonly FCR_DESCRIPCION = FCR_DESCRIPCION;
  readonly ABANDONADO_POR_LABELS = ABANDONADO_POR_LABELS;

  detalle: LiwaAnalisisDetalle | null = null;
  cargando = true;
  infoAbierta = false;
  mostrarRazonamiento = false;

  constructor(private readonly liwa: LiwaService) {}

  ngOnChanges(): void {
    this.cargando = true;
    this.detalle = null;
    this.liwa.obtenerAnalisisDetalle(this.item.id)
      .then((d) => { this.detalle = d; })
      .catch(() => { this.detalle = null; })
      .finally(() => { this.cargando = false; });
  }

  get turnos(): LiwaTurnoAnalizado[] {
    if (this.detalle?.conversacionTurnos?.length) return this.detalle.conversacionTurnos;
    if (!this.detalle?.conversacionCompleta) return [];
    return parsearHistorial(this.detalle.conversacionCompleta).map((m) => ({
      nombre: m.nombreAutor || '',
      fecha: m.fecha,
      mensaje: m.texto,
      autor: m.autor,
    }));
  }

  esCliente(t: LiwaTurnoAnalizado): boolean {
    return esTurnoCliente(t);
  }

  get resumenMotivo(): string { return this.detalle?.resumenMotivo || this.item.resumenMotivo || ''; }
  get resumenDesenlace(): string { return this.detalle?.resumenDesenlace || this.item.resumenDesenlace || ''; }
  get fcr(): boolean { return !!(this.detalle?.fcr ?? this.item.fcr); }
  get resultado() { return this.detalle?.resultado ?? this.item.resultado; }
  get abandono(): boolean { return !!(this.detalle?.abandono ?? this.item.abandono); }
  get abandonadoPor() { return this.detalle?.abandonadoPor ?? this.item.abandonadoPor; }
  get abandonadoInfo() { return this.abandonadoPor ? ABANDONADO_POR_LABELS[this.abandonadoPor] : null; }
  get sentimientoFinal() { return this.detalle?.sentimientoFinal || this.item.sentimientoFinal; }
  get esfuerzo() { return this.detalle?.esfuerzoCliente || this.item.esfuerzoCliente; }
  get temas(): string[] | undefined { return (this.detalle?.temas?.length ? this.detalle.temas : this.item.temas); }
  get banderas(): string[] { return this.detalle?.banderasCalidad ?? this.item.banderasCalidad ?? []; }
  get sinRespuesta(): boolean { return this.liwa.tieneBandera({ banderasCalidad: this.banderas }, 'sin_respuesta'); }
  get esperoDemasiado(): boolean { return this.liwa.tieneBandera({ banderasCalidad: this.banderas }, 'espero_demasiado'); }
  get tratoInadecuado(): boolean { return this.liwa.tieneBandera({ banderasCalidad: this.banderas }, 'trato_inadecuado'); }
  get gestionPendiente(): boolean { return this.liwa.tieneBandera({ banderasCalidad: this.banderas }, 'gestion_pendiente'); }
  get oportunidadVenta(): boolean { return !!(this.detalle?.oportunidadVenta ?? this.item.oportunidadVenta); }
  get municipio() { return this.detalle?.municipio || this.item.municipio; }
  get barrio() { return this.detalle?.barrio || this.item.barrio; }
  get razonamiento() { return this.detalle?.razonamiento ?? this.item.razonamiento; }
}
