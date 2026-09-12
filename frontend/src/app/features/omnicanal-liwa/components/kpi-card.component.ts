import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

// Traducción fiel de components/liwa/KpiCard.tsx: tarjeta blanca con icono en
// cuadro de color (gradiente), valor grande y label uppercase. El original
// no le pone cursor-pointer ni feedback visual al hover aunque tenga
// onClick -- se respeta esa falta de affordance a propósito.
@Component({
  selector: 'app-liwa-kpi-card',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  template: `
    <div class="kpi-card" (click)="onClick()">
      <div class="kpi-icon" [style.background]="color">
        <mat-icon>{{ icon }}</mat-icon>
      </div>
      <div class="kpi-body">
        <p class="kpi-label">{{ label }}</p>
        <p class="kpi-value">{{ value }}</p>
        <p class="kpi-sub" *ngIf="sub">{{ sub }}</p>
      </div>
    </div>
  `,
  styles: [`
    .kpi-card {
      min-width: 0; background: #fff; border-radius: 16px; border: 1px solid #e2e8f0;
      box-shadow: 0 1px 2px rgba(0,0,0,0.05); padding: 16px; display: flex; align-items: center; gap: 12px;
    }
    .kpi-icon {
      width: 44px; height: 44px; border-radius: 12px; display: flex; align-items: center; justify-content: center;
      color: #fff; flex-shrink: 0;
    }
    .kpi-icon mat-icon { font-size: 20px; width: 20px; height: 20px; }
    .kpi-body { min-width: 0; }
    .kpi-label {
      margin: 0; font-size: 10px; font-weight: 700; color: #94a3b8; text-transform: uppercase;
      letter-spacing: 0.04em; line-height: 1.2; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
    }
    .kpi-value {
      margin: 0; font-size: 1.5rem; font-weight: 700; color: #0f172a; line-height: 1.2;
      font-variant-numeric: tabular-nums;
    }
    .kpi-sub {
      margin: 0; font-size: 10px; color: #94a3b8; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
    }
  `],
})
export class LiwaKpiCardComponent {
  @Input() icon = 'insights';
  @Input() label = '';
  @Input() value: number | string = 0;
  @Input() sub?: string;
  @Input() color = 'linear-gradient(135deg, #3b82f6, #2563eb)';
  @Input() clickable = false;
  @Input() onSelect?: () => void;

  onClick(): void {
    if (this.clickable && this.onSelect) this.onSelect();
  }
}
