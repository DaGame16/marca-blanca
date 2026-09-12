import { Injectable, OnDestroy, inject, signal } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../auth/auth.service';

/**
 * Cliente STOMP del socket de Omnicanal (Liwa). Reemplaza el polling de 20s
 * que tenia antes omnicanal-liwa-panel.component.ts: el backend (ver
 * ConfiguracionWebSocket en bootstrap) publica en
 * /topic/empresa/{identificadorEmpresa}/omnicanal cada vez que se archiva
 * una conversacion o termina un analisis IA, y este servicio sube `revision`
 * cada vez que llega un mensaje -- mismo rol que useLiwaEvento() en la
 * version original (React). Los componentes que quieran refrescarse solos
 * observan `revision` (una signal), no el contenido del mensaje: el backend
 * es deliberadamente ciego al tipo de evento (ver
 * NotificadorEventosOmnicanal en el backend).
 *
 * El JWT viaja como query param en el handshake (un WebSocket nativo del
 * navegador no admite headers custom) -- ver AutenticacionHandshakeInterceptor
 * en el backend, que es quien lo valida y resuelve la empresa antes de dejar
 * abrir el socket.
 */
@Injectable({ providedIn: 'root' })
export class OmnicanalSocketService implements OnDestroy {
  private readonly auth = inject(AuthService);
  private cliente: Client | null = null;
  private suscriptores = 0;

  /** Sube cada vez que llega un evento de Omnicanal por el socket. */
  readonly revision = signal(0);
  /** true mientras el socket esta efectivamente conectado (no solo "creado"). */
  readonly conectado = signal(false);

  /**
   * Conecta (si hace falta) y cuenta una suscripcion mas. Varios componentes
   * pueden llamar `conectar()` a la vez (uno por pestaña montada) -- el
   * socket real se abre una sola vez y se comparte; solo se cierra cuando
   * el ultimo interesado llama `desconectar()`.
   */
  conectar(): void {
    this.suscriptores++;
    if (this.cliente) {
      return;
    }

    const token = this.auth.getToken();
    const identificadorEmpresa = this.auth.getIdentificadorEmpresa();
    if (!token || !identificadorEmpresa) {
      return;
    }

    this.cliente = new Client({
      brokerURL: `${this.wsBaseUrl()}/ws?token=${encodeURIComponent(token)}`,
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        this.conectado.set(true);
        this.cliente?.subscribe(`/topic/empresa/${identificadorEmpresa}/omnicanal`, (_msg: IMessage) => {
          this.revision.update((n) => n + 1);
        });
      },
      onWebSocketClose: () => this.conectado.set(false),
      onStompError: () => this.conectado.set(false),
    });
    this.cliente.activate();
  }

  /** Libera una suscripcion; cuando nadie mas la necesita, cierra el socket. */
  desconectar(): void {
    this.suscriptores = Math.max(0, this.suscriptores - 1);
    if (this.suscriptores === 0) {
      this.cliente?.deactivate();
      this.cliente = null;
      this.conectado.set(false);
    }
  }

  ngOnDestroy(): void {
    this.cliente?.deactivate();
  }

  // apiUrl es "http://localhost:8080/api/v1" en dev y "/api/v1" (relativo,
  // mismo origen) en produccion -- en ambos casos el endpoint /ws vive en el
  // mismo host que el API, sin el sufijo /api/v1.
  private wsBaseUrl(): string {
    const origen = environment.apiUrl.startsWith('http')
      ? new URL(environment.apiUrl).origin
      : window.location.origin;
    return origen.replace(/^http/, 'ws');
  }
}
