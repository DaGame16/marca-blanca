import { Component, EventEmitter, OnInit, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { LiwaService } from '../data/liwa.service';
import { VistaConfig } from '../models/liwa.model';

// Pantalla de configuración self-service del tenant para el módulo Liwa --
// las llamadas HTTP (obtenerConfig/actualizarConfig/token/rotar-secreto) ya
// existían en LiwaService desde que se tradujo el servicio (comentario
// original: "Sirven de base para una futura pantalla de Configuración, no
// construida todavía"). Este componente es esa pantalla.
@Component({
  selector: 'app-liwa-config-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule],
  template: `
    <section class="tarjeta">
      <div class="cargando" *ngIf="cargando()"><mat-icon class="spin">progress_activity</mat-icon> Cargando configuración…</div>

      <ng-container *ngIf="!cargando() && config() as config">
        <div class="bloque">
          <h2><mat-icon>hub</mat-icon> Integración con Liwa</h2>
          <p class="ayuda">Datos del bot de WhatsApp que archiva las conversaciones para analizar.</p>

          <div class="campo">
            <label>Token de Liwa</label>
            <div class="fila-token">
              <span class="badge" [class.ok]="config.liwaTokenConfigurado" [class.na]="!config.liwaTokenConfigurado">
                <mat-icon>{{ config.liwaTokenConfigurado ? 'check_circle' : 'error_outline' }}</mat-icon>
                {{ config.liwaTokenConfigurado ? 'Token configurado' : 'Sin token' }}
              </span>
              <button type="button" *ngIf="config.liwaTokenConfigurado" class="btn-outline" (click)="eliminarToken()" [disabled]="guardandoToken">
                Eliminar token
              </button>
            </div>
            <div class="fila-nuevo-token">
              <input type="password" [(ngModel)]="tokenNuevo" placeholder="Pegar nuevo token de Liwa" autocomplete="off" />
              <button type="button" class="btn-primario" [disabled]="!tokenNuevo.trim() || guardandoToken" (click)="guardarToken()">
                @if (guardandoToken) {
                  <mat-icon class="spin">progress_activity</mat-icon>
                } @else {
                  Guardar token
                }
              </button>
            </div>
          </div>
        </div>

        <div class="bloque">
          <h2><mat-icon>webhook</mat-icon> Webhook</h2>
          <p class="ayuda">URL y secreto que Liwa usa para avisarle a la plataforma cuando se archiva una conversación nueva.</p>

          <div class="campo solo-lectura">
            <label>URL del webhook</label>
            <div class="fila-copiar">
              <input type="text" [value]="config.webhookUrl" readonly />
              <button type="button" class="btn-outline" (click)="copiar(config.webhookUrl)">
                <mat-icon>content_copy</mat-icon>
              </button>
            </div>
          </div>

          <div class="campo solo-lectura">
            <label>Secreto del webhook</label>
            <div class="fila-copiar">
              <input [type]="mostrarSecreto ? 'text' : 'password'" [value]="config.webhookSecret" readonly />
              <button type="button" class="btn-outline" (click)="mostrarSecreto = !mostrarSecreto">
                <mat-icon>{{ mostrarSecreto ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
              <button type="button" class="btn-outline" (click)="copiar(config.webhookSecret)">
                <mat-icon>content_copy</mat-icon>
              </button>
            </div>
          </div>

          <button type="button" class="btn-outline peligro" (click)="rotarSecreto()" [disabled]="rotando">
            @if (rotando) {
              <mat-icon class="spin">progress_activity</mat-icon>
            } @else {
              <mat-icon>autorenew</mat-icon>
            }
            Rotar secreto del webhook
          </button>
          <p class="ayuda aviso">Rotar el secreto invalida el anterior de inmediato -- hay que actualizarlo también del lado de Liwa.</p>
        </div>

        <div class="acciones-guardar">
          <button type="button" class="btn-primario grande" [disabled]="guardando" (click)="guardar()">
            @if (guardando) {
              <mat-icon class="spin">progress_activity</mat-icon> Guardando…
            } @else {
              Guardar cambios
            }
          </button>
          <span class="mensaje ok" *ngIf="mensajeOk">{{ mensajeOk }}</span>
          <span class="mensaje error" *ngIf="mensajeError">{{ mensajeError }}</span>
        </div>
      </ng-container>

      <p class="error" *ngIf="!cargando() && !config()">No se pudo cargar la configuración del módulo.</p>
    </section>
  `,
  styles: [`
    .tarjeta { background: #fff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 24px; max-width: 720px; margin: 0 auto; display: flex; flex-direction: column; gap: 28px; }
    .cargando { display: flex; align-items: center; gap: 8px; padding: 24px; color: #94a3b8; font-size: 0.85rem; }
    .spin { animation: spin 1s linear infinite; }
    @keyframes spin { to { transform: rotate(360deg); } }

    .bloque { display: flex; flex-direction: column; gap: 12px; padding-bottom: 24px; border-bottom: 1px solid #f1f5f9; }
    .bloque:last-of-type { border-bottom: none; padding-bottom: 0; }
    .bloque h2 { display: flex; align-items: center; gap: 8px; margin: 0; font-size: 1rem; font-weight: 700; color: #0f172a; }
    .bloque h2 mat-icon { color: #2563eb; }
    .ayuda { margin: 0; font-size: 0.78rem; color: #64748b; line-height: 1.5; }
    .ayuda.aviso { color: #b45309; }

    .check-grande { display: flex; align-items: center; gap: 10px; font-size: 0.88rem; font-weight: 600; color: #1e293b; cursor: pointer; }
    .check-grande input { width: 18px; height: 18px; accent-color: #2563eb; cursor: pointer; }

    .campo { display: flex; flex-direction: column; gap: 6px; }
    .campo label { font-size: 0.75rem; font-weight: 700; color: #475569; }
    .campo input { border: 1px solid #cbd5e1; border-radius: 8px; padding: 9px 12px; font-size: 0.85rem; }
    .campo input:disabled { background: #f1f5f9; color: #94a3b8; }
    .campo.solo-lectura input { background: #f8fafc; color: #475569; }

    .fila-token { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
    .fila-nuevo-token { display: flex; gap: 8px; }
    .fila-nuevo-token input { flex: 1; }
    .fila-copiar { display: flex; gap: 6px; }
    .fila-copiar input { flex: 1; font-family: monospace; font-size: 0.8rem; }

    .badge { display: inline-flex; align-items: center; gap: 4px; font-size: 0.72rem; font-weight: 700; padding: 4px 10px; border-radius: 999px; }
    .badge mat-icon { font-size: 14px; width: 14px; height: 14px; }
    .badge.ok { background: #dcfce7; color: #166534; }
    .badge.na { background: #fef3c7; color: #92400e; }

    .btn-primario { display: inline-flex; align-items: center; justify-content: center; gap: 6px; background: #2563eb; color: #fff; border: none; border-radius: 8px; padding: 9px 16px; font-size: 0.82rem; font-weight: 600; cursor: pointer; }
    .btn-primario:disabled { opacity: 0.6; cursor: default; }
    .btn-primario.grande { padding: 11px 24px; font-size: 0.88rem; }
    .btn-outline { display: inline-flex; align-items: center; justify-content: center; gap: 6px; background: #fff; color: #475569; border: 1px solid #cbd5e1; border-radius: 8px; padding: 8px 12px; font-size: 0.8rem; font-weight: 600; cursor: pointer; }
    .btn-outline:hover { background: #f8fafc; }
    .btn-outline:disabled { opacity: 0.6; cursor: default; }
    .btn-outline.peligro { color: #b91c1c; border-color: #fecaca; width: fit-content; }
    .btn-outline.peligro:hover { background: #fef2f2; }

    .acciones-guardar { display: flex; align-items: center; gap: 14px; flex-wrap: wrap; }
    .mensaje { font-size: 0.82rem; font-weight: 600; }
    .mensaje.ok { color: #166534; }
    .mensaje.error { color: #b91c1c; }
    .error { color: #b91c1c; font-size: 0.85rem; }
  `],
})
export class LiwaConfigPanelComponent implements OnInit {
  // Se emite cuando el token de Liwa queda configurado por primera vez --
  // el panel del modulo lo usa para pasar de "sin configurar" a la vista
  // normal sin que el usuario tenga que recargar la pagina.
  @Output() guardado = new EventEmitter<void>();

  cargando = signal(true);
  config = signal<VistaConfig | null>(null);
  form = { iaHabilitada: false, openaiModelo: '' as string | null, liwaBaseUrl: '' as string | null, liwaCustomFieldAds: '' as string | null };

  tokenNuevo = '';
  mostrarSecreto = false;
  guardando = false;
  guardandoToken = false;
  rotando = false;
  mensajeOk = '';
  mensajeError = '';

  constructor(private readonly liwa: LiwaService) {}

  ngOnInit(): void {
    this.cargar();
  }

  private async cargar(): Promise<void> {
    this.cargando.set(true);
    try {
      const config = await this.liwa.obtenerConfig();
      this.config.set(config);
      this.form = {
        iaHabilitada: config.iaHabilitada,
        openaiModelo: config.openaiModelo,
        liwaBaseUrl: config.liwaBaseUrl,
        liwaCustomFieldAds: config.liwaCustomFieldAds,
      };
    } catch {
      this.config.set(null);
    } finally {
      this.cargando.set(false);
    }
  }

  private limpiarMensajes(): void {
    this.mensajeOk = '';
    this.mensajeError = '';
  }

  async guardar(): Promise<void> {
    this.limpiarMensajes();
    this.guardando = true;
    try {
      this.config.set(await this.liwa.actualizarConfig(this.form));
      this.mensajeOk = 'Configuración guardada.';
    } catch {
      this.mensajeError = 'No se pudo guardar la configuración.';
    } finally {
      this.guardando = false;
    }
  }

  async guardarToken(): Promise<void> {
    if (!this.tokenNuevo.trim()) return;
    this.limpiarMensajes();
    this.guardandoToken = true;
    try {
      await this.liwa.guardarTokenLiwa(this.tokenNuevo.trim());
      this.tokenNuevo = '';
      const noTeniaToken = !this.config()?.liwaTokenConfigurado;
      await this.cargar();
      this.mensajeOk = 'Token guardado.';
      if (noTeniaToken) {
        this.guardado.emit();
      }
    } catch {
      this.mensajeError = 'No se pudo guardar el token.';
    } finally {
      this.guardandoToken = false;
    }
  }

  async eliminarToken(): Promise<void> {
    if (!confirm('¿Quitar el token de Liwa? El módulo no podrá sincronizar conversaciones nuevas hasta que pongas uno nuevo.')) return;
    this.limpiarMensajes();
    this.guardandoToken = true;
    try {
      await this.liwa.eliminarTokenLiwa();
      await this.cargar();
      this.mensajeOk = 'Token eliminado.';
    } catch {
      this.mensajeError = 'No se pudo eliminar el token.';
    } finally {
      this.guardandoToken = false;
    }
  }

  async rotarSecreto(): Promise<void> {
    if (!confirm('¿Rotar el secreto del webhook? El anterior deja de funcionar de inmediato.')) return;
    this.limpiarMensajes();
    this.rotando = true;
    try {
      this.config.set(await this.liwa.rotarSecretoWebhook());
      this.mensajeOk = 'Secreto rotado -- actualízalo también en Liwa.';
    } catch {
      this.mensajeError = 'No se pudo rotar el secreto.';
    } finally {
      this.rotando = false;
    }
  }

  copiar(valor: string): void {
    navigator.clipboard?.writeText(valor);
  }
}
