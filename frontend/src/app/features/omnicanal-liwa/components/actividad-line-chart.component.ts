import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BaseChartDirective } from 'ng2-charts';
import { provideCharts, withDefaultRegisterables } from 'ng2-charts';
import { ChartConfiguration } from 'chart.js';

// Traducción de components/liwa/LiwaCharts.tsx -> ActividadLine. El original
// usa Chart.js (react-chartjs-2); acá se replica con ng2-charts manteniendo
// exactamente los mismos colores/props (línea suave azul con relleno
// translúcido, sin leyenda, grid solo en el eje Y).
//
// provideCharts() va en los `providers` del propio componente (no en
// app.config.ts) para que chart.js solo se cargue dentro del chunk lazy de
// este módulo, sin inflar el bundle inicial (ver angular.json budgets).
@Component({
  selector: 'app-liwa-actividad-line',
  standalone: true,
  imports: [CommonModule, BaseChartDirective],
  providers: [provideCharts(withDefaultRegisterables())],
  template: `
    <div class="contenedor">
      <canvas
        *ngIf="data.length; else vacio"
        baseChart
        [data]="chartData"
        [options]="chartOptions"
        type="line"
      ></canvas>
      <ng-template #vacio>
        <div class="sin-datos">Sin datos de actividad en este rango</div>
      </ng-template>
    </div>
  `,
  styles: [`
    .contenedor { position: relative; width: 100%; height: 100%; min-width: 0; overflow: hidden; }
    .sin-datos { display: flex; align-items: center; justify-content: center; height: 100%; color: #94a3b8; font-size: 0.8rem; }
  `],
})
export class LiwaActividadLineComponent {
  @Input() data: { hora: string; mensajes: number }[] = [];

  get chartData(): ChartConfiguration<'line'>['data'] {
    return {
      labels: this.data.map((d) => d.hora),
      datasets: [{
        data: this.data.map((d) => d.mensajes),
        label: 'Mensajes',
        borderColor: '#2563eb',
        backgroundColor: 'rgba(37,99,235,0.15)',
        fill: true,
        tension: 0.4,
        pointBackgroundColor: '#2563eb',
        pointBorderColor: '#ffffff',
        pointBorderWidth: 1.5,
        pointRadius: 3,
      }],
    };
  }

  readonly chartOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: {
        backgroundColor: '#ffffff',
        titleColor: '#0f172a',
        bodyColor: '#334155',
        borderColor: '#e2e8f0',
        borderWidth: 1,
        cornerRadius: 8,
        padding: 10,
      },
    },
    scales: {
      x: {
        grid: { display: false },
        ticks: { font: { size: 11 }, maxRotation: 0, autoSkip: true, maxTicksLimit: 15 },
      },
      y: {
        beginAtZero: true,
        grid: { color: 'rgba(100,116,139,0.12)' },
        ticks: { font: { size: 11 }, precision: 0 },
      },
    },
  };
}
