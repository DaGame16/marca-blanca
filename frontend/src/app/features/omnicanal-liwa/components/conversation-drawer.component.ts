import { ChangeDetectorRef, Component, ElementRef, EventEmitter, Input, OnChanges, OnDestroy, Output, SimpleChanges, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { LiwaService, formatearNumero } from '../data/liwa.service';
import { LiwaChat, LiwaMensaje, LiwaResumenContacto } from '../models/liwa.model';

export interface MomentoResaltado {
  fecha: string;
  etiqueta: string;
}

// Traducción fiel de components/liwa/ConversationDrawer.tsx: panel deslizante
// desde la derecha, estilo WhatsApp (fondo #ECE5DD), con:
//  - resumen del contacto (casos ya analizados) arriba del chat
//  - revelado progresivo de mensajes (para no golpear con una conversación
//    larga de una vez, igual que el original)
//  - soporte de "momento" resaltado: cuando se abre desde otro panel (ej.
//    Asesores) señalando el instante exacto de un evento (abandono,
//    resolución), oculta lo posterior, resalta esa burbuja y hace scroll ahí.
@Component({
  selector: 'app-liwa-conversation-drawer',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  template: `
    <div class="overlay" [class.visible]="!!chat" (click)="cerrar()"></div>
    <aside class="drawer" [class.abierto]="!!chat">
      <ng-container *ngIf="chat">
        <header class="drawer-header">
          <div class="avatar"><mat-icon>person</mat-icon></div>
          <div class="info-principal">
            <p class="nombre">{{ chat.nombre || 'Sin nombre' }}</p>
            <p class="numero"><span class="punto"></span>{{ formatearNumero(chat.numero) }}</p>
          </div>
          <button type="button" class="accion" title="Llamar"><mat-icon>call</mat-icon></button>
          <button type="button" class="accion" (click)="cerrar()"><mat-icon>close</mat-icon></button>
        </header>

        <div class="info-chat">
          <span>Asesor: {{ chat.agente || 'Sin asignar' }}</span>
          <span class="badge" [class.analizada]="analizada" [class.pendiente]="!analizada">
            {{ analizada ? 'Analizada por IA' : 'Pendiente de análisis' }}
          </span>
        </div>

        <div class="resumen-contacto" *ngIf="chat.idContacto">
          <p class="resumen-titulo">Resumen del contacto</p>
          <div class="resumen-cargando" *ngIf="cargandoResumen">
            <mat-icon class="spin">progress_activity</mat-icon> Cargando resumen del contacto...
          </div>
          <ng-container *ngIf="!cargandoResumen">
            <div class="resumen-caja" *ngIf="resumen && resumen.totalCasos > 0; else sinResumen">
              <mat-icon>psychology</mat-icon>
              <span><strong>{{ resumen.totalCasos }}</strong> caso{{ resumen.totalCasos === 1 ? '' : 's' }} registrado{{ resumen.totalCasos === 1 ? '' : 's' }}</span>
            </div>
            <p class="resumen-detalle" *ngIf="ultimoCasoTexto">{{ ultimoCasoTexto }}</p>
            <ng-template #sinResumen>
              <p class="resumen-vacio">Sin resumen disponible para este contacto.</p>
            </ng-template>
          </ng-container>
        </div>

        <div class="mensajes" #scrollContenedor>
          <div class="momento-banner" *ngIf="momento">
            <mat-icon>history</mat-icon>
            <span>{{ momento.etiqueta }} · {{ momento.fecha | date: "dd/MM/yyyy HH:mm" }}</span>
            <p>El historial se muestra hasta este momento.</p>
          </div>

          <div
            *ngFor="let m of mensajesVisibles; let i = index"
            class="mensaje"
            [class]="'autor-' + m.autor"
            [class.destacado]="esMomentoDestacado(m)"
          >
            <div class="burbuja">
              <span class="nombre-autor" *ngIf="m.autor !== 'cliente'">{{ m.nombreAutor || (m.autor === 'bot' ? 'Bot' : 'Asesor') }}</span>
              <p>{{ m.texto }}</p>
              <span class="fecha">
                {{ m.fecha | date: 'HH:mm' }}
                <mat-icon *ngIf="m.autor !== 'cliente'" class="check">done_all</mat-icon>
              </span>
            </div>
          </div>

          <div class="mensajes-vacio" *ngIf="!chat.mensajes.length">
            <mat-icon>forum</mat-icon>
            <p>No hay mensajes para mostrar.</p>
          </div>
        </div>
      </ng-container>
    </aside>
  `,
  styles: [`
    .overlay {
      position: fixed; inset: 0; z-index: 60; background: rgba(15,23,42,0.4); backdrop-filter: blur(1px);
      opacity: 0; pointer-events: none; transition: opacity 0.3s ease;
    }
    .overlay.visible { opacity: 1; pointer-events: auto; }
    .drawer {
      position: fixed; top: 0; right: 0; height: 100vh; width: 100%; max-width: 420px; background: #fff;
      box-shadow: -8px 0 24px rgba(0,0,0,0.15); z-index: 61; display: flex; flex-direction: column;
      transform: translateX(100%); transition: transform 0.3s ease;
    }
    .drawer.abierto { transform: translateX(0); }

    .drawer-header {
      display: flex; align-items: center; gap: 10px; padding: 12px 16px; background: #059669; color: #fff; flex-shrink: 0;
    }
    .avatar {
      width: 40px; height: 40px; border-radius: 999px; background: rgba(255,255,255,0.2); display: flex;
      align-items: center; justify-content: center; flex-shrink: 0;
    }
    .info-principal { flex: 1; min-width: 0; }
    .nombre { margin: 0; font-weight: 700; font-size: 0.85rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .numero { margin: 1px 0 0; font-size: 0.68rem; color: #d1fae5; display: flex; align-items: center; gap: 5px; }
    .punto { width: 6px; height: 6px; border-radius: 999px; background: #fff; animation: pulso 1.6s ease-in-out infinite; }
    @keyframes pulso { 0%, 100% { opacity: 0.5; } 50% { opacity: 1; } }
    .accion { border: none; background: transparent; color: #fff; padding: 6px; border-radius: 8px; cursor: pointer; }
    .accion:hover { background: rgba(255,255,255,0.15); }

    .info-chat {
      display: flex; align-items: center; justify-content: space-between; padding: 8px 16px; background: #f8fafc;
      border-bottom: 1px solid #f1f5f9; font-size: 0.68rem; color: #475569; flex-shrink: 0;
    }
    .badge { padding: 2px 8px; border-radius: 999px; font-weight: 700; font-size: 0.62rem; }
    .badge.analizada { background: #ecfdf5; color: #047857; }
    .badge.pendiente { background: #fffbeb; color: #b45309; }

    .resumen-contacto { padding: 10px 16px; background: #f8fafc; border-bottom: 1px solid #f1f5f9; flex-shrink: 0; }
    .resumen-titulo { margin: 0 0 6px; font-size: 9px; font-weight: 700; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.05em; }
    .resumen-cargando { display: flex; align-items: center; gap: 6px; font-size: 0.7rem; color: #64748b; }
    .resumen-cargando .spin { animation: spin 1s linear infinite; font-size: 14px; width: 14px; height: 14px; }
    @keyframes spin { to { transform: rotate(360deg); } }
    .resumen-caja {
      display: flex; align-items: center; justify-content: center; gap: 6px; background: #fff; border: 1px solid #f1f5f9;
      border-radius: 8px; padding: 8px; font-size: 0.72rem; color: #1e293b;
    }
    .resumen-caja mat-icon { color: #8b5cf6; font-size: 14px; width: 14px; height: 14px; }
    .resumen-detalle { margin: 6px 0 0; font-size: 0.68rem; color: #64748b; text-align: center; }
    .resumen-vacio { margin: 0; font-size: 0.7rem; color: #94a3b8; text-align: center; }

    .mensajes { flex: 1; overflow-y: auto; padding: 16px; display: flex; flex-direction: column; gap: 8px; background: #ECE5DD; }

    .momento-banner {
      border: 1px solid #fcd34d; background: #fffbeb; border-radius: 8px; padding: 8px 10px; margin-bottom: 4px;
      display: flex; flex-wrap: wrap; align-items: center; gap: 6px; font-size: 0.68rem; color: #92400e; font-weight: 700;
    }
    .momento-banner mat-icon { font-size: 13px; width: 13px; height: 13px; }
    .momento-banner p { margin: 2px 0 0; width: 100%; font-weight: 500; font-size: 0.64rem; color: #b45309; }

    .mensaje { display: flex; animation: aparecer 0.2s ease-out; }
    @keyframes aparecer { from { opacity: 0; transform: translateY(4px); } to { opacity: 1; transform: translateY(0); } }
    .mensaje.autor-cliente { justify-content: flex-start; }
    .mensaje.autor-bot, .mensaje.autor-asesor { justify-content: flex-end; }
    .burbuja { max-width: 78%; padding: 8px 12px; border-radius: 14px; box-shadow: 0 1px 1px rgba(0,0,0,0.08); }
    .autor-cliente .burbuja { background: #fff; color: #1e293b; border-bottom-left-radius: 3px; }
    .autor-bot .burbuja, .autor-asesor .burbuja { background: #10b981; color: #fff; border-bottom-right-radius: 3px; }
    .mensaje.destacado .burbuja { box-shadow: 0 0 0 2px #fbbf24; }
    .nombre-autor { display: block; font-size: 9px; font-weight: 700; color: #d1fae5; margin-bottom: 2px; }
    .burbuja p { margin: 0; font-size: 0.82rem; white-space: pre-wrap; word-break: break-word; }
    .fecha { display: flex; align-items: center; justify-content: flex-end; gap: 2px; font-size: 0.62rem; margin-top: 3px; opacity: 0.7; }
    .fecha .check { font-size: 13px; width: 13px; height: 13px; }

    .mensajes-vacio { display: flex; flex-direction: column; align-items: center; gap: 6px; margin: auto; color: #94a3b8; }
    .mensajes-vacio mat-icon { font-size: 26px; width: 26px; height: 26px; }
    .mensajes-vacio p { margin: 0; font-size: 0.78rem; }
  `],
})
export class LiwaConversationDrawerComponent implements OnChanges, OnDestroy {
  @Input() chat: LiwaChat | null = null;
  @Input() analizada = false;
  @Input() momento: MomentoResaltado | null = null;
  @Output() close = new EventEmitter<void>();

  @ViewChild('scrollContenedor') scrollContenedor?: ElementRef<HTMLDivElement>;

  resumen: LiwaResumenContacto | null = null;
  cargandoResumen = false;
  mensajesVisibles: LiwaMensaje[] = [];

  private timers: ReturnType<typeof setTimeout>[] = [];
  private peticionResumen = 0;

  constructor(private readonly liwa: LiwaService, private readonly cdr: ChangeDetectorRef) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['chat']) {
      this.limpiarTimers();
      this.mensajesVisibles = [];
      if (this.chat) {
        this.iniciarRevelado();
        this.cargarResumen(this.chat.idContacto);
      } else {
        this.resumen = null;
      }
    }
  }

  ngOnDestroy(): void {
    this.limpiarTimers();
  }

  cerrar(): void {
    this.close.emit();
  }

  formatearNumero(numero: string): string {
    return formatearNumero(numero);
  }

  esMomentoDestacado(m: LiwaMensaje): boolean {
    if (!this.momento) return false;
    return new Date(m.fecha).getTime() === new Date(this.momento.fecha).getTime();
  }

  get ultimoCasoTexto(): string | null {
    const casos = this.resumen?.casos ?? [];
    if (!casos.length) return null;
    const ultimo = [...casos].sort((a, b) => (a.archivadaEn < b.archivadaEn ? 1 : -1))[0];
    const a = ultimo.analisis;
    if (!a) return null;
    const partes = [a.resumenMotivo, a.resultado, a.sentimientoFinal].filter((v): v is string => !!v);
    let texto = partes.join(' · ');
    if (a.asesor) texto += (texto ? ' · ' : '') + `Asesor: ${a.asesor}`;
    return texto || null;
  }

  private async cargarResumen(idContacto: string): Promise<void> {
    const id = ++this.peticionResumen;
    this.cargandoResumen = true;
    try {
      const r = await this.liwa.obtenerResumenContacto(idContacto);
      if (id === this.peticionResumen) this.resumen = r;
    } finally {
      if (id === this.peticionResumen) {
        this.cargandoResumen = false;
        this.cdr.markForCheck();
      }
    }
  }

  // Revelado progresivo: el primer mensaje aparece a los 350ms, luego se
  // agregan lotes crecientes cada 90ms -- evita "volcar" toda una
  // conversación larga de una sola vez, igual que el original.
  private iniciarRevelado(): void {
    const todos = this.recortarHastaMomento(this.chat?.mensajes ?? []);
    if (!todos.length) return;

    const tope = todos.length;
    const pasos = Math.min(20, tope);
    const porLote = Math.max(1, Math.ceil(tope / pasos));

    this.timers.push(setTimeout(() => {
      this.mensajesVisibles = todos.slice(0, 1);
      this.cdr.markForCheck();
      this.autoScroll();
      let mostrados = 1;
      const revelarLote = () => {
        mostrados = Math.min(tope, mostrados + porLote);
        this.mensajesVisibles = todos.slice(0, mostrados);
        this.cdr.markForCheck();
        this.autoScroll();
        if (mostrados < tope) this.timers.push(setTimeout(revelarLote, 90));
      };
      if (mostrados < tope) this.timers.push(setTimeout(revelarLote, 90));
    }, 350));
  }

  private recortarHastaMomento(mensajes: LiwaMensaje[]): LiwaMensaje[] {
    if (!this.momento) return mensajes;
    const limite = new Date(this.momento.fecha).getTime();
    const idx = mensajes.findIndex((m) => new Date(m.fecha).getTime() > limite);
    return idx === -1 ? mensajes : mensajes.slice(0, idx + 1);
  }

  private autoScroll(): void {
    setTimeout(() => {
      const el = this.scrollContenedor?.nativeElement;
      if (!el) return;
      if (this.momento) {
        const destacado = el.querySelector('.destacado');
        destacado?.scrollIntoView({ block: 'center' });
      } else {
        el.scrollTop = el.scrollHeight;
      }
    });
  }

  private limpiarTimers(): void {
    this.timers.forEach((t) => clearTimeout(t));
    this.timers = [];
  }
}
