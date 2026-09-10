import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LiwaChat } from '../models/liwa.model';
import { formatearNumero } from '../data/liwa.service';

// Traducción de components/liwa/LiwaTable.tsx.
// NOTA de limitación heredada del original: "procesada" ya no vive en la
// conversación (se movió a LiwaCaso), así que el estado Analizada/Pendiente
// se calcula por fuera comparando contra el set idContacto de /analisis
// (ver contactosAnalizados en el panel).
@Component({
  selector: 'app-liwa-table',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="tabla-wrap">
      <table class="liwa-table">
        <thead>
          <tr>
            <th>Contacto</th>
            <th>Número</th>
            <th>Agente</th>
            <th>Mensajes</th>
            <th>Estado</th>
            <th>Archivada</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngIf="!chats.length">
            <td colspan="6" class="vacio">No hay conversaciones en este rango.</td>
          </tr>
          <tr *ngFor="let c of chats" (click)="onSeleccionar.emit(c)" class="fila">
            <td>{{ c.nombre || 'Sin nombre' }}</td>
            <td>{{ formatear(c.numero) }}</td>
            <td>{{ c.agente || '—' }}</td>
            <td>{{ c.cantidadMensajes }}</td>
            <td>
              <span class="badge" [class.badge-ok]="contactosAnalizados.has(c.idContacto)">
                {{ contactosAnalizados.has(c.idContacto) ? 'Analizada' : 'Pendiente' }}
              </span>
            </td>
            <td>{{ c.archivadaEn | date: 'dd/MM/yyyy HH:mm' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  `,
  styles: [`
    .tabla-wrap { background: #fff; border: 1px solid #e2e8f0; border-radius: 16px; overflow: auto; }
    .liwa-table { width: 100%; border-collapse: collapse; font-size: 0.8rem; }
    .liwa-table th {
      text-align: left; padding: 10px 14px; background: #f8fafc; color: #64748b;
      font-weight: 700; text-transform: uppercase; font-size: 0.65rem; letter-spacing: 0.04em;
      border-bottom: 1px solid #e2e8f0;
    }
    .liwa-table td { padding: 10px 14px; border-bottom: 1px solid #f1f5f9; color: #1e293b; }
    .fila { cursor: pointer; }
    .fila:hover { background: #f8fafc; }
    .vacio { text-align: center; color: #94a3b8; padding: 24px; }
    .badge {
      display: inline-flex; padding: 2px 10px; border-radius: 999px; font-size: 0.7rem;
      font-weight: 700; background: #f1f5f9; color: #64748b;
    }
    .badge-ok { background: #d1fae5; color: #047857; }
  `],
})
export class LiwaTableComponent {
  @Input() chats: LiwaChat[] = [];
  @Input() contactosAnalizados: Set<string> = new Set();
  @Output() onSeleccionar = new EventEmitter<LiwaChat>();

  formatear(numero: string): string {
    return formatearNumero(numero);
  }
}
