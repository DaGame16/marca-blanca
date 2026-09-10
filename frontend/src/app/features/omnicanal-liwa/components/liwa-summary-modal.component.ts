import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { LiwaChat } from '../models/liwa.model';
import { LiwaTableComponent } from './liwa-table.component';

type ModalMode = 'conversaciones' | 'clientes';

// Traducción de components/liwa/LiwaSummaryModal.tsx — se abre al hacer clic
// en los KPIs "Total de conversaciones" / "Clientes distintos".
@Component({
  selector: 'app-liwa-summary-modal',
  standalone: true,
  imports: [CommonModule, MatIconModule, LiwaTableComponent],
  template: `
    <div class="overlay" (click)="onClose.emit()">
      <div class="modal" (click)="$event.stopPropagation()">
        <header>
          <div class="titulo">
            <mat-icon>{{ mode === 'clientes' ? 'group' : 'chat' }}</mat-icon>
            <div>
              <h2>{{ mode === 'clientes' ? 'Clientes distintos' : 'Conversaciones de WhatsApp' }}</h2>
              <p>{{ mode === 'clientes' ? (clientes.length + ' clientes') : (chats.length + ' conversaciones') }}</p>
            </div>
          </div>
          <button type="button" (click)="onClose.emit()"><mat-icon>close</mat-icon></button>
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
              (onSeleccionar)="seleccionar($event)"
            />
          </ng-template>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .overlay { position: fixed; inset: 0; z-index: 100; background: rgba(2,6,23,0.4); display: flex; align-items: center; justify-content: center; padding: 16px; }
    .modal { width: 100%; max-width: 900px; max-height: 90vh; display: flex; flex-direction: column; background: #fff; border-radius: 16px; overflow: hidden; box-shadow: 0 20px 50px rgba(0,0,0,0.25); }
    header { display: flex; align-items: center; justify-content: space-between; padding: 14px 18px; border-bottom: 1px solid #f1f5f9; }
    .titulo { display: flex; align-items: center; gap: 8px; }
    .titulo mat-icon { color: #059669; }
    .titulo h2 { margin: 0; font-size: 0.9rem; font-weight: 800; color: #1e293b; }
    .titulo p { margin: 0; font-size: 0.68rem; color: #94a3b8; }
    header button { border: none; background: transparent; cursor: pointer; color: #94a3b8; padding: 6px; border-radius: 8px; }
    header button:hover { background: #f1f5f9; color: #334155; }
    .cuerpo { overflow-y: auto; padding: 14px; }
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
  @Output() onClose = new EventEmitter<void>();
  @Output() onSeleccionar = new EventEmitter<LiwaChat>();

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
    this.onClose.emit();
    this.onSeleccionar.emit(chat);
  }
}
