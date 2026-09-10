import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { LiwaAnalisisItem } from '../models/liwa.model';

// Traducción de ResumenAnalisisIA en components/liwa/AnalisisIA.tsx.
@Component({
  selector: 'app-liwa-resumen-analisis-ia',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  template: `
    <div class="grid" *ngIf="!cargando && items.length">
      <div class="card ok">
        <mat-icon>check_circle</mat-icon>
        <div>
          <p class="label">Resueltos</p>
          <p class="valor">{{ resueltos }} <span>de {{ items.length }}</span></p>
        </div>
      </div>
      <div class="card no">
        <mat-icon>cancel</mat-icon>
        <div>
          <p class="label">No resueltos</p>
          <p class="valor">{{ items.length - resueltos }} <span>de {{ items.length }}</span></p>
        </div>
      </div>
      <div class="card" [class]="peor.clase">
        <mat-icon>{{ peor.icono }}</mat-icon>
        <div>
          <p class="label">Sentimiento peor</p>
          <p class="valor">{{ peor.etiqueta }} <span>· {{ peor.n }}</span></p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 8px; }
    .card { display: flex; align-items: center; gap: 8px; border: 1px solid #f1f5f9; border-radius: 12px; padding: 10px 12px; }
    .card.ok { background: rgba(16,185,129,0.06); }
    .card.ok mat-icon { color: #10b981; }
    .card.no { background: rgba(244,63,94,0.06); }
    .card.no mat-icon { color: #f43f5e; }
    .card.sent-positivo { background: rgba(16,185,129,0.06); color: #047857; }
    .card.sent-neutral { background: rgba(245,158,11,0.06); color: #92400e; }
    .card.sent-negativo { background: rgba(244,63,94,0.06); color: #be123c; }
    .label { margin: 0; font-size: 0.6rem; font-weight: 700; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.03em; }
    .valor { margin: 0; font-size: 0.85rem; font-weight: 700; }
    .valor span { font-size: 0.68rem; font-weight: 500; color: #94a3b8; }
  `],
})
export class LiwaResumenAnalisisIaComponent {
  @Input() items: LiwaAnalisisItem[] = [];
  @Input() cargando = false;

  get resueltos(): number {
    return this.items.filter((a) => a.resultado === 'resuelto').length;
  }

  get peor(): { etiqueta: string; n: number; icono: string; clase: string } {
    const negativos = this.items.filter((a) => a.sentimientoFinal === 'negativo').length;
    const neutros = this.items.filter((a) => a.sentimientoFinal === 'neutral').length;
    if (negativos > 0) return { etiqueta: 'Negativo', n: negativos, icono: 'sentiment_dissatisfied', clase: 'sent-negativo' };
    if (neutros > 0) return { etiqueta: 'Neutral', n: neutros, icono: 'sentiment_neutral', clase: 'sent-neutral' };
    return { etiqueta: 'Positivo', n: this.items.length, icono: 'sentiment_satisfied', clase: 'sent-positivo' };
  }
}
