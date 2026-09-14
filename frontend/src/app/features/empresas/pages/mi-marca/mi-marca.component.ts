import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MarcaService } from '../../../../core/identidad-visual/marca.service';
import { MarcaDeEmpresa } from '../../../../core/identidad-visual/models';
import { VistaPreviaMarcaService } from '../../../../core/identidad-visual/vista-previa-marca.service';
import { TemaPaginaService, TemaPagina } from '../../../../core/temas/tema-pagina.service';
import { ordenarClaroOscuro } from '../../../../shared/brand/color-utils';
import { estiloFormaLogo } from '../../../../shared/brand/logo-forma';

const FORMATO_HEX = /^#[0-9A-Fa-f]{6}$/;
const MAX_LOGO_BYTES = 500 * 1024;

interface OpcionAjusteLogo {
  valor: number;
  nombre: string;
  css: 'contain' | 'cover' | 'fill';
}

const OPCIONES_AJUSTE: OpcionAjusteLogo[] = [
  { valor: 1, nombre: 'Contener', css: 'contain' },
  { valor: 2, nombre: 'Cubrir', css: 'cover' },
  { valor: 3, nombre: 'Estirar', css: 'fill' },
];

type CodigoTemaLogin = 'lateral' | 'centrado' | 'fondo';

interface OpcionTemaLogin {
  codigo: CodigoTemaLogin;
  numero: number;
  nombre: string;
  descripcion: string;
}

const OPCIONES_LOGIN: OpcionTemaLogin[] = [
  {
    codigo: 'lateral',
    numero: 1,
    nombre: 'Panel lateral',
    descripcion: 'Panel de marca a un lado y el formulario al otro. El diseño clásico.',
  },
  {
    codigo: 'centrado',
    numero: 2,
    nombre: 'Centrado',
    descripcion: 'Tarjeta centrada con el logo arriba, sin panel lateral. Minimalista.',
  },
  {
    codigo: 'fondo',
    numero: 3,
    nombre: 'Fondo completo',
    descripcion: 'Fondo degradado a pantalla completa con el formulario flotando en el centro.',
  },
];

interface OpcionPagina {
  codigo: TemaPagina;
  numero: number;
  nombre: string;
  descripcion: string;
}

const OPCIONES_PAGINA: OpcionPagina[] = [
  { codigo: 'clasico', numero: 1, nombre: 'Clásico', descripcion: 'Barra lateral a la izquierda con la navegación. El diseño actual.' },
  { codigo: 'derecha', numero: 2, nombre: 'Barra a la derecha', descripcion: 'La misma barra de navegación, pero ubicada a la derecha de la pantalla.' },
  { codigo: 'encabezado', numero: 3, nombre: 'Header arriba', descripcion: 'Sin barra lateral: la navegación va en una franja horizontal arriba, con más ancho para el contenido.' },
];

/**
 * Vista unificada de identidad + diseño ("Mi marca" + el antiguo
 * "Experiencia de acceso" en /tema-login, fusionados en una sola pantalla):
 * logo/colores/dominio, diseño del login y densidad de las páginas. Antes
 * vivian en 2 rutas separadas del sidebar -- se combinan porque las 3 cosas
 * son "como se ve mi empresa" y el usuario terminaba saltando entre ambas
 * para configurar su marca completa.
 */
@Component({
  selector: 'app-mi-marca',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    ReactiveFormsModule,
    MatIconModule,
    MatButtonModule,
    MatInputModule,
    MatFormFieldModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
  ],
  template: `
    <div class="marca-page">
      <header class="marca-header">
        <h1>Marca y diseño</h1>
        <p>Personaliza el logo, los colores, el dominio y el diseño con los que tus clientes ven la plataforma.</p>
      </header>

      @if (cargando()) {
        <div class="state-container">
          <mat-spinner diameter="44"></mat-spinner>
          <p>Cargando tu marca...</p>
        </div>
      } @else if (errorCarga()) {
        <div class="state-container">
          <mat-icon color="warn">error_outline</mat-icon>
          <p>{{ errorCarga() }}</p>
          <button mat-stroked-button (click)="cargar()">Reintentar</button>
        </div>
      } @else {
        <!-- IDENTIDAD -->
        <section class="tarjeta">
          <h2><span class="h2-icono violeta"><mat-icon>palette</mat-icon></span>Identidad de marca</h2>
          <p class="hint">Logo, colores y dominio propio con los que tus clientes ven la plataforma.</p>

          <div class="marca-layout">
            <form class="marca-form" [formGroup]="form" (ngSubmit)="guardar()">
              <div class="logo-field">
                <span class="campo-label">Logo de tu empresa</span>
                <div class="logo-row">
                  @if (form.value.urlLogo) {
                    <img
                      [src]="form.value.urlLogo"
                      alt="Vista previa del logo"
                      class="logo-preview"
                      [style.object-fit]="ajusteCss()"
                      [style.border-radius]="formaLogoEstilo().borderRadius"
                      [style.aspect-ratio]="formaLogoEstilo().aspectRatio"
                    />
                  } @else {
                    <div class="logo-preview logo-preview-vacio">
                      <mat-icon>image</mat-icon>
                    </div>
                  }
                  <div class="logo-acciones">
                    <input #inputLogo type="file" accept="image/*" hidden (change)="onLogoSeleccionado($event)" />
                    <button mat-stroked-button type="button" (click)="inputLogo.click()">
                      {{ form.value.urlLogo ? 'Cambiar logo' : 'Subir logo' }}
                    </button>
                    @if (form.value.urlLogo) {
                      @if (esRecortable()) {
                        <button mat-button type="button" (click)="ajustarRecorteExistente()">Mover / recortar</button>
                      }
                      <button mat-button type="button" (click)="quitarLogo()">Quitar</button>
                    }
                  </div>
                </div>
                @if (errorLogo()) {
                  <p class="field-error">{{ errorLogo() }}</p>
                } @else {
                  <p class="campo-hint">PNG o JPG, hasta 500 KB. También puedes pegar la URL de una imagen ya publicada.</p>
                }
              </div>

              <mat-form-field appearance="outline">
                <mat-label>...o pega la URL de una imagen</mat-label>
                <input matInput formControlName="urlLogo" placeholder="https://mi-empresa.com/logo.png" />
                <mat-icon matPrefix>link</mat-icon>
              </mat-form-field>

              @if (form.value.urlLogo) {
                <div class="forma-field">
                  <span class="campo-label">Forma del logo</span>
                  <div class="ajuste-opciones">
                    <button type="button" class="ajuste-boton" [class.ajuste-boton-activo]="form.value.formaLogo === 1" (click)="form.patchValue({ formaLogo: 1 })">Cuadrado</button>
                    <button type="button" class="ajuste-boton" [class.ajuste-boton-activo]="form.value.formaLogo === 2" (click)="form.patchValue({ formaLogo: 2 })">Rectangular</button>
                    <button type="button" class="ajuste-boton" [class.ajuste-boton-activo]="form.value.formaLogo === 3" (click)="form.patchValue({ formaLogo: 3 })">Circular</button>
                  </div>
                </div>
              }

              <div class="color-field">
                <mat-form-field appearance="outline">
                  <mat-label>Color primario</mat-label>
                  <input matInput formControlName="colorPrimario" placeholder="#2563EB" />
                  <mat-icon matPrefix>palette</mat-icon>
                </mat-form-field>
                <input
                  type="color"
                  class="swatch-nativo"
                  [value]="esHexValido(form.value.colorPrimario) ? form.value.colorPrimario : '#2563eb'"
                  (pointerdown)="iniciarArrastreTono()"
                  (input)="form.patchValue({ colorPrimario: $any($event.target).value })"
                  (change)="animarCambioColor()"
                />
              </div>
              @if (form.get('colorPrimario')?.invalid && form.get('colorPrimario')?.touched) {
                <p class="field-error">Formato inválido. Usa un hexadecimal de 6 dígitos, ej: #2563EB</p>
              }

              <div class="color-field">
                <mat-form-field appearance="outline">
                  <mat-label>Color secundario</mat-label>
                  <input matInput formControlName="colorSecundario" placeholder="#1E3A5F" />
                  <mat-icon matPrefix>palette</mat-icon>
                </mat-form-field>
                <input
                  type="color"
                  class="swatch-nativo"
                  [value]="esHexValido(form.value.colorSecundario) ? form.value.colorSecundario : '#1e3a5f'"
                  (pointerdown)="iniciarArrastreTono()"
                  (input)="form.patchValue({ colorSecundario: $any($event.target).value })"
                  (change)="animarCambioColor()"
                />
              </div>
              @if (form.get('colorSecundario')?.invalid && form.get('colorSecundario')?.touched) {
                <p class="field-error">Formato inválido. Usa un hexadecimal de 6 dígitos, ej: #1E3A5F</p>
              }

              <div class="acciones-form">
                <button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || guardando()">
                  @if (guardando()) {
                    <mat-spinner diameter="18"></mat-spinner>
                  } @else {
                    <span>Guardar cambios</span>
                  }
                </button>
                <button mat-button type="button" (click)="restaurarColoresIniciales()">
                  Volver a los colores iniciales
                </button>
              </div>
            </form>

            <aside class="marca-preview">
              <p class="preview-label">Vista previa</p>
              <div class="preview-card">
                <div class="preview-capa" [style.background]="gradienteAnterior()"></div>
                <div
                  class="preview-capa preview-capa-nueva"
                  [class.revelada]="revelando()"
                  [class.sin-transicion]="sinTransicion()"
                  [style.background]="gradienteActual()"
                ></div>
                <div class="preview-contenido">
                  <div class="preview-header">
                    @if (form.value.urlLogo) {
                      <img
                        [src]="form.value.urlLogo"
                        alt="Logo de la empresa"
                        [style.object-fit]="ajusteCss()"
                        [style.border-radius]="formaLogoEstilo().borderRadius"
                        [style.aspect-ratio]="formaLogoEstilo().aspectRatio"
                        (error)="logoConError.set(true)"
                      />
                    } @else {
                      <mat-icon>image</mat-icon>
                    }
                  </div>
                  <button class="preview-btn" type="button" [style.color]="previewPrimario()">Botón de ejemplo</button>
                </div>
              </div>
            </aside>
          </div>
        </section>

        <!-- DISEÑO DE LOGIN -->
        <section class="tarjeta">
          <h2><span class="h2-icono azul"><mat-icon>dashboard_customize</mat-icon></span>Diseño de inicio de sesión</h2>
          <p class="hint">
            Elige cómo se ve la pantalla de login de tu empresa. Todos los que inicien sesión lo van a ver así.
          </p>

          <div class="temas-grid" [style.--acento]="colorAcento()" [style.--acento-oscuro]="colorFondo()">
            @for (opcion of opcionesLogin; track opcion.codigo) {
              <div class="tema-card" [class.tema-card-activa]="codigoLoginActivo() === opcion.codigo">
                <div class="preview" [class]="'preview-' + opcion.codigo">
                  @switch (opcion.codigo) {
                    @case ('lateral') {
                      <div class="preview-lateral">
                        <div class="preview-panel">
                          <span class="preview-logo">
                            @if (form.value.urlLogo) {
                              <img [src]="form.value.urlLogo" alt="" />
                            } @else {
                              <mat-icon>hub</mat-icon>
                            }
                          </span>
                          <div class="preview-linea preview-linea-clara corta"></div>
                          <div class="preview-linea preview-linea-clara"></div>
                        </div>
                        <div class="preview-form">
                          <div class="preview-campo"></div>
                          <div class="preview-campo"></div>
                          <div class="preview-boton"></div>
                        </div>
                      </div>
                    }
                    @case ('centrado') {
                      <div class="preview-centrado">
                        <div class="preview-tarjeta">
                          <span class="preview-logo oscuro">
                            @if (form.value.urlLogo) {
                              <img [src]="form.value.urlLogo" alt="" />
                            } @else {
                              <mat-icon>hub</mat-icon>
                            }
                          </span>
                          <div class="preview-linea corta centrada"></div>
                          <div class="preview-campo"></div>
                          <div class="preview-campo"></div>
                          <div class="preview-boton"></div>
                        </div>
                      </div>
                    }
                    @case ('fondo') {
                      <div class="preview-fondo">
                        <div class="preview-tarjeta preview-tarjeta-flotante">
                          <span class="preview-logo oscuro">
                            @if (form.value.urlLogo) {
                              <img [src]="form.value.urlLogo" alt="" />
                            } @else {
                              <mat-icon>hub</mat-icon>
                            }
                          </span>
                          <div class="preview-linea corta centrada"></div>
                          <div class="preview-campo"></div>
                          <div class="preview-campo"></div>
                          <div class="preview-boton"></div>
                        </div>
                      </div>
                    }
                  }
                </div>

                <h3>{{ opcion.nombre }}</h3>
                <p class="tema-desc">{{ opcion.descripcion }}</p>

                @if (codigoLoginActivo() === opcion.codigo) {
                  <button mat-flat-button disabled class="btn-activo">
                    <mat-icon>check_circle</mat-icon>
                    En uso
                  </button>
                } @else {
                  <button mat-stroked-button [disabled]="guardandoLogin()" (click)="elegirLogin(opcion)">Usar este diseño</button>
                }
              </div>
            }
          </div>
          <p class="hint hint-preview">
            <mat-icon inline>info</mat-icon>
            Las 3 vistas usan tu logo y tus colores de "Identidad de marca" de arriba.
          </p>

          <a routerLink="/login" class="ver-login-link" target="_blank">
            <mat-icon inline>open_in_new</mat-icon>
            Ver el login en una pestaña nueva
          </a>
        </section>

        <!-- DISEÑO DE LAS PÁGINAS -->
        <section class="tarjeta">
          <h2><span class="h2-icono verde"><mat-icon>view_agenda</mat-icon></span>Diseño de las páginas</h2>
          <p class="hint">Cómo se organiza la navegación y el espacio en el resto de la plataforma (menú, listados, etc.).</p>

          <div class="temas-grid" [style.--acento]="colorAcento()" [style.--acento-oscuro]="colorFondo()">
            @for (opcion of opcionesPagina; track opcion.codigo) {
              <div class="tema-card" [class.tema-card-activa]="codigoPaginaActivo() === opcion.codigo">
                <div class="preview preview-pagina" [class]="'preview-pagina-' + opcion.codigo">
                  @if (opcion.codigo === 'encabezado') {
                    <div class="preview-pagina-header">
                      <span class="preview-logo chico">
                        @if (form.value.urlLogo) {
                          <img [src]="form.value.urlLogo" alt="" />
                        } @else {
                          <mat-icon>hub</mat-icon>
                        }
                      </span>
                      <div class="preview-nav-item activo horizontal"></div>
                      <div class="preview-nav-item horizontal"></div>
                      <div class="preview-nav-item horizontal"></div>
                    </div>
                    <div class="preview-pagina-contenido ancho">
                      <div class="preview-pagina-barra grande"></div>
                      @for (fila of [1, 2, 3]; track fila) {
                        <div class="preview-pagina-fila">
                          <div class="preview-pagina-card"></div>
                          <div class="preview-pagina-card"></div>
                          <div class="preview-pagina-card"></div>
                        </div>
                      }
                    </div>
                  } @else {
                    <div class="preview-pagina-sidebar">
                      <span class="preview-logo chico">
                        @if (form.value.urlLogo) {
                          <img [src]="form.value.urlLogo" alt="" />
                        } @else {
                          <mat-icon>hub</mat-icon>
                        }
                      </span>
                      <div class="preview-nav-item activo"></div>
                      <div class="preview-nav-item"></div>
                      <div class="preview-nav-item"></div>
                    </div>
                    <div class="preview-pagina-contenido">
                      <div class="preview-pagina-barra grande"></div>
                      @for (fila of [1, 2, 3]; track fila) {
                        <div class="preview-pagina-fila">
                          <div class="preview-pagina-card"></div>
                          <div class="preview-pagina-card"></div>
                        </div>
                      }
                    </div>
                  }
                </div>

                <h3>{{ opcion.nombre }}</h3>
                <p class="tema-desc">{{ opcion.descripcion }}</p>

                @if (codigoPaginaActivo() === opcion.codigo) {
                  <button mat-flat-button disabled class="btn-activo">
                    <mat-icon>check_circle</mat-icon>
                    En uso
                  </button>
                } @else {
                  <button mat-stroked-button [disabled]="guardandoPagina()" (click)="elegirPagina(opcion)">
                    Usar este diseño
                  </button>
                }
              </div>
            }
          </div>
        </section>
      }
    </div>

    @if (mostrarRecorte()) {
      <div class="recorte-overlay" (pointermove)="moverArrastre($event)" (pointerup)="terminarArrastre()" (pointerleave)="terminarArrastre()">
        <div class="recorte-panel">
          <h3>Ajustar logo</h3>
          <p class="recorte-hint">Arrastra la imagen para moverla y usa +/- para acercar o alejar.</p>

          <div class="recorte-opciones-forma">
            <button type="button" class="recorte-forma-boton" [class.activa]="formaRecorte() === 1" (click)="elegirFormaRecorte(1)">Cuadrado</button>
            <button type="button" class="recorte-forma-boton" [class.activa]="formaRecorte() === 2" (click)="elegirFormaRecorte(2)">Rectangular</button>
            <button type="button" class="recorte-forma-boton" [class.activa]="formaRecorte() === 3" (click)="elegirFormaRecorte(3)">Circular</button>
          </div>

          <div
            class="recorte-marco"
            [style.width.px]="marcoActual().w"
            [style.height.px]="marcoActual().h"
            [style.border-radius]="formaRecorte() === 3 ? '50%' : '10px'"
            (pointerdown)="iniciarArrastre($event)"
          >
            <img
              [src]="imagenRecorteOriginal()"
              class="recorte-imagen"
              [style.width.px]="imagenNaturalW()"
              [style.height.px]="imagenNaturalH()"
              [style.transform]="transformRecorte()"
              (load)="onImagenRecorteCargada($event)"
              draggable="false"
              alt="Imagen a recortar"
            />
          </div>

          <div class="recorte-zoom">
            <button type="button" mat-icon-button (click)="cambiarZoom(-0.1)" aria-label="Alejar">
              <mat-icon>remove</mat-icon>
            </button>
            <input
              type="range"
              min="1"
              max="3"
              step="0.01"
              [value]="zoomRecorte()"
              (input)="zoomRecorte.set(+$any($event.target).value)"
            />
            <button type="button" mat-icon-button (click)="cambiarZoom(0.1)" aria-label="Acercar">
              <mat-icon>add</mat-icon>
            </button>
          </div>

          <div class="recorte-acciones">
            <button mat-button type="button" (click)="cancelarRecorte()">Cancelar</button>
            <button mat-flat-button color="primary" type="button" (click)="confirmarRecorte()">
              <mat-icon>check</mat-icon>
              Aplicar
            </button>
          </div>
        </div>
      </div>
    }
  `,
  styles: [`
    .marca-page {
      min-height: 100%;
      padding: 40px 24px 64px;
      max-width: 1100px;
      margin: 0 auto;
    }

    .marca-header h1 {
      margin: 0 0 6px;
      font-size: 28px;
      font-weight: 700;
      color: #1e3a5f;
    }

    .marca-header p {
      margin: 0 0 32px;
      color: #64748b;
    }

    .state-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12px;
      padding: 64px 0;
      color: #64748b;
    }

    .tarjeta {
      background: #fff;
      border: 1px solid #e2e8f0;
      border-radius: 16px;
      padding: 26px 28px;
      margin-bottom: 24px;
      box-shadow: 0 6px 18px rgba(15, 23, 42, .03);
    }

    h2 {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 1.15rem;
      font-weight: 700;
      margin: 0 0 6px;
      color: #0f172a;
    }

    .h2-icono {
      width: 34px;
      height: 34px;
      border-radius: 10px;
      display: grid;
      place-items: center;
      flex-shrink: 0;
    }
    .h2-icono mat-icon { color: #fff; font-size: 19px; width: 19px; height: 19px; }
    .h2-icono.azul { background: linear-gradient(135deg, #38bdf8, #2563eb); }
    .h2-icono.violeta { background: linear-gradient(135deg, #a855f7, #7c3aed); }
    .h2-icono.verde { background: linear-gradient(135deg, #34d399, #059669); }

    .hint {
      margin: 0 0 20px;
      color: #64748b;
      font-size: 0.88rem;
      line-height: 1.5;
    }

    /* ---------- Identidad de marca ---------- */
    .marca-layout {
      display: grid;
      grid-template-columns: 1fr 320px;
      gap: 28px;
      align-items: start;
    }

    @media (max-width: 720px) {
      .marca-layout {
        grid-template-columns: 1fr;
      }
    }

    .marca-form {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .marca-form mat-form-field {
      width: 100%;
    }

    .campo-label {
      display: block;
      font-size: 0.78rem;
      font-weight: 600;
      color: #475569;
      margin-bottom: 4px;
    }

    .campo-hint {
      margin: 6px 0 16px;
      font-size: 0.78rem;
      color: #94a3b8;
    }

    .logo-field {
      margin-bottom: 18px;
    }

    .logo-row {
      display: flex;
      align-items: center;
      gap: 14px;
    }

    .logo-preview {
      width: 56px;
      height: 56px;
      border-radius: 10px;
      object-fit: contain;
      border: 1px solid #e2e8f0;
      background: #f8fafc;
    }

    .logo-preview-vacio {
      display: flex;
      align-items: center;
      justify-content: center;
      color: #475569;
    }

    .logo-acciones {
      display: flex;
      align-items: center;
      gap: 6px;
    }

    .ajuste-field, .forma-field {
      margin: -4px 0 18px;
    }

    .recorte-overlay {
      position: fixed;
      inset: 0;
      background: rgba(15, 23, 42, 0.55);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1000;
      touch-action: none;
    }

    .recorte-panel {
      background: #fff;
      border-radius: 16px;
      padding: 24px;
      width: min(400px, 92vw);
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 14px;
      box-shadow: 0 20px 50px rgba(0, 0, 0, 0.25);
    }

    .recorte-panel h3 {
      margin: 0;
      align-self: flex-start;
      font-size: 1.05rem;
      color: #0f172a;
    }

    .recorte-hint {
      margin: -8px 0 0;
      align-self: flex-start;
      font-size: 0.78rem;
      color: #64748b;
    }

    .recorte-opciones-forma {
      display: flex;
      gap: 8px;
      width: 100%;
    }

    .recorte-forma-boton {
      flex: 1;
      padding: 7px 8px;
      border: 1.5px solid #e2e8f0;
      border-radius: 8px;
      background: #fff;
      font-size: 0.76rem;
      font-weight: 600;
      color: #475569;
      cursor: pointer;
    }

    .recorte-forma-boton.activa {
      border-color: #2563eb;
      color: #2563eb;
      background: #eff6ff;
    }

    .recorte-marco {
      position: relative;
      overflow: hidden;
      background: repeating-conic-gradient(#e2e8f0 0% 25%, #f8fafc 0% 50%) 0 0 / 20px 20px;
      cursor: grab;
      touch-action: none;
    }

    .recorte-imagen {
      position: absolute;
      top: 50%;
      left: 50%;
      max-width: none;
      transform-origin: center;
      user-select: none;
      pointer-events: none;
    }

    .recorte-zoom {
      display: flex;
      align-items: center;
      gap: 8px;
      width: 100%;
    }

    .recorte-zoom input[type='range'] {
      flex: 1;
    }

    .recorte-acciones {
      display: flex;
      justify-content: flex-end;
      gap: 8px;
      width: 100%;
    }

    .ajuste-opciones {
      display: flex;
      gap: 8px;
      margin-top: 6px;
    }

    .ajuste-boton {
      flex: 1;
      padding: 8px 10px;
      border: 1.5px solid #e2e8f0;
      border-radius: 8px;
      background: #fff;
      font-size: 0.78rem;
      font-weight: 600;
      color: #475569;
      cursor: pointer;
      transition: border-color 0.15s, color 0.15s;
    }

    .ajuste-boton:hover {
      border-color: #cbd5e1;
    }

    .ajuste-boton-activo {
      border-color: #2563eb;
      color: #2563eb;
      background: #eff6ff;
    }

    .color-field {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .color-field mat-form-field {
      flex: 1;
    }

    /* input[type=color] nativo -- al hacer clic abre el selector de color
       real del sistema operativo/navegador (rueda de saturacion/luz + barra
       de tono + hex/RGB), mucho mas completo que reconstruir uno a mano.
       Se le quita el borde por defecto feo de Chrome y se deja solo como
       swatch cuadrado, ocupando el mismo lugar que antes tenia el <span>
       de solo-lectura. */
    .swatch-nativo {
      width: 36px;
      height: 36px;
      border-radius: 8px;
      border: 1px solid #e2e8f0;
      flex-shrink: 0;
      margin-bottom: 20px;
      padding: 0;
      cursor: pointer;
      background: none;
    }
    .swatch-nativo::-webkit-color-swatch-wrapper {
      padding: 3px;
    }
    .swatch-nativo::-webkit-color-swatch {
      border: none;
      border-radius: 5px;
    }
    .swatch-nativo::-moz-color-swatch {
      border: none;
      border-radius: 5px;
    }

    .field-error {
      margin: -8px 0 8px;
      font-size: 12px;
      color: #dc2626;
    }

    .acciones-form {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-top: 12px;
    }

    button[type='submit'] {
      min-width: 160px;
    }

    .marca-preview {
      position: sticky;
      top: 24px;
    }

    .preview-label {
      margin: 0 0 8px;
      font-size: 12px;
      font-weight: 700;
      color: #64748b;
      text-transform: uppercase;
      letter-spacing: 0.06em;
    }

    .preview-card {
      position: relative;
      overflow: hidden;
      border-radius: 16px;
      color: #fff;
    }

    /* "Wipe" de abajo hacia arriba al cambiar de color: la capa vieja queda
       de fondo, la capa nueva entra con clip-path (de totalmente tapada por
       arriba a totalmente visible) -- como el clip tapa desde arriba, al
       encogerse el area visible crece desde ABAJO hacia arriba. */
    .preview-capa {
      position: absolute;
      inset: 0;
    }

    .preview-capa-nueva {
      clip-path: inset(100% 0 0 0);
      transition: clip-path 0.65s cubic-bezier(.65, 0, .35, 1);
    }

    .preview-capa-nueva.revelada {
      clip-path: inset(0 0 0 0);
    }

    .preview-capa-nueva.sin-transicion {
      transition: none;
    }

    .preview-contenido {
      position: relative;
      z-index: 1;
      padding: 24px;
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .preview-header {
      height: 48px;
      display: flex;
      align-items: center;
    }

    .preview-header img {
      max-height: 48px;
      max-width: 160px;
      object-fit: contain;
    }

    .preview-btn {
      align-self: flex-start;
      background: #fff;
      border: none;
      border-radius: 8px;
      padding: 10px 18px;
      font-weight: 600;
      cursor: default;
      transition: color 0.3s ease;
    }

    .preview-domain {
      display: flex;
      align-items: center;
      gap: 6px;
      margin: 0;
      font-size: 13px;
      opacity: 0.85;
    }

    /* ---------- Diseño de login / densidad de paginas (previews grandes,
       con el logo y los colores reales que el cliente eligio arriba) ---------- */
    .temas-grid {
      --acento: #2563eb;
      --acento-oscuro: #1e3a5f;
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 22px;
      margin-bottom: 12px;
    }

    .tema-card {
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      border-radius: 16px;
      padding: 18px;
      display: flex;
      flex-direction: column;
    }

    .tema-card-activa {
      border-color: var(--acento);
      background: #fff;
      box-shadow: 0 0 0 3px color-mix(in srgb, var(--acento) 18%, transparent);
    }

    .preview {
      height: 230px;
      border-radius: 12px;
      overflow: hidden;
      margin-bottom: 16px;
      background: #f1f5f9;
      box-shadow: inset 0 0 0 1px rgba(15, 23, 42, .06);
    }

    .preview-logo {
      width: 34px;
      height: 34px;
      border-radius: 9px;
      display: grid;
      place-items: center;
      flex-shrink: 0;
      background: rgba(255, 255, 255, .18);
      color: #fff;
    }
    .preview-logo.oscuro { background: color-mix(in srgb, var(--acento) 12%, white); color: var(--acento); }
    .preview-logo.chico { width: 26px; height: 26px; border-radius: 7px; }
    .preview-logo img { width: 100%; height: 100%; object-fit: contain; border-radius: inherit; }
    .preview-logo mat-icon { font-size: 18px; width: 18px; height: 18px; }

    .preview-lateral {
      display: grid;
      grid-template-columns: 1fr 1.1fr;
      height: 100%;
    }

    .preview-panel {
      display: flex;
      flex-direction: column;
      gap: 10px;
      padding: 18px;
      background: linear-gradient(135deg, var(--acento-oscuro) 0%, var(--acento) 100%);
    }

    .preview-form {
      display: flex;
      flex-direction: column;
      justify-content: center;
      gap: 10px;
      padding: 20px;
      background: #fff;
    }

    .preview-centrado {
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #e2e8f0;
      padding: 16px;
    }

    .preview-fondo {
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(160deg, #0f172a 0%, var(--acento-oscuro) 45%, var(--acento) 100%);
      padding: 16px;
    }

    .preview-tarjeta {
      width: 78%;
      background: white;
      border-radius: 10px;
      padding: 16px;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 9px;
      box-shadow: 0 6px 16px rgba(15, 23, 42, 0.14);
    }

    .preview-tarjeta-flotante {
      box-shadow: 0 14px 30px rgba(0, 0, 0, 0.4);
    }

    .preview-linea {
      height: 7px;
      border-radius: 3px;
      background: #cbd5e1;
      width: 100%;
    }

    .preview-linea-clara {
      background: rgba(255, 255, 255, .55);
    }

    .preview-linea.corta {
      width: 55%;
      height: 9px;
    }

    .preview-linea.centrada {
      align-self: center;
    }

    .preview-campo {
      height: 16px;
      width: 100%;
      border-radius: 5px;
      border: 1px solid #e2e8f0;
      background: #f8fafc;
    }

    .preview-boton {
      height: 16px;
      width: 100%;
      border-radius: 5px;
      background: var(--acento);
      margin-top: 2px;
    }

    .tema-card h3 {
      font-size: 1rem;
      font-weight: 700;
      margin: 0 0 4px;
      color: #0f172a;
    }

    .tema-desc {
      font-size: 0.84rem;
      color: #64748b;
      line-height: 1.4;
      margin: 0 0 16px;
      min-height: 40px;
    }

    .tema-card button {
      margin-top: auto;
    }

    .btn-activo {
      background: #f0fdf4 !important;
      color: #16a34a !important;
    }

    .hint-preview {
      display: flex;
      align-items: center;
      gap: 6px;
      margin: 0;
      font-size: 0.8rem;
    }
    .hint-preview mat-icon { font-size: 15px; width: 15px; height: 15px; color: #94a3b8; }

    .ver-login-link {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      margin-top: 14px;
      font-size: 0.85rem;
      font-weight: 600;
      color: #2468d9;
      text-decoration: none;
    }

    .ver-login-link:hover {
      text-decoration: underline;
    }

    /* ---------- Densidad de páginas: mockup de sidebar + contenido ---------- */
    .preview-pagina {
      display: grid;
      grid-template-columns: 66px 1fr;
      background: #eef2f7;
    }

    .preview-pagina-sidebar {
      background: var(--acento-oscuro);
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 10px;
      padding: 14px 10px;
    }

    .preview-nav-item {
      width: 100%;
      height: 8px;
      border-radius: 4px;
      background: rgba(255, 255, 255, .18);
    }
    .preview-nav-item.activo { background: var(--acento); }

    .preview-pagina-contenido {
      padding: 16px;
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .preview-pagina-barra {
      height: 10px;
      width: 45%;
      border-radius: 4px;
      background: #cbd5e1;
    }
    .preview-pagina-barra.grande { height: 12px; width: 55%; margin-bottom: 2px; }

    .preview-pagina-fila {
      display: flex;
      gap: 10px;
    }

    .preview-pagina-card {
      flex: 1;
      height: 30px;
      border-radius: 6px;
      background: #fff;
      box-shadow: 0 2px 6px rgba(15, 23, 42, .06);
    }

    /* "A la derecha": mismo grid, solo se invierte el orden de las columnas
       y de los hijos -- asi se ve igual que lo que hace ShellComponent
       (flex-direction: row-reverse) con el sidebar real. */
    .preview-pagina-derecha { grid-template-columns: 1fr 66px; }
    .preview-pagina-derecha .preview-pagina-sidebar { order: 2; }
    .preview-pagina-derecha .preview-pagina-contenido { order: 1; }

    /* "Header arriba": layout real distinto (columna, no grid de sidebar) --
       mismo que implementa ShellComponent cuando tipoPantallaPrincipal=3. */
    .preview-pagina-encabezado {
      grid-template-columns: 1fr;
      grid-template-rows: auto 1fr;
    }
    .preview-pagina-header {
      background: var(--acento-oscuro);
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 10px 12px;
    }
    .preview-nav-item.horizontal { width: 34px; height: 8px; flex-shrink: 0; }
    .preview-pagina-contenido.ancho .preview-pagina-fila { gap: 8px; }
  `],
})
export class MiMarcaComponent implements OnInit, OnDestroy {
  private readonly marcaService = inject(MarcaService);
  private readonly temaPaginaService = inject(TemaPaginaService);
  private readonly vistaPreviaMarca = inject(VistaPreviaMarcaService);
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly opcionesLogin = OPCIONES_LOGIN;
  protected readonly opcionesPagina = OPCIONES_PAGINA;

  protected readonly cargando = signal(true);
  protected readonly guardando = signal(false);
  protected readonly guardandoLogin = signal(false);
  protected readonly guardandoPagina = signal(false);
  protected readonly errorCarga = signal<string | null>(null);
  protected readonly logoConError = signal(false);
  protected readonly errorLogo = signal<string | null>(null);

  // Estado del "wipe" animado de la vista previa al cambiar de color -- ver
  // animarCambioColor(). revelando=true es el estado estable normal (capa
  // nueva completamente visible); se pone en false momentaneamente para
  // reproducir la entrada de abajo hacia arriba.
  protected readonly gradienteAnterior = signal('linear-gradient(135deg, #1e3a5f, #2563eb)');
  protected readonly revelando = signal(true);
  protected readonly sinTransicion = signal(false);

  protected readonly codigoLoginActivo = signal<CodigoTemaLogin>('lateral');
  protected readonly codigoPaginaActivo = signal<TemaPagina>('clasico');

  private marcaActual: MarcaDeEmpresa | null = null;

  protected readonly form = this.fb.nonNullable.group({
    urlLogo: [''],
    ajusteLogo: [1],
    formaLogo: [1],
    colorPrimario: ['', [Validators.pattern(FORMATO_HEX)]],
    colorSecundario: ['', [Validators.pattern(FORMATO_HEX)]],
    dominioPropio: [''],
  });

  // Modal de recorte -- se abre al subir un logo nuevo desde el PC (o al
  // pedir "Ajustar recorte" sobre uno ya subido) para permitir mover/hacer
  // zoom a la imagen y elegir su forma antes de guardarla. Solo aplica a
  // logos subidos como archivo (data:), nunca a URLs externas pegadas -- una
  // imagen de otro dominio "tinta" el canvas al exportar (CORS) y no se
  // puede leer de vuelta como PNG.
  protected readonly mostrarRecorte = signal(false);
  protected readonly imagenRecorteOriginal = signal<string | null>(null);
  protected readonly formaRecorte = signal(1);
  protected readonly zoomRecorte = signal(1);
  protected readonly posicionRecorte = signal({ x: 0, y: 0 });
  protected readonly imagenNaturalW = signal(0);
  protected readonly imagenNaturalH = signal(0);
  protected readonly escalaBase = signal(1);
  private arrastrando = false;
  private arrastreInicio = { x: 0, y: 0 };

  protected readonly marcoActual = computed(() => (this.formaRecorte() === 2 ? { w: 320, h: 160 } : { w: 260, h: 260 }));

  protected readonly transformRecorte = computed(() => {
    const escala = this.escalaBase() * this.zoomRecorte();
    const pos = this.posicionRecorte();
    return `translate(-50%, -50%) translate(${pos.x}px, ${pos.y}px) scale(${escala})`;
  });

  // Metodos (no computed()): dependen de form.value, que es un objeto plano
  // y no una signal -- un computed() nunca se invalidaria y quedaria
  // pegado en el primer valor leido. Igual que el ya existente ajusteCss().
  protected esRecortable(): boolean {
    const url = this.form.value.urlLogo;
    return !!url && url.startsWith('data:');
  }

  protected formaLogoEstilo() {
    return estiloFormaLogo(this.form.value.formaLogo);
  }

  ngOnInit(): void {
    this.cargar();
    // El menu real (sidebar/header, ver ShellComponent) se pinta en vivo con
    // cualquier color que se pruebe aca, incluso antes de guardar -- asi el
    // usuario ve el efecto en toda la plataforma, no solo en la tarjeta de
    // "Vista previa" aislada.
    this.form.get('colorPrimario')!.valueChanges.subscribe(() => this.actualizarVistaPreviaMenu());
    this.form.get('colorSecundario')!.valueChanges.subscribe(() => this.actualizarVistaPreviaMenu());
  }

  ngOnDestroy(): void {
    // Si el usuario se va sin guardar, el menu real vuelve al color
    // realmente guardado -- no debe quedarse pintado con una prueba que
    // nunca se confirmo. Si ya guardo (form === marcaActual), se deja la
    // vista previa puesta: es identica a lo guardado y evita un parpadeo de
    // vuelta al color viejo mientras el Shell no vuelve a cargar desde el
    // backend.
    const v = this.form.getRawValue();
    const huboEdicionSinGuardar =
      (v.colorPrimario || null) !== (this.marcaActual?.colorPrimario ?? null) ||
      (v.colorSecundario || null) !== (this.marcaActual?.colorSecundario ?? null);
    if (huboEdicionSinGuardar) {
      this.vistaPreviaMarca.limpiar();
    }
  }

  private actualizarVistaPreviaMenu(): void {
    const v = this.form.getRawValue();
    this.vistaPreviaMarca.fijar(
      this.esHexValido(v.colorPrimario) ? v.colorPrimario : null,
      this.esHexValido(v.colorSecundario) ? v.colorSecundario : null,
    );
  }

  cargar(): void {
    this.cargando.set(true);
    this.errorCarga.set(null);
    this.marcaService.obtener().subscribe({
      next: (marca: MarcaDeEmpresa) => {
        this.marcaActual = marca;
        this.form.patchValue({
          urlLogo: marca.urlLogo ?? '',
          ajusteLogo: marca.ajusteLogo ?? 1,
          formaLogo: marca.formaLogo ?? 1,
          colorPrimario: marca.colorPrimario ?? '',
          colorSecundario: marca.colorSecundario ?? '',
          dominioPropio: marca.dominioPropio ?? '',
        });
        const opcionLogin = OPCIONES_LOGIN.find((o) => o.numero === marca.tipoLogin);
        this.codigoLoginActivo.set(opcionLogin?.codigo ?? 'lateral');
        const opcionPagina = OPCIONES_PAGINA.find((o) => o.numero === marca.tipoPantallaPrincipal);
        this.codigoPaginaActivo.set(opcionPagina?.codigo ?? 'clasico');
        // Sincroniza la capa "anterior" del wipe con el color real recien
        // cargado -- sin esto, el primer cambio de color animaria desde el
        // azul de relleno en vez de desde el color que ya tenia la empresa.
        this.gradienteAnterior.set(this.gradienteActual());
        this.cargando.set(false);
      },
      error: () => {
        this.errorCarga.set('No pudimos cargar tu marca. Intenta de nuevo.');
        this.cargando.set(false);
      },
    });
  }

  onLogoSeleccionado(event: Event): void {
    const input = event.target as HTMLInputElement;
    const archivo = input.files?.[0];
    if (!archivo) {
      return;
    }
    if (archivo.size > MAX_LOGO_BYTES) {
      this.errorLogo.set('El logo pesa demasiado (máximo 500 KB).');
      input.value = '';
      return;
    }
    this.errorLogo.set(null);
    const lector = new FileReader();
    lector.onload = () => {
      this.abrirRecorte(lector.result as string);
      input.value = '';
    };
    lector.readAsDataURL(archivo);
  }

  quitarLogo(): void {
    this.form.patchValue({ urlLogo: '' });
    this.errorLogo.set(null);
  }

  protected ajusteCss(): 'contain' | 'cover' | 'fill' {
    const valor = this.form.value.ajusteLogo;
    return OPCIONES_AJUSTE.find((o) => o.valor === valor)?.css ?? 'contain';
  }

  protected ajustarRecorteExistente(): void {
    const url = this.form.value.urlLogo;
    if (url) {
      this.abrirRecorte(url);
    }
  }

  private abrirRecorte(dataUrl: string): void {
    this.imagenRecorteOriginal.set(dataUrl);
    this.formaRecorte.set(this.form.value.formaLogo ?? 1);
    this.zoomRecorte.set(1);
    this.posicionRecorte.set({ x: 0, y: 0 });
    this.mostrarRecorte.set(true);
  }

  protected cancelarRecorte(): void {
    this.mostrarRecorte.set(false);
    this.imagenRecorteOriginal.set(null);
  }

  protected elegirFormaRecorte(forma: number): void {
    this.formaRecorte.set(forma);
    // Cambiar de cuadrado/circular a rectangular (u opuesto) cambia el
    // tamaño del marco -- recalcular la escala base para que la imagen
    // siga cubriendo todo el marco nuevo sin dejar huecos.
    const marco = this.marcoActual();
    const anchoNatural = this.imagenNaturalW();
    const altoNatural = this.imagenNaturalH();
    if (anchoNatural && altoNatural) {
      this.escalaBase.set(Math.max(marco.w / anchoNatural, marco.h / altoNatural));
      this.zoomRecorte.set(1);
      this.posicionRecorte.set({ x: 0, y: 0 });
    }
  }

  protected onImagenRecorteCargada(event: Event): void {
    const img = event.target as HTMLImageElement;
    this.imagenNaturalW.set(img.naturalWidth);
    this.imagenNaturalH.set(img.naturalHeight);
    const marco = this.marcoActual();
    this.escalaBase.set(Math.max(marco.w / img.naturalWidth, marco.h / img.naturalHeight));
    this.zoomRecorte.set(1);
    this.posicionRecorte.set({ x: 0, y: 0 });
  }

  protected cambiarZoom(delta: number): void {
    this.zoomRecorte.update((z) => Math.min(3, Math.max(1, +(z + delta).toFixed(2))));
  }

  protected iniciarArrastre(event: PointerEvent): void {
    this.arrastrando = true;
    const pos = this.posicionRecorte();
    this.arrastreInicio = { x: event.clientX - pos.x, y: event.clientY - pos.y };
  }

  protected moverArrastre(event: PointerEvent): void {
    if (!this.arrastrando) {
      return;
    }
    this.posicionRecorte.set({
      x: event.clientX - this.arrastreInicio.x,
      y: event.clientY - this.arrastreInicio.y,
    });
  }

  protected terminarArrastre(): void {
    this.arrastrando = false;
  }

  protected confirmarRecorte(): void {
    const original = this.imagenRecorteOriginal();
    if (!original) {
      return;
    }
    const marco = this.marcoActual();
    const exportW = 480;
    const exportH = Math.round((marco.h / marco.w) * exportW);
    const escala = this.escalaBase() * this.zoomRecorte();
    const pos = this.posicionRecorte();
    const anchoImg = this.imagenNaturalW();
    const altoImg = this.imagenNaturalH();

    const img = new Image();
    img.onload = () => {
      const canvas = document.createElement('canvas');
      canvas.width = exportW;
      canvas.height = exportH;
      const ctx = canvas.getContext('2d');
      if (!ctx) {
        this.errorLogo.set('No se pudo procesar la imagen. Intenta con otra.');
        this.cancelarRecorte();
        return;
      }
      const factorExport = exportW / marco.w;
      ctx.save();
      ctx.translate(exportW / 2, exportH / 2);
      ctx.scale(factorExport, factorExport);
      ctx.drawImage(img, -anchoImg * escala / 2 + pos.x, -altoImg * escala / 2 + pos.y, anchoImg * escala, altoImg * escala);
      ctx.restore();
      try {
        const dataUrl = canvas.toDataURL('image/png');
        this.form.patchValue({ urlLogo: dataUrl, formaLogo: this.formaRecorte() });
      } catch {
        this.errorLogo.set('No se pudo procesar la imagen. Intenta con otra.');
      }
      this.mostrarRecorte.set(false);
      this.imagenRecorteOriginal.set(null);
    };
    img.src = original;
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valores = this.form.getRawValue();
    const marca: MarcaDeEmpresa = {
      urlLogo: valores.urlLogo || null,
      colorPrimario: valores.colorPrimario || null,
      colorSecundario: valores.colorSecundario || null,
      dominioPropio: valores.dominioPropio || null,
      // null = "no tocar" (el backend conserva el valor que ya tenia guardado).
      tipoLogin: this.marcaActual?.tipoLogin ?? null,
      tipoPantallaPrincipal: this.marcaActual?.tipoPantallaPrincipal ?? null,
      ajusteLogo: valores.ajusteLogo || null,
      formaLogo: valores.formaLogo || null,
    };

    this.guardando.set(true);
    this.marcaService.actualizar(marca).subscribe({
      next: () => {
        this.marcaActual = marca;
        this.guardando.set(false);
        this.snackBar.open('Marca actualizada', 'Cerrar', { duration: 3000 });
      },
      error: () => {
        this.guardando.set(false);
        this.snackBar.open('No pudimos guardar los cambios. Intenta de nuevo.', 'Cerrar', { duration: 4000 });
      },
    });
  }

  elegirLogin(opcion: OpcionTemaLogin): void {
    this.guardandoLogin.set(true);
    const marca: MarcaDeEmpresa = {
      urlLogo: this.marcaActual?.urlLogo ?? null,
      colorPrimario: this.marcaActual?.colorPrimario ?? null,
      colorSecundario: this.marcaActual?.colorSecundario ?? null,
      dominioPropio: this.marcaActual?.dominioPropio ?? null,
      tipoLogin: opcion.numero,
      tipoPantallaPrincipal: this.marcaActual?.tipoPantallaPrincipal ?? null,
      ajusteLogo: this.marcaActual?.ajusteLogo ?? null,
      formaLogo: this.marcaActual?.formaLogo ?? null,
    };
    this.marcaService.actualizar(marca).subscribe({
      next: () => {
        this.marcaActual = marca;
        this.codigoLoginActivo.set(opcion.codigo);
        this.guardandoLogin.set(false);
        this.snackBar.open(`Diseño "${opcion.nombre}" activado`, 'Cerrar', { duration: 2500 });
      },
      error: () => {
        this.guardandoLogin.set(false);
        this.snackBar.open('No se pudo guardar el diseño. Intenta de nuevo.', 'Cerrar', { duration: 4000 });
      },
    });
  }

  elegirPagina(opcion: OpcionPagina): void {
    this.guardandoPagina.set(true);
    const marca: MarcaDeEmpresa = {
      urlLogo: this.marcaActual?.urlLogo ?? null,
      colorPrimario: this.marcaActual?.colorPrimario ?? null,
      colorSecundario: this.marcaActual?.colorSecundario ?? null,
      dominioPropio: this.marcaActual?.dominioPropio ?? null,
      tipoLogin: this.marcaActual?.tipoLogin ?? null,
      tipoPantallaPrincipal: opcion.numero,
      ajusteLogo: this.marcaActual?.ajusteLogo ?? null,
      formaLogo: this.marcaActual?.formaLogo ?? null,
    };
    this.marcaService.actualizar(marca).subscribe({
      next: () => {
        this.marcaActual = marca;
        this.codigoPaginaActivo.set(opcion.codigo);
        // Se aplica al instante en este mismo navegador (el shell, la
        // proxima vez que abra en cualquier otro, lo sincroniza desde el
        // backend -- ver ShellComponent).
        this.temaPaginaService.elegir(opcion.codigo);
        this.guardandoPagina.set(false);
        this.snackBar.open(`Diseño "${opcion.nombre}" activado`, 'Cerrar', { duration: 2500 });
      },
      error: () => {
        this.guardandoPagina.set(false);
        this.snackBar.open('No se pudo guardar el diseño. Intenta de nuevo.', 'Cerrar', { duration: 4000 });
      },
    });
  }

  // Se llama al soltar la barra de tono (pointerdown captura el color de
  // "antes" para que el wipe tenga de donde partir; mientras se arrastra,
  // el color cambia en vivo sin animacion -- reanimar en cada pixel se veria
  // entrecortado, no fluido).
  protected iniciarArrastreTono(): void {
    this.gradienteAnterior.set(this.gradienteActual());
  }

  // "Wipe" de abajo hacia arriba: oculta la capa nueva SIN transicion
  // (sinTransicion evita que el ocultamiento en si se anime), espera 2
  // frames para que el navegador confirme ese estado, y recien ahi reactiva
  // la transicion y revela -- de lo contrario el clip-path "rebota" en vez
  // de entrar limpio.
  protected animarCambioColor(): void {
    this.sinTransicion.set(true);
    this.revelando.set(false);
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        this.sinTransicion.set(false);
        this.revelando.set(true);
      });
    });
  }

  protected esHexValido(valor: string | null | undefined): boolean {
    return !!valor && FORMATO_HEX.test(valor);
  }

  protected previewPrimario(): string {
    const valor = this.form.value.colorPrimario;
    return this.esHexValido(valor) ? (valor as string) : '#2563eb';
  }

  protected previewSecundario(): string {
    const valor = this.form.value.colorSecundario;
    return this.esHexValido(valor) ? (valor as string) : '#1e3a5f';
  }

  // El mas oscuro de los 2 colores va de fondo y el mas claro queda como
  // acento en las miniaturas de "Diseño de login"/"Diseño de páginas" -- ver
  // color-utils.ts. Sin esto, las miniaturas no coincidian con lo que
  // realmente pinta ShellComponent/LoginComponent.
  protected colorFondo(): string {
    return ordenarClaroOscuro(this.previewPrimario(), this.previewSecundario()).oscuro ?? '#1e3a5f';
  }

  protected colorAcento(): string {
    return ordenarClaroOscuro(this.previewPrimario(), this.previewSecundario()).claro ?? '#2563eb';
  }

  protected gradienteActual(): string {
    return `linear-gradient(135deg, ${this.colorFondo()}, ${this.colorAcento()})`;
  }

  // "Volver a los colores iniciales": vuelve a los colores que le dimos a
  // la empresa al principio (wizard de registro, o su primer guardado aca
  // si el wizard no los pidio) -- no a lo ultimo guardado, que puede llevar
  // varias vueltas de cambios. El backend guarda ese snapshot aparte (ver
  // color-utils y MarcaDeEmpresa.colorPrimarioOriginal) y nunca lo toca de
  // nuevo. Si una empresa muy vieja no tiene snapshot (creada antes de que
  // existiera esta columna), se cae a lo ultimo guardado como antes.
  protected restaurarColoresIniciales(): void {
    this.gradienteAnterior.set(this.gradienteActual());
    this.form.patchValue({
      colorPrimario: this.marcaActual?.colorPrimarioOriginal ?? this.marcaActual?.colorPrimario ?? '',
      colorSecundario: this.marcaActual?.colorSecundarioOriginal ?? this.marcaActual?.colorSecundario ?? '',
    });
    this.animarCambioColor();
  }
}
