import { Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

// Placeholder minimo -- el modulo de omnicanal (chat/WhatsApp) todavia no
// tiene UI real construida del lado del cliente. Vive dentro del Shell,
// distinto de OmnicanalDetalleComponent (la pagina publica de "conocer la
// solucion" enlazada desde el home antes de comprar).
@Component({
  selector: 'app-omnicanal-panel',
  standalone: true,
  imports: [MatIconModule],
  template: `
    <div class="placeholder">
      <mat-icon class="placeholder-icono">support_agent</mat-icon>
      <p>Soy omnicanal</p>
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
      color: #7c3aed;
    }

    .placeholder p {
      font-size: 1.1rem;
      font-weight: 600;
      margin: 0;
    }
  `],
})
export class OmnicanalPanelComponent {}
