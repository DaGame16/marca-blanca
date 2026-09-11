import { ChangeDetectorRef, Component, Input, OnChanges, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { LiwaService } from '../data/liwa.service';
import { LiwaAnalisisItem } from '../models/liwa.model';

type Semaforo = 'bueno' | 'regular' | 'malo';
interface Kpi { icon: string; label: string; valor: string; sub?: string; semaforo?: Semaforo; }

function formatTiempo(ms: number | null): string {
  if (ms == null || !Number.isFinite(ms)) return '—';
  const minutos = Math.round(ms / 60000);
  if (minutos < 1) return '<1 min';
  if (minutos < 60) return `${minutos} min`;
  const h = Math.floor(minutos / 60);
  const m = minutos % 60;
  return `${h} h ${m.toString().padStart(2, '0')} min`;
}
function semaforoMasEsMejor(v: number): Semaforo {
  if (v >= 80) return 'bueno';
  if (v >= 50) return 'regular';
  return 'malo';
}
function semaforoMenosEsMejor(v: number): Semaforo {
  if (v <= 15) return 'bueno';
  if (v <= 40) return 'regular';
  return 'malo';
}

// Traducción de components/liwa/IndicadoresCalidad.tsx (KpisCalidadPanel).
@Component({
  selector: 'app-liwa-kpis-calidad-panel',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  template: `
    <section class="tarjeta">
      <header>
        <div>
          <h2>KPIs de calidad</h2>
          <p>CSAT, efectividad, eficiencia, esfuerzo y conversión — calculados del análisis IA dentro del rango de fechas.</p>
        </div>
        <span class="total">{{ nTotal }} conversaciones analizadas</span>
      </header>

      <p class="error" *ngIf="error">{{ error }}</p>

      <div class="cargando" *ngIf="cargando">
        <mat-icon class="spin">progress_activity</mat-icon> Calculando KPIs…
      </div>
      <p class="vacio" *ngIf="!cargando && nTotal === 0">Sin conversaciones analizadas en el periodo.</p>

      <div class="grid" *ngIf="!cargando && nTotal > 0">
        <div class="kpi" *ngFor="let k of kpis">
          <div class="kpi-icon" [class]="'sem-' + (k.semaforo || 'default')"><mat-icon>{{ k.icon }}</mat-icon></div>
          <div class="kpi-body">
            <p class="label">{{ k.label }}</p>
            <p class="valor">{{ k.valor }}</p>
            <p class="sub" *ngIf="k.sub">{{ k.sub }}</p>
          </div>
        </div>
      </div>

      <p class="nota">
        El color del icono refleja el estado (verde bueno · ámbar regular · rojo malo): "más es mejor" para CSAT,
        sentimiento, FCR, resolución, esfuerzo bajo y conversión; "menos es mejor" para recontacto, escalamiento y
        abandono. CSAT se infiere del sentimiento final (no hay micro-encuesta). Recontacto = clientes que vuelven por
        el mismo motivo dentro del periodo (idContacto + motivoContacto).
      </p>
    </section>
  `,
  styles: [`
    .tarjeta { background: #fff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 16px; }
    header { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 8px; margin-bottom: 16px; }
    header h2 { margin: 0; font-size: 1rem; font-weight: 700; color: #1e293b; }
    header p { margin: 2px 0 0; font-size: 0.75rem; color: #64748b; max-width: 560px; }
    .total { font-size: 0.72rem; color: #64748b; }
    .error { background: #fff1f2; color: #be123c; font-size: 0.72rem; padding: 8px 12px; border-radius: 8px; }
    .cargando { display: flex; align-items: center; gap: 8px; justify-content: center; padding: 40px; color: #94a3b8; font-size: 0.75rem; }
    .spin { animation: spin 1s linear infinite; }
    @keyframes spin { to { transform: rotate(360deg); } }
    .vacio { text-align: center; color: #94a3b8; padding: 40px; font-size: 0.85rem; }
    .grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 8px; }
    .kpi { display: flex; align-items: center; gap: 10px; background: #fff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 10px; }
    .kpi-icon { width: 32px; height: 32px; border-radius: 10px; display: flex; align-items: center; justify-content: center; color: #fff; background: #6366f1; flex-shrink: 0; }
    .kpi-icon mat-icon { font-size: 16px; width: 16px; height: 16px; }
    .kpi-icon.sem-bueno { background: #10b981; }
    .kpi-icon.sem-regular { background: #f59e0b; }
    .kpi-icon.sem-malo { background: #f43f5e; }
    .kpi-body { min-width: 0; }
    .kpi-body .label { margin: 0; font-size: 0.6rem; font-weight: 700; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.03em; }
    .kpi-body .valor { margin: 0; font-size: 0.95rem; font-weight: 800; color: #0f172a; }
    .kpi-body .sub { margin: 0; font-size: 0.6rem; color: #94a3b8; }
    .nota { margin: 16px 0 0; font-size: 0.65rem; color: #94a3b8; line-height: 1.4; }
  `],
})
export class LiwaKpisCalidadPanelComponent implements OnChanges, OnDestroy {
  @Input() desde?: string;
  @Input() hasta?: string;
  // Ver comentario en ads-panel.component.ts.
  @Input() autoRefreshTick?: number;

  analisis: LiwaAnalisisItem[] = [];
  cargando = true;
  error: string | null = null;
  private peticion = 0;

  constructor(private readonly liwa: LiwaService, private readonly cdr: ChangeDetectorRef) {}

  ngOnChanges(): void {
    void this.cargar();
  }

  ngOnDestroy(): void {
    this.peticion++;
  }

  private async cargar(): Promise<void> {
    const id = ++this.peticion;
    this.cargando = true;
    this.error = null;
    try {
      const respuesta = await this.liwa.obtenerTodosLosAnalisis({ desde: this.desde, hasta: this.hasta });
      if (id !== this.peticion) return;
      this.analisis = Array.isArray(respuesta) ? respuesta : [];
    } catch {
      if (id === this.peticion) this.error = 'No fue posible cargar las conversaciones analizadas por IA.';
    } finally {
      if (id === this.peticion) {
        this.cargando = false;
        // Ver comentario en ads-panel.component.ts.
        this.cdr.markForCheck();
      }
    }
  }

  private get visibles(): LiwaAnalisisItem[] {
    return this.analisis.filter((a) => this.liwa.analisisEnRango(a, this.desde, this.hasta));
  }

  get nTotal(): number {
    return this.visibles.length;
  }

  get kpis(): Kpi[] {
    const base = this.visibles;
    const nTotal = base.length;
    const pct = (n: number) => (nTotal > 0 ? Math.round((n / nTotal) * 100) : 0);
    const contar = (fn: (a: LiwaAnalisisItem) => boolean) => base.filter(fn).length;

    const positivos = contar((a) => a.sentimientoFinal === 'positivo');
    const neutros = contar((a) => a.sentimientoFinal === 'neutral');
    const negativos = contar((a) => a.sentimientoFinal === 'negativo');
    const resueltos = contar((a) => a.resultado === 'resuelto');
    const escalados = contar((a) => a.resultado === 'escalado');
    const abandonados = contar((a) => a.abandono === true);
    const fcrOk = contar((a) => a.fcr === true);
    const esfuerzoBajo = contar((a) => a.esfuerzoCliente === 'bajo');
    const esfuerzoMedio = contar((a) => a.esfuerzoCliente === 'medio');
    const esfuerzoAlto = contar((a) => a.esfuerzoCliente === 'alto');
    const conVenta = contar((a) => a.oportunidadVenta === true);
    const ventasCerradas = contar((a) => a.oportunidadVenta === true && a.ventaConfirmadaEnTexto === true);

    const pares = new Map<string, number>();
    base.forEach((a) => {
      if (!a.idContacto || !a.motivoContacto) return;
      const clave = `${a.idContacto}|${a.motivoContacto}`;
      pares.set(clave, (pares.get(clave) || 0) + 1);
    });
    const recontacto = Array.from(pares.values()).filter((n) => n > 1).length;

    const promTiempo = (diff: (a: LiwaAnalisisItem) => number | null) => {
      const valores: number[] = [];
      base.forEach((a) => {
        const v = diff(a);
        if (v != null && Number.isFinite(v)) valores.push(v);
      });
      if (valores.length === 0) return null;
      return Math.round(valores.reduce((s, x) => s + x, 0) / valores.length);
    };
    const tiempPrimeraResp = promTiempo((a) =>
      a.tsPrimerMensaje && a.tsPrimeraRespuesta
        ? new Date(a.tsPrimeraRespuesta).getTime() - new Date(a.tsPrimerMensaje).getTime()
        : null,
    );
    const tiempResolucion = promTiempo((a) =>
      a.tsPrimerMensaje && a.tsCierre
        ? new Date(a.tsCierre).getTime() - new Date(a.tsPrimerMensaje).getTime()
        : null,
    );

    return [
      { icon: 'sentiment_satisfied', label: 'CSAT · satisfacción', valor: `${pct(positivos)}%`, sub: `${positivos} de ${nTotal} · inferido del sentimiento`, semaforo: semaforoMasEsMejor(pct(positivos)) },
      { icon: 'auto_awesome', label: 'Sentimiento', valor: `${pct(positivos)}%`, sub: `neutral ${pct(neutros)}% · negativo ${pct(negativos)}%`, semaforo: semaforoMasEsMejor(pct(positivos)) },
      { icon: 'check_circle', label: 'FCR · 1er contacto', valor: `${pct(fcrOk)}%`, sub: 'resueltas sin que el cliente vuelva', semaforo: semaforoMasEsMejor(pct(fcrOk)) },
      { icon: 'thumb_up', label: 'Tasa de resolución', valor: `${pct(resueltos)}%`, sub: 'casos que terminaron resueltos', semaforo: semaforoMasEsMejor(pct(resueltos)) },
      { icon: 'repeat', label: 'Tasa de recontacto', valor: `${pct(recontacto)}%`, sub: 'reabren el mismo motivo (falso resuelto)', semaforo: semaforoMenosEsMejor(pct(recontacto)) },
      { icon: 'timer', label: 'Tiempo a 1ra respuesta', valor: formatTiempo(tiempPrimeraResp), sub: 'promedio hasta la 1ra respuesta' },
      { icon: 'schedule', label: 'Resolución media', valor: formatTiempo(tiempResolucion), sub: 'promedio hasta el cierre' },
      { icon: 'arrow_outward', label: 'Tasa de escalamiento', valor: `${pct(escalados)}%`, sub: 'transferidas a humano / otra área', semaforo: semaforoMenosEsMejor(pct(escalados)) },
      { icon: 'block', label: 'Tasa de abandono', valor: `${pct(abandonados)}%`, sub: 'clientes que se van sin cerrar', semaforo: semaforoMenosEsMejor(pct(abandonados)) },
      { icon: 'speed', label: 'CES · esfuerzo bajo', valor: `${pct(esfuerzoBajo)}%`, sub: `medio ${pct(esfuerzoMedio)}% · alto ${pct(esfuerzoAlto)}%`, semaforo: semaforoMasEsMejor(pct(esfuerzoBajo)) },
      { icon: 'track_changes', label: 'Tasa de conversión', valor: `${conVenta > 0 ? pct(ventasCerradas) : 0}%`, sub: `${ventasCerradas} de ${conVenta} chats con oportunidad`, semaforo: semaforoMasEsMejor(conVenta > 0 ? pct(ventasCerradas) : 0) },
    ];
  }
}
