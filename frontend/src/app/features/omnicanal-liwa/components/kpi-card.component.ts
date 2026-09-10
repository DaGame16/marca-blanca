import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

// Traducción de components/liwa/KpiCard.tsx.
@Component({
  selector: 'app-liwa-kpi-card',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  template: `
    <button
      type="button"
      class="kpi-card"
      [class.clickable]="!!clickable"
      [style.background]="color"
      (click)="onClick()"
    >
      <div class="kpi-icon"><mat-icon>{{ icon }}</mat-icon></div>
      <div class="kpi-body">
        <span class="kpi-value">{{ value }}</span>
        <span class="kpi-label">{{ label }}</span>
      </div>
    </button>
  `,
  styles: [`
    .kpi-card {
      display: flex;
      align-items: center;
      gap: 14px;
      border: none;
      border-radius: 16px;
      padding: 16px 18px;
      color: #fff;
      text-align: left;
      cursor: default;
      box-shadow: 0 4px 14px rgba(0,0,0,0.08);
    }
    .kpi-card.clickable { cursor: pointer; }
    .kpi-icon {
      width: 40px;
      height: 40px;
      border-radius: 12px;
      background: rgba(255,255,255,0.18);
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .kpi-body { display: flex; flex-direction: column; gap: 2px; }
    .kpi-value { font-size: 1.5rem; font-weight: 800; line-height: 1; }
    .kpi-label { font-size: 0.75rem; opacity: 0.9; font-weight: 600; }
  `],
})
export class LiwaKpiCardComponent {
  @Input() icon = 'insights';
  @Input() label = '';
  @Input() value: number | string = 0;
  @Input() color = 'linear-gradient(135deg, #3b82f6, #2563eb)';
  @Input() clickable = false;
  @Input() onSelect?: () => void;

  onClick(): void {
    if (this.clickable && this.onSelect) this.onSelect();
  }
}
