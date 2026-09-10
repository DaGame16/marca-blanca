import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { LiwaChat } from '../models/liwa.model';

// Traducción simplificada de components/liwa/ConversationDrawer.tsx — panel
// lateral con el historial completo de mensajes de un chat seleccionado.
@Component({
  selector: 'app-liwa-conversation-drawer',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  template: `
    <div class="overlay" *ngIf="chat" (click)="cerrar()"></div>
    <aside class="drawer" [class.abierto]="!!chat">
      <ng-container *ngIf="chat">
        <header class="drawer-header">
          <div>
            <h3>{{ chat.nombre || 'Sin nombre' }}</h3>
            <p>{{ chat.numero }}</p>
          </div>
          <button type="button" class="cerrar" (click)="cerrar()">
            <mat-icon>close</mat-icon>
          </button>
        </header>
        <div class="mensajes">
          <div *ngFor="let m of chat.mensajes" class="mensaje" [class]="'autor-' + m.autor">
            <div class="burbuja">
              <span class="nombre">{{ m.nombreAutor || m.autor }}</span>
              <p>{{ m.texto }}</p>
              <span class="fecha">{{ m.fecha | date: 'dd/MM HH:mm' }}</span>
            </div>
          </div>
        </div>
      </ng-container>
    </aside>
  `,
  styles: [`
    .overlay { position: fixed; inset: 0; background: rgba(15,23,42,0.35); z-index: 40; }
    .drawer {
      position: fixed; top: 0; right: -420px; width: 400px; max-width: 100vw; height: 100vh;
      background: #fff; box-shadow: -8px 0 24px rgba(0,0,0,0.12); z-index: 50;
      transition: right 0.2s ease; display: flex; flex-direction: column;
    }
    .drawer.abierto { right: 0; }
    .drawer-header {
      display: flex; align-items: center; justify-content: space-between;
      padding: 16px; border-bottom: 1px solid #e2e8f0;
    }
    .drawer-header h3 { margin: 0; font-size: 1rem; color: #0f172a; }
    .drawer-header p { margin: 2px 0 0; font-size: 0.75rem; color: #64748b; }
    .cerrar { border: none; background: transparent; cursor: pointer; color: #64748b; }
    .mensajes { flex: 1; overflow-y: auto; padding: 16px; display: flex; flex-direction: column; gap: 10px; background: #f8fafc; }
    .mensaje { display: flex; }
    .mensaje.autor-cliente { justify-content: flex-start; }
    .mensaje.autor-bot, .mensaje.autor-asesor { justify-content: flex-end; }
    .burbuja { max-width: 80%; padding: 8px 12px; border-radius: 12px; background: #fff; border: 1px solid #e2e8f0; }
    .autor-bot .burbuja, .autor-asesor .burbuja { background: #d1fae5; border-color: #a7f3d0; }
    .nombre { display: block; font-size: 0.65rem; font-weight: 700; color: #64748b; margin-bottom: 2px; }
    .burbuja p { margin: 0; font-size: 0.82rem; color: #1e293b; white-space: pre-wrap; }
    .fecha { display: block; font-size: 0.62rem; color: #94a3b8; margin-top: 4px; }
  `],
})
export class LiwaConversationDrawerComponent {
  @Input() chat: LiwaChat | null = null;
  @Output() onClose = new EventEmitter<void>();

  cerrar(): void {
    this.onClose.emit();
  }
}
