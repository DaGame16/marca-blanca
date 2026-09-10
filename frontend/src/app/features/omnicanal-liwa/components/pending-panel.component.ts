import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

// Placeholder para las pestañas de omnicanal-liwa que todavía NO se
// tradujeron de React a Angular (ver resumen al final de la tarea de
// refactor). Deja claro en la propia UI qué falta y de dónde viene, para
// que quien retome esto sepa exactamente qué archivo de guajiranet portar.
@Component({
  selector: 'app-liwa-pending-panel',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  template: `
    <div class="pendiente">
      <mat-icon>construction</mat-icon>
      <h3>{{ titulo }}</h3>
      <p>{{ descripcion }}</p>
      <code>guajiranet: {{ archivoOrigen }}</code>
    </div>
  `,
  styles: [`
    .pendiente {
      display: flex; flex-direction: column; align-items: center; text-align: center;
      gap: 8px; padding: 48px 24px; background: #fff; border: 1px dashed #cbd5e1;
      border-radius: 16px; color: #64748b;
    }
    .pendiente mat-icon { font-size: 32px; width: 32px; height: 32px; color: #f59e0b; }
    .pendiente h3 { margin: 0; color: #0f172a; font-size: 1rem; }
    .pendiente p { margin: 0; max-width: 440px; font-size: 0.85rem; }
    .pendiente code { font-size: 0.72rem; background: #f1f5f9; padding: 4px 10px; border-radius: 8px; color: #475569; }
  `],
})
export class LiwaPendingPanelComponent {
  @Input() titulo = '';
  @Input() descripcion = '';
  @Input() archivoOrigen = '';
}
