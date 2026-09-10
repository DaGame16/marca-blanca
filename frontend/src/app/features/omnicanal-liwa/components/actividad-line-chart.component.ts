import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

// Traducción simplificada de components/liwa/LiwaCharts.tsx (ActividadLine).
// El original usaba Recharts (dependencia de React); acá se dibuja un
// gráfico de barras liviano en SVG inline, sin agregar dependencias nuevas
// al proyecto Angular. Si más adelante se instala una librería de gráficas
// para el resto del proyecto, conviene reemplazar esto por esa misma.
@Component({
  selector: 'app-liwa-actividad-line',
  standalone: true,
  imports: [CommonModule],
  template: `
    <svg *ngIf="data.length; else vacio" [attr.viewBox]="'0 0 ' + width + ' ' + height" class="chart" preserveAspectRatio="none">
      <polyline
        [attr.points]="puntos"
        fill="none"
        stroke="#10b981"
        stroke-width="2"
      />
      <circle *ngFor="let p of coords" [attr.cx]="p.x" [attr.cy]="p.y" r="2.5" fill="#059669" />
    </svg>
    <ng-template #vacio>
      <div class="sin-datos">Sin datos de actividad en este rango</div>
    </ng-template>
    <div class="ejes" *ngIf="data.length">
      <span *ngFor="let p of data">{{ p.hora }}</span>
    </div>
  `,
  styles: [`
    .chart { width: 100%; height: 100%; display: block; }
    .sin-datos { display: flex; align-items: center; justify-content: center; height: 100%; color: #94a3b8; font-size: 0.8rem; }
    .ejes { display: flex; justify-content: space-between; font-size: 10px; color: #94a3b8; margin-top: 4px; }
  `],
})
export class LiwaActividadLineComponent {
  @Input() data: { hora: string; mensajes: number }[] = [];

  readonly width = 600;
  readonly height = 140;

  get coords(): { x: number; y: number }[] {
    if (!this.data.length) return [];
    const max = Math.max(1, ...this.data.map((d) => d.mensajes));
    const paso = this.data.length > 1 ? this.width / (this.data.length - 1) : 0;
    return this.data.map((d, i) => ({
      x: this.data.length > 1 ? i * paso : this.width / 2,
      y: this.height - (d.mensajes / max) * (this.height - 10) - 5,
    }));
  }

  get puntos(): string {
    return this.coords.map((p) => `${p.x},${p.y}`).join(' ');
  }
}
