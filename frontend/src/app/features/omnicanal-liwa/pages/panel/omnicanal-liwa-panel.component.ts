import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { LiwaService } from '../../data/liwa.service';
import { generarResumen } from '../../data/liwa-resumen.util';
import { LiwaChat, LiwaEstadisticas } from '../../models/liwa.model';
import { LiwaKpiCardComponent } from '../../components/kpi-card.component';
import { LiwaTableComponent } from '../../components/liwa-table.component';
import { LiwaConversationDrawerComponent } from '../../components/conversation-drawer.component';
import { LiwaActividadLineComponent } from '../../components/actividad-line-chart.component';
import { LiwaPendingPanelComponent } from '../../components/pending-panel.component';
import { LiwaSummaryModalComponent } from '../../components/liwa-summary-modal.component';
import { LiwaDashboardCalidadComponent } from '../../components/dashboard-calidad.component';
import { LiwaKpisCalidadPanelComponent } from '../../components/kpis-calidad-panel.component';
import { LiwaResumenAnalisisPanelComponent, LiwaAnalisisIaPanelComponent } from '../../components/analisis/analisis-ia-panel.component';
import { LiwaAdsPanelComponent } from '../../components/ads-panel.component';
import { LiwaAsesoresPanelComponent } from '../../components/asesores-panel.component';
import { LiwaCaseReportsPanelComponent } from '../../components/case-reports-panel.component';
import { LiwaConfigPanelComponent } from '../../components/config-panel.component';

type Vista = 'conversaciones' | 'analisis' | 'calidad' | 'indicadores' | 'asesores' | 'ads';

// Traducción de pages/reportes/liwa.tsx (guajiranet) al patrón Angular
// standalone de marca-blanca. Orquesta el selector de vistas y el filtro de
// fechas; delega cada vista en su propio componente.
//
// Estado de la traducción (ver resumen final entregado al usuario):
//  - "Conversaciones" (vista por defecto): traducida completa — KPIs,
//    gráfica de actividad, tabla, drawer de conversación.
//  - "Análisis y casos", "Gráficas", "KPIs", "Asesores", "Meta Ads": los
//    paneles React de origen (CaseReportsPanel, AnalisisIAPanel,
//    DashboardCalidad, IndicadoresCalidad, AsesoresPanel, AdsPanel) son
//    componentes grandes (10K-46KB cada uno) que NO se tradujeron todavía;
//    quedan como placeholders "pendiente" con el nombre del archivo origen.
//  - No se conectó socket en tiempo real (SocketContext/useLiwaEvento):
//    marca-blanca no tiene todavía un cliente de sockets equivalente en el
//    frontend Angular. El refresco automático cada 20s si se portó.
@Component({
  selector: 'app-omnicanal-liwa-panel',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatIconModule,
    LiwaKpiCardComponent,
    LiwaTableComponent,
    LiwaConversationDrawerComponent,
    LiwaActividadLineComponent,
    LiwaPendingPanelComponent,
    LiwaSummaryModalComponent,
    LiwaDashboardCalidadComponent,
    LiwaKpisCalidadPanelComponent,
    LiwaResumenAnalisisPanelComponent,
    LiwaAnalisisIaPanelComponent,
    LiwaAdsPanelComponent,
    LiwaAsesoresPanelComponent,
    LiwaCaseReportsPanelComponent,
    LiwaConfigPanelComponent,
  ],
  template: `
    <div class="liwa-page" *ngIf="verificandoConfig()">
      <div class="cargando-config"><mat-icon class="spin">progress_activity</mat-icon> Cargando módulo…</div>
    </div>

    <div class="liwa-page" *ngIf="!verificandoConfig() && !configurado()">
      <div class="sin-configurar">
        <mat-icon>settings_suggest</mat-icon>
        <h2>Configura el módulo antes de empezar</h2>
        <p>Todavía no tienes un token de Liwa configurado -- sin eso, el módulo no puede sincronizar conversaciones. Complétalo aquí para empezar a ver tus datos.</p>
      </div>
      <app-liwa-config-panel (guardado)="alConfigurar()" />
    </div>

    <div class="liwa-page" *ngIf="!verificandoConfig() && configurado()">
      <header class="liwa-header">
        <div class="titulo">
          <div class="icono-titulo"><mat-icon>forum</mat-icon></div>
          <div>
            <h1>Reporte Liwa</h1>
            <p>Conversaciones archivadas del bot de WhatsApp</p>
          </div>
        </div>

        <div class="filtros">
          <div class="presets">
            <button type="button" [class.activo]="presetActivo === 'hoy'" (click)="presetHoy()">Hoy</button>
            <button type="button" [class.activo]="presetActivo === 'ayer'" (click)="presetAyer()">Ayer</button>
            <button type="button" [class.activo]="presetActivo === '7dias'" (click)="preset7Dias()">Últimos 7 días</button>
            <button type="button" [class.activo]="presetActivo === '30dias'" (click)="preset30Dias()">Últimos 30 días</button>
          </div>
          <div class="rango">
            <label>
              <span>Desde</span>
              <input type="date" [(ngModel)]="desde" [max]="hasta || null" (change)="onFechaCambio()" />
            </label>
            <label>
              <span>Hasta</span>
              <input type="date" [(ngModel)]="hasta" [min]="desde || null" (change)="onFechaCambio()" />
            </label>
            <button type="button" class="limpiar" *ngIf="desde || hasta" (click)="limpiarFiltros()">
              Ver todo el historial
            </button>
            <span class="cargando" *ngIf="cargando">Actualizando…</span>
          </div>
        </div>
      </header>

      <div class="error" *ngIf="error">
        <mat-icon>warning</mat-icon>
        {{ error }}
      </div>

      <nav class="tabs">
        <button type="button" [class.activo]="vista === 'conversaciones'" (click)="vista = 'conversaciones'">Conversaciones</button>
        <button type="button" [class.activo]="vista === 'analisis'" (click)="vista = 'analisis'">Análisis y casos</button>
        <button type="button" [class.activo]="vista === 'calidad'" (click)="vista = 'calidad'">Gráficas</button>
        <button type="button" [class.activo]="vista === 'indicadores'" (click)="vista = 'indicadores'">KPIs</button>
        <button type="button" [class.activo]="vista === 'asesores'" (click)="vista = 'asesores'">Asesores</button>
        <button type="button" class="ads" [class.activo]="vista === 'ads'" (click)="vista = 'ads'">Meta Ads</button>
      </nav>

      <ng-container [ngSwitch]="vista">
        <div *ngSwitchCase="'calidad'">
          <app-liwa-dashboard-calidad [estadisticas]="estadisticas" [chats]="chats" [desde]="desde || undefined" [hasta]="hasta || undefined" [autoRefreshTick]="refrescoTick" />
        </div>
        <div *ngSwitchCase="'indicadores'">
          <app-liwa-kpis-calidad-panel [desde]="desde || undefined" [hasta]="hasta || undefined" [autoRefreshTick]="refrescoTick" />
        </div>
        <div *ngSwitchCase="'asesores'">
          <app-liwa-asesores-panel [desde]="desde || undefined" [hasta]="hasta || undefined" [autoRefreshTick]="refrescoTick" />
        </div>
        <div *ngSwitchCase="'ads'">
          <app-liwa-ads-panel [desde]="desde || undefined" [hasta]="hasta || undefined" [autoRefreshTick]="refrescoTick" />
        </div>
        <div *ngSwitchCase="'analisis'" class="analisis-stack">
          <app-liwa-resumen-analisis-panel [desde]="desde || undefined" [hasta]="hasta || undefined" [autoRefreshTick]="refrescoTick" />
          <app-liwa-case-reports-panel [desde]="desde || undefined" [hasta]="hasta || undefined" [autoRefreshTick]="refrescoTick" />
          <app-liwa-analisis-ia-panel [desde]="desde || undefined" [hasta]="hasta || undefined" [mostrarResumen]="false" [autoRefreshTick]="refrescoTick" />
        </div>
        <div *ngSwitchDefault>
          <div class="kpis">
            <app-liwa-kpi-card icon="chat" label="Total de conversaciones" [value]="totalEventos" color="linear-gradient(135deg,#3b82f6,#2563eb)" [clickable]="true" [onSelect]="verModalConversaciones" />
            <app-liwa-kpi-card icon="history" label="Total de mensajes" [value]="resumen.totalMensajes" color="linear-gradient(135deg,#f59e0b,#f97316)" />
          </div>

          <div class="actividad">
            <div class="actividad-header">
              <h2>Actividad por día</h2>
              <span>{{ resumen.totalMensajes }} mensajes</span>
            </div>
            <div class="actividad-chart">
              <app-liwa-actividad-line [data]="serieActividad" />
            </div>
          </div>

          <div class="tabla-tabla" [class.cargando-opacidad]="cargando">
            <app-liwa-table
              [chats]="chats"
              [contactosAnalizados]="contactosAnalizados"
              (seleccionado)="seleccionarChat($event.id)"
            />
          </div>

          <app-liwa-conversation-drawer [chat]="chatSeleccionado" (close)="seleccionarChat(null)" />

          <app-liwa-summary-modal
            *ngIf="modalResumen"
            [mode]="modalResumen"
            [chats]="chats"
            [contactosAnalizados]="contactosAnalizados"
            (close)="modalResumen = null"
            (seleccionado)="seleccionarChat($event.id)"
          />
        </div>
      </ng-container>
    </div>
  `,
  styles: [`
    .liwa-page { padding: 20px; display: flex; flex-direction: column; gap: 16px; background: #f8fafc; min-height: 100%; }

    .cargando-config { display: flex; align-items: center; gap: 8px; justify-content: center; padding: 60px; color: #64748b; font-size: 0.85rem; }
    .cargando-config .spin { animation: spin 1s linear infinite; }
    @keyframes spin { to { transform: rotate(360deg); } }

    .sin-configurar { display: flex; flex-direction: column; align-items: center; text-align: center; gap: 8px; max-width: 520px; margin: 0 auto; padding: 24px 0 4px; }
    .sin-configurar mat-icon { font-size: 40px; width: 40px; height: 40px; color: #f59e0b; }
    .sin-configurar h2 { margin: 0; font-size: 1.15rem; font-weight: 800; color: #0f172a; }
    .sin-configurar p { margin: 0; font-size: 0.82rem; color: #64748b; line-height: 1.5; }
    .liwa-header { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 12px; }
    .titulo { display: flex; align-items: center; gap: 12px; }
    .icono-titulo {
      width: 44px; height: 44px; border-radius: 14px; display: flex; align-items: center; justify-content: center;
      background: linear-gradient(135deg,#10b981,#059669); color: #fff; box-shadow: 0 6px 16px rgba(16,185,129,0.3);
    }
    .titulo h1 { margin: 0; font-size: 1.3rem; font-weight: 800; color: #0f172a; }
    .titulo p { margin: 0; font-size: 0.75rem; color: #64748b; }

    .filtros { display: flex; flex-direction: column; gap: 6px; }
    .presets { display: flex; gap: 6px; flex-wrap: wrap; }
    .presets button {
      border: 1px solid #e2e8f0; background: #fff; color: #475569; border-radius: 8px; padding: 4px 10px;
      font-size: 0.7rem; font-weight: 600; cursor: pointer;
    }
    .presets button.activo { background: #059669; border-color: #059669; color: #fff; }

    .rango { display: flex; align-items: flex-end; gap: 10px; background: #fff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 8px; flex-wrap: wrap; }
    .rango label { display: flex; flex-direction: column; gap: 2px; font-size: 0.65rem; color: #64748b; font-weight: 700; text-transform: uppercase; }
    .rango input {
      border: 1px solid #e2e8f0; background: #f8fafc; border-radius: 8px; padding: 6px 8px; font-size: 0.75rem;
    }
    .limpiar { border: none; background: #f1f5f9; color: #475569; border-radius: 8px; padding: 8px 14px; font-size: 0.72rem; font-weight: 600; cursor: pointer; }
    .cargando { display: inline-flex; align-items: center; font-size: 0.7rem; color: #059669; font-weight: 700; }

    .error {
      display: flex; align-items: center; gap: 8px; background: #fffbeb; border: 1px solid #fde68a; color: #92400e;
      font-size: 0.78rem; font-weight: 600; padding: 10px 14px; border-radius: 12px;
    }

    .tabs { display: flex; gap: 4px; flex-wrap: wrap; background: rgba(203,213,225,0.5); padding: 4px; border-radius: 12px; width: fit-content; }
    .tabs button {
      border: none; background: transparent; color: #64748b; font-size: 0.72rem; font-weight: 700;
      padding: 8px 16px; border-radius: 8px; cursor: pointer;
    }
    .tabs button.activo { background: #fff; color: #047857; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
    .tabs button.ads.activo { background: #2563eb; color: #fff; }

    .kpis { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 12px; }

    .actividad { background: #fff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 16px; }
    .actividad-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
    .actividad-header h2 { margin: 0; font-size: 0.85rem; color: #1e293b; }
    .actividad-header span { font-size: 0.65rem; color: #94a3b8; }
    .actividad-chart { height: 160px; }

    .tabla-tabla { transition: opacity 0.2s; }
    .cargando-opacidad { opacity: 0.6; }

    .analisis-stack { display: flex; flex-direction: column; gap: 12px; }
  `],
})
export class OmnicanalLiwaPanelComponent implements OnInit, OnDestroy {
  // Sin token de Liwa no hay nada que sincronizar -- en vez de mostrar el
  // dashboard vacio con un error de carga, se muestra directo la pantalla
  // de configuracion hasta que quede lista.
  verificandoConfig = signal(true);
  configurado = signal(false);

  vista: Vista = 'conversaciones';
  desde = this.fechaHaceNDias(30);
  hasta = '';

  chats: LiwaChat[] = [];
  estadisticas: LiwaEstadisticas | null = null;
  cargando = false;
  error: string | null = null;
  contactosAnalizados = new Set<string>();
  chatSeleccionadoId: string | null = null;
  modalResumen: 'conversaciones' | 'clientes' | null = null;
  // Se incrementa junto con el polling de abajo -- los paneles de las otras
  // pestañas (análisis, gráficas, KPIs, asesores, ads) lo reciben como
  // @Input y eso les dispara ngOnChanges, sin que el usuario tenga que
  // cambiar de pestaña para que se refresquen solos.
  refrescoTick = 0;

  // Arrow function (no pierde el "this") para pasarla como [onSelect] al
  // KpiCard — mismo patrón que el onClick de setModalResumen('conversaciones')
  // en el original.
  verModalConversaciones = (): void => {
    this.modalResumen = 'conversaciones';
  };

  private debounceCarga?: ReturnType<typeof setTimeout>;
  private debounceAnalisis?: ReturnType<typeof setTimeout>;
  private intervaloPolling?: ReturnType<typeof setInterval>;
  private peticionId = 0;

  constructor(private readonly liwa: LiwaService) {}

  ngOnInit(): void {
    void this.verificarConfigYArrancar();
  }

  private async verificarConfigYArrancar(): Promise<void> {
    try {
      const config = await this.liwa.obtenerConfig();
      this.configurado.set(config.liwaTokenConfigurado);
    } catch {
      this.configurado.set(false);
    } finally {
      this.verificandoConfig.set(false);
    }

    if (!this.configurado()) return;
    this.arrancarCargaDeDatos();
  }

  // Se llama cuando LiwaConfigPanelComponent avisa que el token ya quedo
  // configurado (Output "guardado") mientras se estaba en la vista de
  // "sin configurar" -- pasa a la vista normal sin recargar la pagina.
  alConfigurar(): void {
    this.configurado.set(true);
    this.arrancarCargaDeDatos();
  }

  private arrancarCargaDeDatos(): void {
    this.recargarConDebounce();
    this.recargarAnalisisConDebounce();
    // Respaldo por si el backend no emite ningún evento en tiempo real
    // (marca-blanca todavía no tiene socket equivalente conectado acá):
    // refresca cada 20s mientras la pestaña esté visible.
    this.intervaloPolling = setInterval(() => {
      if (document.visibilityState !== 'visible') return;
      void this.cargarDesdeBackend();
      this.refrescoTick++;
    }, 20000);
  }

  ngOnDestroy(): void {
    clearTimeout(this.debounceCarga);
    clearTimeout(this.debounceAnalisis);
    clearInterval(this.intervaloPolling);
  }

  private fechaHaceNDias(n: number): string {
    const d = new Date();
    d.setDate(d.getDate() - n);
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const dia = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${dia}`;
  }

  get presetActivo(): 'hoy' | 'ayer' | '7dias' | '30dias' | null {
    const hoy = this.fechaHaceNDias(0);
    if (this.desde === hoy && this.hasta === hoy) return 'hoy';
    if (this.desde === this.fechaHaceNDias(1) && this.hasta === this.fechaHaceNDias(1)) return 'ayer';
    if (this.desde === this.fechaHaceNDias(7) && this.hasta === hoy) return '7dias';
    if (this.desde === this.fechaHaceNDias(30) && this.hasta === hoy) return '30dias';
    return null;
  }

  private aplicarPreset(desde: string, hasta: string): void {
    this.desde = desde;
    this.hasta = hasta;
    this.onFechaCambio();
  }

  presetHoy(): void { this.aplicarPreset(this.fechaHaceNDias(0), this.fechaHaceNDias(0)); }
  presetAyer(): void { this.aplicarPreset(this.fechaHaceNDias(1), this.fechaHaceNDias(1)); }
  preset7Dias(): void { this.aplicarPreset(this.fechaHaceNDias(7), this.fechaHaceNDias(0)); }
  preset30Dias(): void { this.aplicarPreset(this.fechaHaceNDias(30), this.fechaHaceNDias(0)); }

  limpiarFiltros(): void {
    this.desde = '';
    this.hasta = '';
    this.onFechaCambio();
  }

  onFechaCambio(): void {
    this.recargarConDebounce();
    this.recargarAnalisisConDebounce();
  }

  private recargarConDebounce(): void {
    clearTimeout(this.debounceCarga);
    this.debounceCarga = setTimeout(() => void this.cargarDesdeBackend(), 400);
  }

  private recargarAnalisisConDebounce(): void {
    clearTimeout(this.debounceAnalisis);
    this.debounceAnalisis = setTimeout(() => void this.cargarContactosAnalizados(), 400);
  }

  private async cargarDesdeBackend(): Promise<void> {
    const idPeticion = ++this.peticionId;
    this.cargando = true;
    this.error = null;
    const desde = this.desde || undefined;
    const hasta = this.hasta || undefined;
    let fallaronChats = false;
    let fallaronEstadisticas = false;
    const [chats, estadisticas] = await Promise.all([
      this.liwa.obtenerConversacionesChat({ desde, hasta }).catch(() => { fallaronChats = true; return [] as LiwaChat[]; }),
      this.liwa.obtenerEstadisticas({ desde, hasta }).catch(() => { fallaronEstadisticas = true; return null; }),
    ]);
    // Si mientras esperábamos ya se disparó una petición más nueva (otro
    // cambio de filtro o el polling), esta quedó obsoleta.
    if (idPeticion !== this.peticionId) return;
    this.chats = chats;
    this.estadisticas = estadisticas;
    this.cargando = false;
    this.error = this.mensajeDeErrorCarga(fallaronChats, fallaronEstadisticas);
  }

  private mensajeDeErrorCarga(fallaronChats: boolean, fallaronEstadisticas: boolean): string | null {
    if (fallaronChats && fallaronEstadisticas) return 'No se pudo cargar el reporte. Intenta de nuevo.';
    if (fallaronChats) return 'No se pudieron cargar las conversaciones. Los KPIs de arriba pueden no reflejar la tabla.';
    if (fallaronEstadisticas) return 'No se pudo cargar el resumen agregado del backend.';
    return null;
  }

  private async cargarContactosAnalizados(): Promise<void> {
    try {
      const items = await this.liwa.obtenerTodosLosAnalisis({ desde: this.desde || undefined, hasta: this.hasta || undefined });
      this.contactosAnalizados = new Set(items.map((i) => i.idContacto).filter((id): id is string => !!id));
    } catch {
      this.contactosAnalizados = new Set();
    }
  }

  seleccionarChat(id: string | null): void {
    this.chatSeleccionadoId = id;
  }

  get chatSeleccionado(): LiwaChat | null {
    return this.chats.find((c) => c.id === this.chatSeleccionadoId) || null;
  }

  get resumen() {
    return generarResumen(this.chats);
  }

  get totalEventos(): number {
    return this.resumen.totalChats;
  }

  get serieActividad(): { hora: string; mensajes: number }[] {
    return this.resumen.actividadPorDia.map((p) => ({ hora: p.periodo, mensajes: p.total }));
  }
}
