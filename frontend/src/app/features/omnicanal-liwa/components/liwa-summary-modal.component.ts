import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { LiwaChat } from '../models/liwa.model';
import { LiwaTableComponent } from './liwa-table.component';
import { LiwaModalComponent } from '../shared/liwa-modal.component';

type ModalMode = 'conversaciones' | 'clientes';

// Traducción fiel de components/liwa/LiwaSummaryModal.tsx — se abre al hacer
// clic en el KPI "Total de conversaciones". El overlay/caja/boton de cerrar
// los aporta app-liwa-modal (ver shared/liwa-modal.component.ts); aca solo
// queda el contenido propio (titulo + tabla o grilla de clientes).
@Component({
  selector: 'app-liwa-summary-modal',
  standalone: true,
  imports: [CommonModule, MatIconModule, LiwaTableComponent, LiwaModalComponent],
  template: `
    <app-liwa-modal maxWidth="1024px" (cerrar)="cerrar.emit()">
      <header>
        <div class="titulo">
          <mat-icon>{{ mode === 'clientes' ? 'group' : 'chat' }}</mat-icon>
          <div>
            <h2>{{ mode === 'clientes' ? 'Clientes distintos' : 'Conversaciones de WhatsApp' }}</h2>
            <p>{{ mode === 'clientes' ? (clientes.length + ' clientes') : (chats.length + ' conversaciones') }}</p>
          </div>
        </div>
      </header>
      <div class="cuerpo">
        <div class="clientes-grid" *ngIf="mode === 'clientes'; else tabla">
          <div class="cliente-card" *ngFor="let c of clientes">
            <p class="nombre">{{ c.nombre || 'Cliente sin nombre' }}</p>
            <p class="numero">{{ c.numero }}</p>
            <div class="stats">
              <span>{{ c.conversaciones }} conversaciones</span>
              <span>{{ c.mensajes }} mensajes</span>
            </div>
          </div>
          <p class="vacio" *ngIf="!clientes.length">No hay clientes para mostrar.</p>
        </div>
        <ng-template #tabla>
          <app-liwa-table
            [chats]="chats"
            [contactosAnalizados]="contactosAnalizados"
            (seleccionado)="seleccionar($event)"
          />
        </ng-template>
      </div>
    </app-liwa-modal>
  `,
  styles: [`
    header {
      display: flex; align-items: center; justify-content: space-between; padding: 12px 44px 12px 16px;
      border-bottom: 1px solid #f1f5f9; flex-shrink: 0;
    }
    .titulo { display: flex; align-items: center; gap: 8px; }
    .titulo mat-icon { color: #059669; font-size: 18px; width: 18px; height: 18px; }
    .titulo h2 { margin: 0; font-size: 0.85rem; font-weight: 700; color: #1e293b; }
    .titulo p { margin: 0; font-size: 0.68rem; color: #94a3b8; }
    .cuerpo { min-height: 0; flex: 1; overflow-y: auto; padding: 12px 16px; }
    .clientes-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 8px; }
    .cliente-card { border: 1px solid #e2e8f0; border-radius: 12px; padding: 12px; }
    .cliente-card .nombre { margin: 0; font-size: 0.82rem; font-weight: 700; color: #1e293b; }
    .cliente-card .numero { margin: 2px 0 0; font-size: 0.72rem; color: #64748b; }
    .cliente-card .stats { display: flex; gap: 10px; margin-top: 8px; font-size: 0.65rem; color: #94a3b8; }
    .vacio { text-align: center; color: #94a3b8; padding: 32px; grid-column: 1 / -1; }
  `],
})
export class LiwaSummaryModalComponent {
  @Input() mode: ModalMode = 'conversaciones';
  @Input() chats: LiwaChat[] = [];
  @Input() contactosAnalizados: Set<string> = new Set();
  @Output() cerrar = new EventEmitter<void>();
  @Output() seleccionado = new EventEmitter<LiwaChat>();

  get clientes(): { id: string; numero: string; nombre: string | null; conversaciones: number; mensajes: number }[] {
    const grouped = new Map<string, { id: string; numero: string; nombre: string | null; conversaciones: number; mensajes: number }>();
    this.chats.forEach((chat) => {
      const key = chat.idContacto || chat.numero;
      const current = grouped.get(key) || { id: key, numero: chat.numero, nombre: chat.nombre, conversaciones: 0, mensajes: 0 };
      current.conversaciones += 1;
      current.mensajes += chat.cantidadMensajes || chat.mensajes.length;
      if (!current.nombre && chat.nombre) current.nombre = chat.nombre;
      grouped.set(key, current);
    });
    return Array.from(grouped.values());
  }

  seleccionar(chat: LiwaChat): void {
    this.cerrar.emit();
    this.seleccionado.emit(chat);
  }
}
