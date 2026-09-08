import { Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

// Placeholder minimo -- el modulo de PBX 3CX (integracion telefonica)
// todavia no tiene UI real construida del lado del cliente. Vive dentro del
// Shell, distinto de Pbx3cxDetalleComponent (la pagina publica de "conocer
// la solucion" enlazada desde el home antes de comprar).
@Component({
  selector: 'app-pbx-3cx-panel',
  standalone: true,
  imports: [MatIconModule],
  template: `
    <div class="placeholder">
      <mat-icon class="placeholder-icono">call</mat-icon>
      <p>Soy PBX</p>
    </div>
  `,
  styles: [`
    .placeholder {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 12px;
      padding: 120px 24px;
      color: #64748b;
    }

    .placeholder-icono {
      font-size: 48px;
      width: 48px;
      height: 48px;
      color: #2563eb;
    }

    .placeholder p {
      font-size: 1.1rem;
      font-weight: 600;
      margin: 0;
    }
  `],
})
export class Pbx3cxPanelComponent {}
