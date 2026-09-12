import { Component, Input, OnChanges, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { LiwaChat } from '../models/liwa.model';
import { formatearNumero } from '../data/liwa.service';

type FiltroEstado = 'todas' | 'analizadas' | 'pendientes';

const POR_PAGINA = 15;

// Traducción fiel de components/liwa/LiwaTable.tsx: buscador + 3 filtros pill
// (Todas/Analizadas/Pendientes) + tabla con avatar/extracto/tiempo relativo +
// paginación de 15 por página (el original la tiene, la version anterior de
// este componente no).
@Component({
  selector: 'app-liwa-table',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule],
  template: `
    <div class="tarjeta">
      <header class="cabecera">
        <div class="titulo">
          <div class="icono"><mat-icon>chat</mat-icon></div>
          <div>
            <p class="titulo-texto">Conversaciones de WhatsApp</p>
            <p class="subtitulo">Archivadas por el bot de Liwa</p>
          </div>
        </div>
        <div class="buscador">
          <mat-icon>search</mat-icon>
          <input type="text" placeholder="Buscar por número..." [(ngModel)]="busqueda" (ngModelChange)="pagina = 1" />
        </div>
        <div class="filtros-estado">
          <button type="button" [class.activo]="filtro === 'todas'" (click)="cambiarFiltro('todas')">Todas</button>
          <button type="button" [class.activo]="filtro === 'analizadas'" (click)="cambiarFiltro('analizadas')">Analizadas</button>
          <button type="button" [class.activo]="filtro === 'pendientes'" (click)="cambiarFiltro('pendientes')">Pendientes</button>
        </div>
      </header>

      <div class="tabla-scroll">
        <table>
          <thead>
            <tr>
              <th>Contacto</th>
              <th>Mensajes</th>
              <th>Archivada</th>
              <th>Análisis IA</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngIf="!filaVisibles.length">
              <td colspan="4" class="vacio">No hay conversaciones que coincidan.</td>
            </tr>
            <tr *ngFor="let c of filaVisibles" class="fila" (click)="seleccionado.emit(c)">
              <td>
                <div class="contacto">
                  <div class="avatar"><mat-icon>chat</mat-icon></div>
                  <div class="contacto-texto">
                    <p class="nombre">{{ c.nombre || formatear(c.numero) || c.idContacto }}</p>
                    <p class="extracto">{{ extracto(c) }}</p>
                  </div>
                </div>
              </td>
              <td><span class="badge-mensajes">{{ c.cantidadMensajes }}</span></td>
              <td class="archivada">{{ tiempoRelativo(c.archivadaEn) }}</td>
              <td>
                <span class="badge-estado" [class.analizada]="analizada(c)" [class.pendiente]="!analizada(c)">
                  {{ analizada(c) ? 'Analizada' : 'Pendiente' }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <footer class="paginacion" *ngIf="filtrados.length">
        <span class="info">Página {{ pagina }} de {{ totalPaginas }} · {{ filtrados.length }} conversaciones en total</span>
        <div class="botones">
          <button type="button" [disabled]="pagina <= 1" (click)="pagina = pagina - 1">Anterior</button>
          <span class="pagina-actual">{{ pagina }}</span>
          <button type="button" [disabled]="pagina >= totalPaginas" (click)="pagina = pagina + 1">Siguiente</button>
        </div>
      </footer>
    </div>
  `,
  styles: [`
    .tarjeta { width: 100%; min-width: 0; background: #fff; border-radius: 16px; border: 1px solid #e2e8f0; box-shadow: 0 1px 2px rgba(0,0,0,0.05); overflow: hidden; }

    .cabecera {
      display: flex; flex-direction: column; gap: 10px; padding: 16px 20px;
      border-bottom: 1px solid #f1f5f9; background: linear-gradient(to right, #f8fafc, #ffffff);
    }
    .titulo { display: flex; align-items: center; gap: 10px; }
    .icono { width: 32px; height: 32px; border-radius: 8px; background: #eff6ff; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
    .icono mat-icon { color: #3b82f6; font-size: 16px; width: 16px; height: 16px; }
    .titulo-texto { margin: 0; font-weight: 700; color: #1e293b; font-size: 0.85rem; }
    .subtitulo { margin: 0; font-size: 10px; color: #94a3b8; }

    .buscador { position: relative; display: flex; align-items: center; }
    .buscador mat-icon { position: absolute; left: 10px; font-size: 15px; width: 15px; height: 15px; color: #94a3b8; }
    .buscador input {
      width: 100%; padding: 8px 12px 8px 32px; font-size: 0.82rem; background: #f8fafc; border: 1px solid #e2e8f0;
      border-radius: 8px; outline: none; box-sizing: border-box;
    }
    .buscador input:focus { box-shadow: 0 0 0 2px #bfdbfe; border-color: #93c5fd; }

    .filtros-estado { display: flex; gap: 6px; flex-wrap: wrap; }
    .filtros-estado button {
      padding: 6px 10px; border-radius: 8px; font-size: 0.68rem; font-weight: 700; border: none; cursor: pointer;
      background: #f1f5f9; color: #64748b;
    }
    .filtros-estado button.activo { background: #2563eb; color: #fff; }

    .tabla-scroll { width: 100%; min-width: 0; max-width: 100%; overflow-x: auto; }
    table { width: 100%; min-width: 560px; table-layout: fixed; font-size: 0.82rem; border-collapse: collapse; }
    th {
      text-align: left; padding: 10px 20px; font-size: 0.65rem; font-weight: 700; text-transform: uppercase;
      letter-spacing: 0.03em; color: #94a3b8; border-bottom: 1px solid #f1f5f9;
    }
    td { padding: 10px 20px; border-bottom: 1px solid #f8fafc; color: #1e293b; }
    .fila { cursor: pointer; transition: background-color 0.15s; }
    .fila:hover { background: #f8fafc; }
    .vacio { text-align: center; color: #94a3b8; padding: 40px; }

    .contacto { display: flex; align-items: center; gap: 10px; min-width: 0; }
    .avatar { width: 36px; height: 36px; border-radius: 999px; background: #ecfdf5; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
    .avatar mat-icon { color: #10b981; font-size: 16px; width: 16px; height: 16px; }
    .contacto-texto { min-width: 0; }
    .nombre { margin: 0; font-weight: 700; color: #1e293b; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .extracto { margin: 2px 0 0; font-size: 0.68rem; color: #94a3b8; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 260px; }

    .badge-mensajes {
      display: inline-flex; align-items: center; justify-content: center; min-width: 24px; padding: 2px 6px;
      border-radius: 999px; background: #f1f5f9; font-size: 0.72rem; font-weight: 700; color: #475569;
    }
    .archivada { color: #64748b; font-size: 0.78rem; white-space: nowrap; }
    .badge-estado {
      display: inline-flex; padding: 2px 8px; border-radius: 999px; font-size: 0.65rem; font-weight: 700;
      box-shadow: inset 0 0 0 1px transparent;
    }
    .badge-estado.analizada { background: #ecfdf5; color: #047857; box-shadow: inset 0 0 0 1px #a7f3d0; }
    .badge-estado.pendiente { background: #fffbeb; color: #b45309; box-shadow: inset 0 0 0 1px #fde68a; }

    .paginacion {
      display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 12px 20px;
      border-top: 1px solid #f1f5f9;
    }
    .info { font-size: 0.68rem; color: #94a3b8; }
    .botones { display: flex; align-items: center; gap: 6px; }
    .botones button {
      padding: 6px 12px; font-size: 0.72rem; font-weight: 700; color: #475569; background: #f1f5f9; border: none;
      border-radius: 8px; cursor: pointer;
    }
    .botones button:disabled { opacity: 0.4; cursor: not-allowed; }
    .pagina-actual {
      padding: 6px 8px; font-size: 0.72rem; font-weight: 700; color: #1d4ed8; background: #eff6ff;
      border: 1px solid #dbeafe; border-radius: 8px; min-width: 2.2rem; text-align: center;
    }

    @media (min-width: 640px) {
      .cabecera { flex-direction: row; align-items: center; flex-wrap: wrap; }
      .buscador { flex: 1; min-width: 160px; }
      .paginacion { flex-direction: row; justify-content: space-between; }
    }
  `],
})
export class LiwaTableComponent implements OnChanges {
  @Input() chats: LiwaChat[] = [];
  @Input() contactosAnalizados: Set<string> = new Set();
  @Output() seleccionado = new EventEmitter<LiwaChat>();

  busqueda = '';
  filtro: FiltroEstado = 'todas';
  pagina = 1;

  ngOnChanges(): void {
    if (this.pagina > this.totalPaginas) this.pagina = 1;
  }

  cambiarFiltro(f: FiltroEstado): void {
    this.filtro = f;
    this.pagina = 1;
  }

  analizada(chat: LiwaChat): boolean {
    return this.contactosAnalizados.has(chat.idContacto);
  }

  get filtrados(): LiwaChat[] {
    const texto = this.busqueda.trim().toLowerCase();
    return this.chats.filter((c) => {
      if (this.filtro === 'analizadas' && !this.analizada(c)) return false;
      if (this.filtro === 'pendientes' && this.analizada(c)) return false;
      if (!texto) return true;
      return (
        (c.numero || '').toLowerCase().includes(texto) ||
        (c.idContacto || '').toLowerCase().includes(texto) ||
        (c.nombre || '').toLowerCase().includes(texto)
      );
    });
  }

  get totalPaginas(): number {
    return Math.max(1, Math.ceil(this.filtrados.length / POR_PAGINA));
  }

  get filaVisibles(): LiwaChat[] {
    const inicio = (this.pagina - 1) * POR_PAGINA;
    return this.filtrados.slice(inicio, inicio + POR_PAGINA);
  }

  formatear(numero: string): string {
    return formatearNumero(numero);
  }

  extracto(chat: LiwaChat): string {
    const ultimo = chat.mensajes.at(-1);
    if (!ultimo) return 'Sin mensajes';
    if (ultimo.autor === 'cliente') return ultimo.texto;
    const prefijo = ultimo.autor === 'bot' ? 'Bot' : (ultimo.nombreAutor || 'Asesor');
    return `${prefijo}: ${ultimo.texto}`;
  }

  tiempoRelativo(fecha: string): string {
    const ms = Date.now() - new Date(fecha).getTime();
    if (!Number.isFinite(ms) || ms < 0) return '—';
    const minutos = Math.floor(ms / 60000);
    if (minutos < 1) return 'hace un momento';
    if (minutos < 60) return `hace ${minutos} min`;
    const horas = Math.floor(minutos / 60);
    if (horas < 24) return `hace ${horas} h`;
    const dias = Math.floor(horas / 24);
    return `hace ${dias} d`;
  }
}
