import { Component, EventEmitter, HostListener, Input, Output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

// Envoltorio compartido para los modales centrados del modulo (antes cada
// panel -- dashboard-calidad, asesores-panel, case-reports-panel,
// liwa-summary-modal, analisis-ia-detalle -- repetia a mano el mismo overlay
// fijo + caja blanca + boton de cerrar, con pequeñas variaciones de estilo
// que iban divergiendo con cada copia). El contenido (header + cuerpo) sigue
// siendo responsabilidad de cada consumidor via <ng-content>; este
// componente solo aporta el overlay, la caja y el cierre (click afuera,
// boton, o Escape -- ninguna de las copias anteriores manejaba Escape).
//
// No cubre app-liwa-conversation-drawer: ese es un panel deslizante desde la
// derecha con su propio lenguaje visual (header verde, transform en vez de
// overlay+caja centrada), no una variante de este mismo patron.
@Component({
  selector: 'app-liwa-modal',
  standalone: true,
  imports: [MatIconModule],
  template: `
    <div class="overlay" (click)="cerrar.emit()">
      <div class="modal" [style.max-width]="maxWidth" (click)="$event.stopPropagation()">
        <button type="button" class="cerrar" (click)="cerrar.emit()" aria-label="Cerrar">
          <mat-icon>close</mat-icon>
        </button>
        <ng-content></ng-content>
      </div>
    </div>
  `,
  styles: [`
    .overlay {
      position: fixed; inset: 0; z-index: 100; background: rgba(2, 6, 23, 0.45);
      display: flex; align-items: center; justify-content: center; padding: 12px;
    }
    .modal {
      position: relative; width: 100%; max-height: 90vh; min-width: 0; display: flex; flex-direction: column;
      background: #fff; border-radius: 16px; overflow: hidden; box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.4);
    }
    .cerrar {
      position: absolute; top: 8px; right: 8px; z-index: 1;
      border: none; background: transparent; color: #94a3b8; cursor: pointer; padding: 6px; border-radius: 8px;
    }
    .cerrar:hover { background: #f1f5f9; color: #334155; }
  `],
})
export class LiwaModalComponent {
  @Input() maxWidth = '680px';
  @Output() cerrar = new EventEmitter<void>();

  @HostListener('document:keydown.escape')
  protected onEscape(): void {
    this.cerrar.emit();
  }
}
