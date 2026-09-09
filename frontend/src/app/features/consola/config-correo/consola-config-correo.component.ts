import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ConsolaConfigService } from '../../../core/consola/consola-config.service';
import { ConfiguracionSmtp } from '../../../core/consola/config-plataforma.models';
import { ConsolaNavComponent } from '../nav/consola-nav.component';

const SEGURIDADES = [
  { valor: 'starttls', etiqueta: 'STARTTLS', puertoTipico: 587 },
  { valor: 'ssl', etiqueta: 'SSL / TLS', puertoTipico: 465 },
  { valor: 'ninguna', etiqueta: 'Sin cifrar', puertoTipico: 25 },
];

// Servidores SMTP conocidos por dominio de correo -- para autocompletar host/
// puerto/seguridad apenas el admin termina de escribir el remitente, en vez
// de obligarlo a buscarlos.
const SMTP_POR_DOMINIO: Record<string, { host: string; puerto: number; seguridad: string }> = {
  'gmail.com': { host: 'smtp.gmail.com', puerto: 587, seguridad: 'starttls' },
  'googlemail.com': { host: 'smtp.gmail.com', puerto: 587, seguridad: 'starttls' },
  'outlook.com': { host: 'smtp.office365.com', puerto: 587, seguridad: 'starttls' },
  'hotmail.com': { host: 'smtp.office365.com', puerto: 587, seguridad: 'starttls' },
  'live.com': { host: 'smtp.office365.com', puerto: 587, seguridad: 'starttls' },
  'office365.com': { host: 'smtp.office365.com', puerto: 587, seguridad: 'starttls' },
  'yahoo.com': { host: 'smtp.mail.yahoo.com', puerto: 587, seguridad: 'starttls' },
  'yahoo.es': { host: 'smtp.mail.yahoo.com', puerto: 587, seguridad: 'starttls' },
  'icloud.com': { host: 'smtp.mail.me.com', puerto: 587, seguridad: 'starttls' },
  'me.com': { host: 'smtp.mail.me.com', puerto: 587, seguridad: 'starttls' },
  'zoho.com': { host: 'smtp.zoho.com', puerto: 587, seguridad: 'starttls' },
};

/**
 * CRUD de la configuración SMTP de la plataforma. Puede haber varias filas pero
 * solo una activa a la vez. La clave viaja UNA vez al guardar (se cifra en el
 * backend, ver CifradorDeCorreo) y nunca vuelve a salir -- por eso el campo
 * siempre se ve vacío al editar, con un texto que aclara si ya hay una guardada.
 */
@Component({
  selector: 'app-consola-config-correo',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    FormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatSelectModule,
    MatSnackBarModule,
    MatTooltipModule,
    ConsolaNavComponent,
  ],
  template: `
    <div class="marco">
      <app-consola-nav />

      <section class="tarjeta">
        <div class="cab">
          <div>
            <h1><mat-icon>forward_to_inbox</mat-icon> Configuración de correo</h1>
            <p class="subtitulo">Define desde dónde salen los correos de la plataforma (bienvenida, avisos, etc.)</p>
          </div>
          @if (editando()) {
            <button mat-stroked-button type="button" (click)="nuevo()"><mat-icon>add</mat-icon> Nueva</button>
          }
        </div>

        @if (cargando()) {
          <mat-progress-bar mode="indeterminate" />
        }

        <form [formGroup]="form" (ngSubmit)="guardar()" class="form">
          <div class="form-titulo">{{ editando() ? 'Editar configuración' : 'Nueva configuración' }}</div>

          <div class="seccion">
            <div class="seccion-etiqueta"><mat-icon>badge</mat-icon> Remitente</div>
            <div class="grid">
              <mat-form-field appearance="outline">
                <mat-label>Nombre que verá el destinatario</mat-label>
                <input matInput formControlName="remitenteNombre" placeholder="Marca Blanca" />
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Correo remitente</mat-label>
                <input matInput type="email" formControlName="remitenteCorreo" placeholder="notificaciones@tuempresa.com"
                       (blur)="alEscribirRemitente()" />
                <mat-icon matPrefix>alternate_email</mat-icon>
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Responder a (opcional)</mat-label>
                <input matInput type="email" formControlName="responderA" />
              </mat-form-field>
            </div>
          </div>

          <div class="seccion">
            <div class="seccion-etiqueta"><mat-icon>dns</mat-icon> Servidor SMTP</div>
            <div class="grid">
              <mat-form-field appearance="outline">
                <mat-label>Host</mat-label>
                <input matInput formControlName="host" placeholder="smtp.gmail.com" />
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Puerto</mat-label>
                <input matInput type="number" formControlName="puerto" />
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Seguridad</mat-label>
                <mat-select formControlName="seguridad" (selectionChange)="alCambiarSeguridad($event.value)">
                  @for (s of seguridades; track s.valor) {
                    <mat-option [value]="s.valor">{{ s.etiqueta }} (puerto {{ s.puertoTipico }})</mat-option>
                  }
                </mat-select>
              </mat-form-field>
            </div>
            <p class="ayuda">
              <mat-icon>info</mat-icon>
              STARTTLS va con el puerto 587, SSL/TLS con el 465 -- mezclarlos es la causa más común de que falle la conexión.
            </p>
          </div>

          <div class="seccion">
            <div class="seccion-etiqueta"><mat-icon>lock</mat-icon> Credenciales</div>
            <div class="grid">
              <mat-form-field appearance="outline">
                <mat-label>Usuario SMTP (opcional)</mat-label>
                <input matInput formControlName="usuario" placeholder="Normalmente el mismo correo remitente" />
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>{{ placeholderClave() }}</mat-label>
                <input matInput [type]="ocultarClave() ? 'password' : 'text'" formControlName="clave" autocomplete="new-password" />
                <button mat-icon-button matSuffix type="button" (click)="ocultarClave.set(!ocultarClave())">
                  <mat-icon>{{ ocultarClave() ? 'visibility_off' : 'visibility' }}</mat-icon>
                </button>
              </mat-form-field>
            </div>
            <p class="ayuda">
              <mat-icon>shield</mat-icon>
              Se guarda cifrada en la base de datos -- nunca en texto plano, y nunca vuelve a mostrarse.
              @if (editando()) {
                Dejá este campo vacío para conservar la clave actual.
              }
              Para Gmail/Outlook con verificación en 2 pasos, generá una "contraseña de aplicación" en vez de usar la contraseña normal de la cuenta.
            </p>
            <p class="ayuda ayuda-destacada">
              <mat-icon>verified</mat-icon>
              Al guardar se manda un correo real de prueba al propio remitente para confirmar que la conexión funciona -- si falla, no se guarda nada. No puede haber dos configuraciones con el mismo remitente, ni dos activas a la vez.
            </p>
          </div>

          <div class="acciones">
            <button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || guardando()">
              @if (guardando()) {
                <ng-container>
                  <mat-icon class="girando">autorenew</mat-icon>
                  Verificando conexión…
                </ng-container>
              } @else {
                <ng-container>
                  <mat-icon>save</mat-icon>
                  {{ editando() ? 'Guardar cambios' : 'Crear configuración' }}
                </ng-container>
              }
            </button>
            @if (editando()) {
              <button mat-button type="button" (click)="nuevo()">Cancelar</button>
            }
          </div>
        </form>

        <div class="lista-titulo">Configuraciones guardadas</div>

        @if (configs().length === 0 && !cargando()) {
          <p class="vacio">Todavía no hay ninguna configuración SMTP. Creá una arriba y activala.</p>
        }
        <ul class="lista">
          @for (c of configs(); track c.uuid) {
            <li [class.activa]="c.esActiva">
              <div class="fila-principal">
                <div class="info">
                  <div class="linea-top">
                    <span class="host">{{ c.host }}:{{ c.puerto }}</span>
                    <span class="chip" [class.chip-ok]="c.seguridad !== 'ninguna'">{{ c.seguridad }}</span>
                    @if (c.esActiva) {
                      <span class="badge"><mat-icon>check_circle</mat-icon> Activa</span>
                    }
                    @if (!c.claveConfigurada) {
                      <span class="badge badge-warn" matTooltip="No podrá enviar correos hasta que tenga una clave">
                        <mat-icon>warning</mat-icon> Sin clave
                      </span>
                    }
                  </div>
                  <span class="rem">{{ c.remitenteNombre ? c.remitenteNombre + ' <' + c.remitenteCorreo + '>' : c.remitenteCorreo }}</span>
                </div>
                <div class="btns">
                  @if (!c.esActiva) {
                    <button mat-stroked-button type="button" (click)="activar(c)">Activar</button>
                  }
                  <button mat-icon-button type="button" (click)="editar(c)" aria-label="Editar" matTooltip="Editar">
                    <mat-icon>edit</mat-icon>
                  </button>
                  <button mat-icon-button type="button" (click)="alternarPrueba(c)" aria-label="Enviar prueba" matTooltip="Enviar correo de prueba">
                    <mat-icon>send</mat-icon>
                  </button>
                  <button mat-icon-button type="button" [disabled]="c.esActiva" (click)="eliminar(c)" aria-label="Eliminar" matTooltip="Eliminar">
                    <mat-icon>delete_outline</mat-icon>
                  </button>
                </div>
              </div>

              @if (probando() === c.uuid) {
                <div class="fila-prueba">
                  <mat-form-field appearance="outline" class="campo-destinatario">
                    <mat-label>Enviar prueba a</mat-label>
                    <input matInput type="email" [(ngModel)]="destinatarioPrueba" [ngModelOptions]="{standalone: true}" placeholder="tu@correo.com" />
                  </mat-form-field>
                  <button mat-flat-button color="accent" type="button" [disabled]="enviandoPrueba() || !destinatarioPrueba" (click)="enviarPrueba(c)">
                    @if (enviandoPrueba()) {
                      <mat-icon class="girando">autorenew</mat-icon>
                    } @else {
                      Enviar
                    }
                  </button>
                  @if (resultadoPrueba(); as resultado) {
                    <span class="resultado" [class.ok]="resultado.enviado" [class.error]="!resultado.enviado">
                      <mat-icon>{{ resultado.enviado ? 'check_circle' : 'error' }}</mat-icon>
                      {{ resultado.enviado ? 'Correo enviado correctamente.' : resultado.error }}
                    </span>
                  }
                </div>
              }
            </li>
          }
        </ul>
      </section>
    </div>
  `,
  styles: [
    `
      :host { display: block; min-height: 100vh; background: #f1f5f9; }
      .marco { max-width: 920px; margin: 0 auto; padding: 24px; }
      .tarjeta { background: #fff; border: 1px solid #e2e8f0; border-radius: 14px; padding: 22px 24px; }
      .cab { display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; margin-bottom: 8px; }
      h1 { display: flex; align-items: center; gap: 8px; font-size: 1.25rem; font-weight: 700; margin: 0; color: #0f172a; }
      .subtitulo { margin: 4px 0 0; font-size: 0.85rem; color: #64748b; }

      .form { border: 1px solid #e2e8f0; border-radius: 12px; padding: 18px 20px; margin: 16px 0 24px; background: #f8fafc; }
      .form-titulo { font-weight: 700; color: #0f172a; margin-bottom: 12px; font-size: 0.95rem; }

      .seccion { margin-bottom: 14px; }
      .seccion-etiqueta {
        display: flex; align-items: center; gap: 6px;
        font-size: 0.75rem; font-weight: 700; text-transform: uppercase; letter-spacing: .04em;
        color: #475569; margin-bottom: 6px;
      }
      .seccion-etiqueta mat-icon { font-size: 16px; width: 16px; height: 16px; }

      .grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 4px 14px; }
      mat-form-field { width: 100%; }

      .ayuda {
        display: flex; align-items: flex-start; gap: 6px;
        font-size: 0.78rem; color: #64748b; line-height: 1.5; margin: 2px 0 0;
      }
      .ayuda mat-icon { font-size: 16px; width: 16px; height: 16px; margin-top: 1px; flex-shrink: 0; }
      .ayuda-destacada {
        color: #0e7490; background: #ecfeff; border: 1px solid #a5f3fc;
        border-radius: 8px; padding: 8px 10px; margin-top: 10px;
      }

      .acciones { display: flex; gap: 8px; align-items: center; margin-top: 4px; }

      .lista-titulo { font-weight: 700; color: #0f172a; font-size: 0.95rem; margin-bottom: 10px; }
      .lista { list-style: none; margin: 0; padding: 0; display: grid; gap: 8px; }
      .lista li { border: 1px solid #e2e8f0; border-radius: 10px; padding: 12px 14px; }
      .lista li.activa { border-color: #0e7490; background: #ecfeff; }

      .fila-principal { display: flex; align-items: center; justify-content: space-between; gap: 12px; flex-wrap: wrap; }
      .info { display: flex; flex-direction: column; gap: 3px; }
      .linea-top { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
      .host { font-weight: 600; color: #0f172a; }
      .rem { font-size: 0.8rem; color: #64748b; }

      .chip {
        font-size: 0.68rem; font-weight: 700; text-transform: uppercase;
        background: #e2e8f0; color: #475569; border-radius: 999px; padding: 2px 8px;
      }
      .chip-ok { background: #dcfce7; color: #166534; }

      .badge {
        display: inline-flex; align-items: center; gap: 3px;
        font-size: 0.7rem; font-weight: 700; text-transform: uppercase;
        color: #0e7490; border: 1px solid #0e7490; border-radius: 999px; padding: 2px 8px;
      }
      .badge mat-icon { font-size: 13px; width: 13px; height: 13px; }
      .badge-warn { color: #b45309; border-color: #b45309; }

      .btns { display: flex; align-items: center; gap: 2px; }

      .fila-prueba {
        display: flex; align-items: center; gap: 10px; flex-wrap: wrap;
        margin-top: 12px; padding-top: 12px; border-top: 1px dashed #cbd5e1;
      }
      .campo-destinatario { flex: 1; min-width: 220px; max-width: 320px; margin-bottom: -1.25em; }
      .resultado { display: flex; align-items: center; gap: 4px; font-size: 0.82rem; }
      .resultado.ok { color: #166534; }
      .resultado.error { color: #b91c1c; }

      .girando { animation: girar 1s linear infinite; }
      @keyframes girar { to { transform: rotate(360deg); } }

      .vacio { color: #64748b; font-size: 0.88rem; }
      @media (max-width: 720px) { .grid { grid-template-columns: 1fr; } }
    `,
  ],
})
export class ConsolaConfigCorreoComponent {
  private readonly fb = inject(FormBuilder);
  private readonly config = inject(ConsolaConfigService);
  private readonly snack = inject(MatSnackBar);

  protected readonly seguridades = SEGURIDADES;
  protected readonly configs = signal<ConfiguracionSmtp[]>([]);
  protected readonly cargando = signal(false);
  protected readonly guardando = signal(false);
  protected readonly editando = signal<string | null>(null);
  protected readonly ocultarClave = signal(true);

  protected readonly probando = signal<string | null>(null);
  protected readonly enviandoPrueba = signal(false);
  protected readonly resultadoPrueba = signal<{ enviado: boolean; error?: string } | null>(null);
  protected destinatarioPrueba = '';

  protected readonly form = this.fb.nonNullable.group({
    remitenteNombre: [''],
    remitenteCorreo: ['', [Validators.required, Validators.email]],
    responderA: [''],
    host: ['', [Validators.required]],
    puerto: [587, [Validators.required, Validators.min(1), Validators.max(65535)]],
    usuario: [''],
    secretoRef: [''],
    seguridad: ['starttls', [Validators.required]],
    clave: [''],
  });

  constructor() {
    this.cargar();
  }

  protected placeholderClave(): string {
    if (!this.editando()) {
      return 'Contraseña SMTP';
    }
    const actual = this.configs().find((c) => c.uuid === this.editando());
    return actual?.claveConfigurada ? 'Nueva contraseña (dejar vacío = sin cambios)' : 'Contraseña SMTP (todavía no tiene una)';
  }

  protected alCambiarSeguridad(valor: string): void {
    const sugerido = SEGURIDADES.find((s) => s.valor === valor)?.puertoTipico;
    if (sugerido) {
      this.form.patchValue({ puerto: sugerido });
    }
  }

  /** Detecta el servidor SMTP por el dominio del correo y completa host/puerto/seguridad/usuario. */
  protected alEscribirRemitente(): void {
    const correo = this.form.controls.remitenteCorreo.value.trim().toLowerCase();
    const dominio = correo.split('@')[1];
    if (!dominio) {
      return;
    }

    if (!this.form.controls.usuario.value.trim()) {
      this.form.patchValue({ usuario: correo });
    }

    // No pisa un host que el admin ya haya escrito a mano (ej. su propio Postfix).
    if (this.form.controls.host.value.trim()) {
      return;
    }
    const smtp = SMTP_POR_DOMINIO[dominio];
    if (smtp) {
      this.form.patchValue({ host: smtp.host, puerto: smtp.puerto, seguridad: smtp.seguridad });
    }
  }

  cargar(): void {
    this.cargando.set(true);
    this.config.listarConfigCorreo().subscribe({
      next: (lista) => this.configs.set(lista),
      error: (e: HttpErrorResponse) => this.snack.open(this.mensaje(e), 'Cerrar', { duration: 4000 }),
      complete: () => this.cargando.set(false),
    });
  }

  nuevo(): void {
    this.editando.set(null);
    this.form.reset({
      remitenteNombre: '',
      remitenteCorreo: '',
      responderA: '',
      host: '',
      puerto: 587,
      usuario: '',
      secretoRef: '',
      seguridad: 'starttls',
      clave: '',
    });
  }

  editar(c: ConfiguracionSmtp): void {
    this.editando.set(c.uuid);
    this.form.setValue({
      remitenteNombre: c.remitenteNombre ?? '',
      remitenteCorreo: c.remitenteCorreo,
      responderA: c.responderA ?? '',
      host: c.host,
      puerto: c.puerto,
      usuario: c.usuario ?? '',
      secretoRef: c.secretoRef ?? '',
      seguridad: c.seguridad,
      clave: '',
    });
  }

  guardar(): void {
    if (this.form.invalid) {
      return;
    }
    this.guardando.set(true);
    const v = this.form.getRawValue();
    const payload = {
      remitenteNombre: v.remitenteNombre.trim() || null,
      remitenteCorreo: v.remitenteCorreo.trim(),
      responderA: v.responderA.trim() || null,
      host: v.host.trim(),
      puerto: Number(v.puerto),
      usuario: v.usuario.trim() || null,
      secretoRef: v.secretoRef.trim() || null,
      seguridad: v.seguridad,
      clave: v.clave.trim() || null,
    };
    const id = this.editando();
    const op$ = id ? this.config.actualizarConfigCorreo(id, payload) : this.config.crearConfigCorreo(payload);

    op$.subscribe({
      next: () => {
        this.snack.open(id ? 'Configuración actualizada' : 'Configuración creada', 'OK', { duration: 2500 });
        this.nuevo();
        this.cargar();
      },
      error: (e: HttpErrorResponse) => this.snack.open(this.mensaje(e), 'Cerrar', { duration: 4000 }),
      complete: () => this.guardando.set(false),
    });
  }

  activar(c: ConfiguracionSmtp): void {
    this.config.activarConfigCorreo(c.uuid).subscribe({
      next: () => {
        this.snack.open('Configuración activada', 'OK', { duration: 2500 });
        this.cargar();
      },
      error: (e: HttpErrorResponse) => this.snack.open(this.mensaje(e), 'Cerrar', { duration: 4000 }),
    });
  }

  eliminar(c: ConfiguracionSmtp): void {
    if (!confirm(`¿Eliminar la configuración de ${c.host}?`)) {
      return;
    }
    this.config.eliminarConfigCorreo(c.uuid).subscribe({
      next: () => {
        this.snack.open('Configuración eliminada', 'OK', { duration: 2500 });
        if (this.editando() === c.uuid) {
          this.nuevo();
        }
        this.cargar();
      },
      error: (e: HttpErrorResponse) => this.snack.open(this.mensaje(e), 'Cerrar', { duration: 4000 }),
    });
  }

  alternarPrueba(c: ConfiguracionSmtp): void {
    this.resultadoPrueba.set(null);
    this.probando.set(this.probando() === c.uuid ? null : c.uuid);
  }

  enviarPrueba(c: ConfiguracionSmtp): void {
    if (!this.destinatarioPrueba) {
      return;
    }
    this.enviandoPrueba.set(true);
    this.resultadoPrueba.set(null);
    this.config.probarConfigCorreo(c.uuid, this.destinatarioPrueba).subscribe({
      next: (resultado) => {
        this.resultadoPrueba.set(resultado);
        this.enviandoPrueba.set(false);
      },
      error: (e: HttpErrorResponse) => {
        this.resultadoPrueba.set({
          enviado: false,
          error: (e.error as { error?: string } | null)?.error || 'No se pudo enviar el correo de prueba.',
        });
        this.enviandoPrueba.set(false);
      },
    });
  }

  private mensaje(e: HttpErrorResponse): string {
    if (e.status === 0) {
      return 'No hay conexión con el servidor.';
    }
    if (e.status === 409) {
      return (e.error as { mensaje?: string } | null)?.mensaje ?? 'No se puede: hay que activar otra primero.';
    }
    const backend = (e.error as { mensaje?: string } | null)?.mensaje;
    return backend || 'No se pudo completar la operación.';
  }
}
