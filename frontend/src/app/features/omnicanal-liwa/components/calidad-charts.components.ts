import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BaseChartDirective, provideCharts, withDefaultRegisterables } from 'ng2-charts';
import { ChartConfiguration } from 'chart.js';
import { CHARTJS_TOOLTIP } from '../shared/liwa-paleta';

// Traducción SIMPLIFICADA de components/liwa/CalidadCharts.tsx. El original
// usa Recharts (barras, donut, dispersión de burbujas, mapa de calor). Acá
// se reimplementan como widgets propios en HTML/CSS/SVG livianos — misma
// lógica de datos (ya portada en DashboardCalidadComponent), sin agregar
// una librería de gráficas nueva al proyecto Angular. Si el proyecto
// adopta una librería de charts más adelante, esto se puede reemplazar
// widget por widget sin tocar quien los usa (mismos @Input).

export interface KpiCalidad {
  label: string;
  valor: string;
  sub: string;
  icon: string;
  onClick?: () => void;
}

@Component({
  selector: 'app-liwa-kpi-grid-calidad',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="grid">
      <button type="button" class="kpi" *ngFor="let k of kpis" [class.clic]="!!k.onClick" (click)="k.onClick && k.onClick()">
        <span class="barra"></span>
        <p class="label">{{ k.label }}</p>
        <p class="valor">{{ k.valor }}</p>
        <p class="sub">{{ k.sub }}</p>
      </button>
    </div>
  `,
  styles: [`
    .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 10px; }
    .kpi { position: relative; text-align: left; background: #fff; border: 1px solid #e2e8f0; border-radius: 14px; padding: 14px; overflow: hidden; cursor: default; }
    .kpi.clic { cursor: pointer; }
    .kpi.clic:hover { box-shadow: 0 6px 16px rgba(0,0,0,0.08); }
    .barra { position: absolute; top: 0; left: 0; right: 0; height: 3px; background: #6366f1; }
    .label { margin: 0; font-size: 0.65rem; font-weight: 700; color: #94a3b8; text-transform: uppercase; }
    .valor { margin: 6px 0 0; font-size: 1.3rem; font-weight: 800; color: #0f172a; }
    .sub { margin: 4px 0 0; font-size: 0.68rem; color: #64748b; }
  `],
})
export class LiwaKpiGridCalidadComponent {
  @Input() kpis: KpiCalidad[] = [];
}

@Component({
  selector: 'app-liwa-barras-categorias',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="barras" *ngIf="ordenadas.length; else vacio">
      <div class="fila" *ngFor="let d of ordenadas; let i = index">
        <span class="etiqueta">{{ d.etiqueta }}</span>
        <div class="track">
          <div class="valor" [style.width.%]="(d.total / max) * 100" [style.background]="color(i)"></div>
        </div>
        <span class="total">{{ d.total }}</span>
      </div>
    </div>
    <ng-template #vacio><p class="sin-datos">Sin datos.</p></ng-template>
  `,
  styles: [`
    .barras { display: flex; flex-direction: column; gap: 8px; }
    .fila { display: grid; grid-template-columns: 110px 1fr 32px; align-items: center; gap: 8px; }
    .etiqueta { font-size: 0.72rem; color: #475569; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .track { background: #f1f5f9; border-radius: 6px; height: 14px; overflow: hidden; }
    .valor { height: 100%; border-radius: 6px; }
    .total { font-size: 0.7rem; font-weight: 700; color: #334155; text-align: right; }
    .sin-datos { text-align: center; color: #94a3b8; font-size: 0.78rem; padding: 24px; }
  `],
})
export class LiwaBarrasCategoriasComponent {
  @Input() data: { etiqueta: string; total: number }[] = [];
  private readonly paleta = ['#f59e0b', '#3b82f6', '#8b5cf6', '#10b981', '#ef4444', '#0ea5e9', '#64748b', '#ec4899'];

  get ordenadas() {
    return [...this.data].sort((a, b) => b.total - a.total);
  }

  get max(): number {
    return Math.max(1, ...this.ordenadas.map((d) => d.total));
  }

  color(i: number): string {
    return this.paleta[i % this.paleta.length];
  }
}

@Component({
  selector: 'app-liwa-barras-area',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="barras-v" *ngIf="data.length; else vacio">
      <div class="col" *ngFor="let d of data">
        <span class="valor">{{ d.total }}</span>
        <div class="track" [style.height.%]="(d.total / max) * 100"></div>
        <span class="etiqueta">{{ d.area }}</span>
      </div>
    </div>
    <ng-template #vacio><p class="sin-datos">Sin datos.</p></ng-template>
  `,
  styles: [`
    .barras-v { display: flex; align-items: flex-end; gap: 10px; height: 200px; padding-top: 20px; }
    .col { display: flex; flex-direction: column; align-items: center; flex: 1; height: 100%; justify-content: flex-end; }
    .valor { font-size: 0.68rem; font-weight: 700; color: #334155; margin-bottom: 4px; }
    .track { width: 60%; background: #0ea5e9; border-radius: 6px 6px 0 0; min-height: 2px; }
    .etiqueta { font-size: 0.6rem; color: #64748b; margin-top: 6px; text-align: center; }
    .sin-datos { text-align: center; color: #94a3b8; font-size: 0.78rem; padding: 24px; }
  `],
})
export class LiwaBarrasAreaComponent {
  @Input() data: { area: string; total: number }[] = [];

  get max(): number {
    return Math.max(1, ...this.data.map((d) => d.total));
  }
}

@Component({
  selector: 'app-liwa-anillo-sentimiento',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div *ngIf="data.length; else vacio">
      <div class="donut-wrap">
        <div class="donut" [style.background]="gradiente"></div>
        <div class="centro">
          <p class="n">{{ total }}</p>
          <p>conversaciones</p>
        </div>
      </div>
      <div class="leyenda">
        <div class="item" *ngFor="let d of data">
          <span class="punto" [style.background]="d.color"></span>
          <div>
            <p class="etq">{{ d.etiqueta }} <span>{{ d.valor }} · {{ pct(d.valor) }}%</span></p>
          </div>
        </div>
      </div>
    </div>
    <ng-template #vacio><p class="sin-datos">Distribución de sentimiento pendiente de análisis IA.</p></ng-template>
  `,
  styles: [`
    .donut-wrap { position: relative; width: 168px; height: 168px; margin: 0 auto; }
    .donut { width: 100%; height: 100%; border-radius: 50%; }
    .centro { position: absolute; inset: 28px; background: #fff; border-radius: 50%; display: flex; flex-direction: column; align-items: center; justify-content: center; }
    .centro .n { margin: 0; font-size: 1.4rem; font-weight: 800; color: #0f172a; }
    .centro p { margin: 0; font-size: 0.6rem; color: #94a3b8; }
    .leyenda { margin-top: 14px; display: flex; flex-direction: column; gap: 8px; }
    .item { display: flex; align-items: center; gap: 8px; }
    .punto { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }
    .etq { margin: 0; font-size: 0.75rem; font-weight: 600; color: #334155; }
    .etq span { color: #94a3b8; font-weight: 500; margin-left: 6px; }
    .sin-datos { text-align: center; color: #94a3b8; font-size: 0.78rem; padding: 24px; }
  `],
})
export class LiwaAnilloSentimientoComponent {
  @Input() data: { etiqueta: string; valor: number; color: string }[] = [];
  @Input() total = 0;

  pct(valor: number): number {
    return this.total > 0 ? Math.round((valor / this.total) * 100) : 0;
  }

  get gradiente(): string {
    let acumulado = 0;
    const total = this.data.reduce((s, d) => s + d.valor, 0) || 1;
    const partes = this.data.map((d) => {
      const desde = (acumulado / total) * 360;
      acumulado += d.valor;
      const hasta = (acumulado / total) * 360;
      return `${d.color} ${desde}deg ${hasta}deg`;
    });
    return `conic-gradient(${partes.join(', ')})`;
  }
}

@Component({
  selector: 'app-liwa-mapa-calor',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="tabla-wrap" *ngIf="filas.length && columnas.length; else vacio">
      <table>
        <thead>
          <tr>
            <th>Motivo</th>
            <th *ngFor="let c of columnas">{{ c }}</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let f of filas">
            <td class="fila-nombre">{{ f }}</td>
            <td *ngFor="let c of columnas" [style.background]="fondo(f, c)">{{ valor(f, c) || '' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
    <ng-template #vacio><p class="sin-datos">Sin conversaciones analizadas con municipio en el periodo.</p></ng-template>
  `,
  styles: [`
    .tabla-wrap { overflow-x: auto; }
    table { border-collapse: separate; border-spacing: 3px; width: 100%; font-size: 0.68rem; }
    th { font-weight: 700; color: #94a3b8; text-transform: uppercase; font-size: 0.58rem; padding: 4px 6px; text-align: center; }
    th:first-child { text-align: left; }
    .fila-nombre { font-weight: 700; color: #475569; white-space: nowrap; padding-right: 8px; }
    td { text-align: center; font-weight: 700; color: #334155; border-radius: 6px; padding: 8px 6px; }
    .sin-datos { text-align: center; color: #94a3b8; font-size: 0.78rem; padding: 24px; }
  `],
})
export class LiwaMapaCalorComponent {
  @Input() filas: string[] = [];
  @Input() columnas: string[] = [];
  @Input() valores: Record<string, number> = {};
  @Input() max = 0;

  valor(f: string, c: string): number {
    return this.valores[`${f}|${c}`] || 0;
  }

  fondo(f: string, c: string): string {
    const v = this.valor(f, c);
    const intensidad = this.max === 0 ? 0 : v / this.max;
    return `rgba(139,92,246,${0.06 + intensidad * 0.7})`;
  }
}

// Traducción fiel de BurbujasDispersion (Recharts ScatterChart) -- CSAT en
// X, FCR en Y, tamaño de burbuja = volumen. Antes era una tabla (CSAT/FCR/
// Volumen en columnas); esto sí es la dispersión real del original.
@Component({
  selector: 'app-liwa-burbujas-dispersion',
  standalone: true,
  imports: [CommonModule, BaseChartDirective],
  providers: [provideCharts(withDefaultRegisterables())],
  template: `
    <div class="contenedor" *ngIf="data.length; else vacio">
      <canvas baseChart type="bubble" [data]="chartData" [options]="chartOptions"></canvas>
    </div>
    <ng-template #vacio><p class="sin-datos">Satisfacción vs. efectividad pendiente de análisis IA.</p></ng-template>
  `,
  styles: [`
    .contenedor { position: relative; height: 260px; }
    .sin-datos { text-align: center; color: #94a3b8; font-size: 0.78rem; padding: 24px; }
  `],
})
export class LiwaBurbujasDispersionComponent {
  @Input() data: { motivo: string; csat: number; fcr: number; volumen: number }[] = [];

  get chartData(): ChartConfiguration<'bubble'>['data'] {
    const maxVol = Math.max(1, ...this.data.map((d) => d.volumen));
    return {
      datasets: [{
        label: 'Motivos',
        data: this.data.map((d) => ({ x: d.csat, y: d.fcr, r: 6 + (d.volumen / maxVol) * 24 })),
        backgroundColor: 'rgba(139,92,246,0.55)',
        borderColor: '#8b5cf6',
      }],
    };
  }

  get chartOptions(): ChartConfiguration<'bubble'>['options'] {
    const datos = this.data;
    return {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          ...CHARTJS_TOOLTIP,
          callbacks: {
            label: (ctx) => {
              const d = datos[ctx.dataIndex];
              return `${d.motivo}: CSAT ${d.csat}% · FCR ${d.fcr}% · ${d.volumen} casos`;
            },
          },
        },
      },
      scales: {
        x: { min: 0, max: 100, title: { display: true, text: 'CSAT (%)', font: { size: 11 } }, ticks: { font: { size: 10 } } },
        y: { min: 0, max: 100, title: { display: true, text: 'FCR (%)', font: { size: 11 } }, ticks: { font: { size: 10 } } },
      },
    };
  }
}

// Traducción fiel de LineaTendencia (Recharts LineChart) -- serie principal
// "total" (color prop) + una línea por zona (series). Antes era una lista de
// barras horizontales por día (sin desglose por municipio); esto sí es el
// gráfico de líneas multi-serie del original.
@Component({
  selector: 'app-liwa-linea-tendencia',
  standalone: true,
  imports: [CommonModule, BaseChartDirective],
  providers: [provideCharts(withDefaultRegisterables())],
  template: `
    <div class="contenedor" *ngIf="data.length; else vacio">
      <canvas baseChart type="line" [data]="chartData" [options]="chartOptions"></canvas>
    </div>
    <ng-template #vacio><p class="sin-datos">Sin mensajes en el periodo.</p></ng-template>
  `,
  styles: [`
    .contenedor { position: relative; height: 240px; }
    .sin-datos { text-align: center; color: #94a3b8; font-size: 0.78rem; padding: 24px; }
  `],
})
export class LiwaLineaTendenciaComponent {
  @Input() data: Record<string, string | number>[] = [];
  @Input() color = '#2563eb';
  @Input() series?: { clave: string; color: string }[];

  num(v: string | number): number {
    return Number(v) || 0;
  }

  get chartData(): ChartConfiguration<'line'>['data'] {
    const principal = {
      label: 'Total',
      data: this.data.map((f) => this.num(f['total'])),
      borderColor: this.color,
      backgroundColor: this.color,
      borderWidth: 2.5,
      pointRadius: 3,
      tension: 0.3,
    };
    const zonas = (this.series ?? []).map((s) => ({
      label: s.clave,
      data: this.data.map((f) => this.num(f[s.clave])),
      borderColor: s.color,
      backgroundColor: s.color,
      borderWidth: 1.8,
      pointRadius: 0,
      tension: 0.3,
    }));
    return { labels: this.data.map((f) => String(f['periodo'])), datasets: [principal, ...zonas] };
  }

  readonly chartOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { position: 'bottom', labels: { boxWidth: 10, font: { size: 10 } } },
      tooltip: CHARTJS_TOOLTIP,
    },
    scales: {
      x: { grid: { display: false }, ticks: { font: { size: 10 }, maxRotation: 0, autoSkip: true, maxTicksLimit: 12 } },
      y: { beginAtZero: true, grid: { color: 'rgba(100,116,139,0.12)' }, ticks: { font: { size: 10 }, precision: 0 } },
    },
  };
}
