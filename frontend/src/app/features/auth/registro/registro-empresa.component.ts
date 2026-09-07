import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TemaLogin, TemaLoginService } from '../../../core/temas/tema-login.service';
import { TemaPagina, TemaPaginaService } from '../../../core/temas/tema-pagina.service';
import { AdminService } from '../../../core/admin/admin.service';
import { Modulo } from '../../../core/admin/models';
import { MarcaPendienteService } from '../../../core/identidad-visual/marca-pendiente.service';
import { RegistroEmpresaService } from './registro-empresa.service';
import { RegistrarEmpresaResponse } from './registro-empresa.models';

// Dominio base solo para el preview visual del identificador.
const DOMINIO_BASE = 'marca-blanca.com';

const RANGO_DIACRITICOS = /[̀-ͯ]/g;
const PATRON_IDENTIFICADOR = /^[a-z][a-z0-9]*(?:_[a-z0-9]+)*$/;

// Debe producir algo que cumpla PATRON_IDENTIFICADOR (igual que
// RegistrarEmpresaRequest.identificador en el backend): minusculas, digitos
// y guion bajo como separador, empezando por letra. Nada de guiones "-".
function generarIdentificador(nombre: string): string {
  let valor = nombre
    .normalize('NFD')
    .replace(RANGO_DIACRITICOS, '') // quita acentos (diacriticos combinados tras normalize NFD)
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '_')
    .replace(/_+/g, '_')
    .replace(/^_+|_+$/g, '');

  if (!valor) {
    valor = 'empresa';
  }
  if (!/^[a-z]/.test(valor)) {
    valor = 'e' + valor;
  }
  return valor.slice(0, 40).replace(/_+$/, '');
}

// Catalogo de respaldo si GET /api/v1/admin/modulos falla (ver
// 0011-poblar-catalogo-modulos.yaml en bootstrap) -- solo para que el
// wizard no se quede sin opciones si el backend no responde.
const MODULOS_RESPALDO: Modulo[] = [
  { id: 'usuarios', codigo: 'usuarios', nombre: 'Usuarios y acceso', descripcion: 'Gestion de usuarios, roles y permisos' },
  { id: 'omnicanal', codigo: 'omnicanal', nombre: 'Comunicacion omnicanal', descripcion: 'Chat interno y bot de WhatsApp (Liwa)' },
  { id: '3cx', codigo: '3cx', nombre: '3CX', descripcion: 'Integracion telefonica 3CX' },
];

interface OpcionTemaLogin {
  codigo: TemaLogin;
  nombre: string;
  descripcion: string;
}

const OPCIONES_TEMA_LOGIN: OpcionTemaLogin[] = [
  { codigo: 'lateral', nombre: 'Panel lateral', descripcion: 'Panel de marca a un lado y el formulario al otro.' },
  { codigo: 'centrado', nombre: 'Centrado', descripcion: 'Tarjeta centrada con el logo arriba, sin panel lateral.' },
  { codigo: 'fondo', nombre: 'Fondo completo', descripcion: 'Fondo degradado a pantalla completa con el formulario flotando.' },
];

interface OpcionTemaPagina {
  codigo: TemaPagina;
  nombre: string;
  descripcion: string;
  icono: string;
}

const OPCIONES_TEMA_PAGINA: OpcionTemaPagina[] = [
  { codigo: 'clasico', nombre: 'Clásico', descripcion: 'El espaciado y densidad actuales de la plataforma.', icono: 'view_agenda' },
  { codigo: 'compacto', nombre: 'Compacto', descripcion: 'Menos espacio entre elementos, más contenido visible.', icono: 'view_headline' },
  { codigo: 'amplio', nombre: 'Amplio', descripcion: 'Más aire entre secciones, tipografía más grande.', icono: 'view_stream' },
];

interface PaletaPredefinida {
  nombre: string;
  primario: string;
  secundario: string;
}

// Paletas de dos colores tipo "swatch" (inspirado en selectores de marca como
// el de Odoo) para elegir rapido; el usuario igual puede afinar con los
// selectores de color de abajo.
const PALETAS_PREDEFINIDAS: PaletaPredefinida[] = [
  { nombre: 'Coast', primario: '#2563eb', secundario: '#facc95' },
  { nombre: 'Candy', primario: '#3b82f6', secundario: '#fbcfe8' },
  { nombre: 'Mint', primario: '#a78bfa', secundario: '#86efac' },
  { nombre: 'Cobalt', primario: '#1d4ed8', secundario: '#d6c9a8' },
  { nombre: 'Coral', primario: '#f87171', secundario: '#fde68a' },
  { nombre: 'Slate', primario: '#f87171', secundario: '#334155' },
  { nombre: 'Esmeralda', primario: '#10b981', secundario: '#134e4a' },
  { nombre: 'Forest', primario: '#166534', secundario: '#a3a380' },
  { nombre: 'Violeta', primario: '#7c3aed', secundario: '#c2410c' },
  { nombre: 'Burgundy', primario: '#9f1239', secundario: '#1e3a5f' },
  { nombre: 'Ember', primario: '#ea580c', secundario: '#bae6fd' },
  { nombre: 'Midnight', primario: '#0f172a', secundario: '#7dd3fc' },
];

const MAX_LOGO_BYTES = 500 * 1024;

type PasoWizard = 1 | 2 | 3 | 4 | 5;

@Component({
  selector: 'app-registro-empresa',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
  ],
  template: `
    <div class="registro-page">
      <header class="topbar">
        <div class="topbar-logo">
          <mat-icon class="topbar-logo-icon">hub</mat-icon>
          <span>Marca Blanca</span>
        </div>
        <a routerLink="/login" class="topbar-link">Ya tengo cuenta</a>
      </header>

      @if (paso() <= 4) {
        <div class="stepper">
          <div class="stepper-item" [class.stepper-item-activo]="paso() >= 1" [class.stepper-item-hecho]="paso() > 1">
            <span class="stepper-circulo">
              @if (paso() > 1) { <mat-icon inline>check</mat-icon> } @else { 1 }
            </span>
            <span class="stepper-texto">Empresa</span>
          </div>
          <span class="stepper-raya" [class.stepper-raya-activa]="paso() > 1"></span>
          <div class="stepper-item" [class.stepper-item-activo]="paso() >= 2" [class.stepper-item-hecho]="paso() > 2">
            <span class="stepper-circulo">
              @if (paso() > 2) { <mat-icon inline>check</mat-icon> } @else { 2 }
            </span>
            <span class="stepper-texto">Módulos</span>
          </div>
          <span class="stepper-raya" [class.stepper-raya-activa]="paso() > 2"></span>
          <div class="stepper-item" [class.stepper-item-activo]="paso() >= 3" [class.stepper-item-hecho]="paso() > 3">
            <span class="stepper-circulo">
              @if (paso() > 3) { <mat-icon inline>check</mat-icon> } @else { 3 }
            </span>
            <span class="stepper-texto">Login</span>
          </div>
          <span class="stepper-raya" [class.stepper-raya-activa]="paso() > 3"></span>
          <div class="stepper-item" [class.stepper-item-activo]="paso() >= 4">
            <span class="stepper-circulo">4</span>
            <span class="stepper-texto">Páginas</span>
          </div>
        </div>
      }

      <section class="form-panel">
        <div class="form-wrapper" [class.form-wrapper-exito]="paso() === 5">
          @switch (paso()) {
            @case (1) {
              <h2>Datos de tu empresa</h2>
              <p class="form-subtitle">Esto es lo que necesita el sistema para crear tu cuenta</p>

              <form [formGroup]="form" (ngSubmit)="irAPaso2()">
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Nombre legal de la empresa</mat-label>
                  <input
                    matInput
                    formControlName="nombreLegal"
                    (input)="onCambiarNombre()"
                    autocomplete="organization"
                  />
                  <mat-icon matPrefix>apartment</mat-icon>
                </mat-form-field>

                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Nombre comercial (opcional)</mat-label>
                  <input matInput formControlName="nombreComercial" />
                  <mat-icon matPrefix>storefront</mat-icon>
                </mat-form-field>

                <div class="slug-preview">
                  @if (editandoIdentificador()) {
                    <mat-form-field appearance="outline" class="full-width">
                      <mat-label>Identificador</mat-label>
                      <input matInput formControlName="identificador" (input)="onEditarIdentificadorManual()" />
                      <mat-icon matPrefix>link</mat-icon>
                    </mat-form-field>
                  } @else {
                    <div class="slug-linea">
                      <span class="slug-texto">
                        <strong>{{ form.controls.identificador.value || 'tu-empresa' }}</strong>.{{ dominioBase }}
                      </span>
                      <button
                        mat-icon-button
                        type="button"
                        (click)="editandoIdentificador.set(true)"
                        aria-label="Editar identificador"
                      >
                        <mat-icon>edit</mat-icon>
                      </button>
                    </div>
                  }
                  <p class="slug-hint">
                    Solo minúsculas, números y guion bajo, empezando por una letra (ej:
                    <code>panaderia_23</code>).
                  </p>
                </div>

                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Dominio propio (opcional)</mat-label>
                  <input matInput formControlName="dominio" placeholder="www.tuempresa.com" />
                  <mat-icon matPrefix>public</mat-icon>
                </mat-form-field>

                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Contraseña maestra</mat-label>
                  <input matInput type="password" formControlName="contrasenaMaestra" />
                  <mat-icon matPrefix>lock</mat-icon>
                </mat-form-field>
                <p class="campo-hint">
                  Es la contraseña del primer usuario administrador de tu empresa (mínimo 8
                  caracteres).
                </p>

                <button
                  mat-flat-button
                  color="primary"
                  class="full-width submit-btn"
                  type="submit"
                  [disabled]="form.invalid"
                >
                  Continuar
                </button>
              </form>
            }

            @case (2) {
              <button mat-button type="button" class="back-link back-link-btn" (click)="paso.set(1)">
                <mat-icon>arrow_back</mat-icon>
                Volver
              </button>

              <h2>Selecciona tus módulos</h2>
              <p class="form-subtitle">Marca los que tu equipo va a usar desde el primer día</p>

              @if (cargandoModulos()) {
                <div class="cargando-modulos">
                  <mat-spinner diameter="28" />
                  <span>Cargando catálogo de módulos…</span>
                </div>
              } @else {
                @if (errorModulos()) {
                  <p class="aviso-respaldo">
                    <mat-icon inline>info_outline</mat-icon>
                    No pudimos cargar el catálogo del servidor, mostrando los módulos conocidos.
                  </p>
                }
                <div class="modulos-lista">
                  @for (modulo of modulos(); track modulo.codigo) {
                    <label class="modulo-item" [class.modulo-item-activo]="estaSeleccionado(modulo.codigo)">
                      <mat-checkbox
                        [checked]="estaSeleccionado(modulo.codigo)"
                        (change)="alternarModulo(modulo.codigo)"
                      />
                      <span class="modulo-texto">
                        <strong>{{ modulo.nombre }}</strong>
                        <span class="modulo-desc">{{ modulo.descripcion }}</span>
                      </span>
                    </label>
                  }
                </div>
              }

              <p class="tema-hint">
                No es obligatorio elegir alguno ahora — puedes activarlos después desde "Mis
                módulos".
              </p>

              <button
                mat-flat-button
                color="primary"
                class="full-width submit-btn"
                type="button"
                (click)="paso.set(3)"
              >
                Continuar
              </button>
            }

            @case (3) {
              <button mat-button type="button" class="back-link back-link-btn" (click)="paso.set(2)">
                <mat-icon>arrow_back</mat-icon>
                Volver
              </button>

              <h2>Diseño de inicio de sesión</h2>
              <p class="form-subtitle">
                Así se va a ver la pantalla de login de
                <strong>{{ form.controls.identificador.value }}.{{ dominioBase }}</strong>
              </p>

              <div class="temas-grid">
                @for (opcion of opcionesLogin; track opcion.codigo) {
                  <button
                    type="button"
                    class="tema-card"
                    [class.tema-card-activa]="temaLogin.tema() === opcion.codigo"
                    (click)="temaLogin.elegir(opcion.codigo)"
                  >
                    <div class="preview" [class]="'preview-' + opcion.codigo">
                      @switch (opcion.codigo) {
                        @case ('lateral') {
                          <div class="preview-lateral">
                            <div class="preview-panel" [style.background]="colorPrimario()">
                              @if (logoDataUrl()) {
                                <img [src]="logoDataUrl()" alt="" class="preview-logo" />
                              }
                            </div>
                            <div class="preview-form">
                              <div class="preview-linea corta" [style.background]="colorSecundario()"></div>
                              <div class="preview-linea"></div>
                            </div>
                          </div>
                        }
                        @case ('centrado') {
                          <div class="preview-centrado">
                            <div class="preview-tarjeta">
                              @if (logoDataUrl()) {
                                <img [src]="logoDataUrl()" alt="" class="preview-logo preview-logo-chica" />
                              }
                              <div class="preview-linea corta centrada" [style.background]="colorPrimario()"></div>
                              <div class="preview-linea"></div>
                            </div>
                          </div>
                        }
                        @case ('fondo') {
                          <div class="preview-fondo" [style.background]="'linear-gradient(135deg, ' + colorPrimario() + ', ' + colorSecundario() + ')'">
                            <div class="preview-tarjeta">
                              @if (logoDataUrl()) {
                                <img [src]="logoDataUrl()" alt="" class="preview-logo preview-logo-chica" />
                              }
                              <div class="preview-linea corta centrada" [style.background]="colorPrimario()"></div>
                              <div class="preview-linea"></div>
                            </div>
                          </div>
                        }
                      }
                    </div>
                    <h3>{{ opcion.nombre }}</h3>
                    <p>{{ opcion.descripcion }}</p>
                    @if (temaLogin.tema() === opcion.codigo) {
                      <span class="tema-activo-badge">
                        <mat-icon inline>check_circle</mat-icon>
                        Seleccionado
                      </span>
                    }
                  </button>
                }
              </div>

              <div class="marca-fields">
                <div class="logo-field">
                  <span class="campo-label">Logo de tu empresa</span>
                  <div class="logo-row">
                    @if (logoDataUrl()) {
                      <img [src]="logoDataUrl()" alt="Vista previa del logo" class="logo-preview" />
                    } @else {
                      <div class="logo-preview logo-preview-vacio">
                        <mat-icon>image</mat-icon>
                      </div>
                    }
                    <div class="logo-acciones">
                      <input #inputLogo type="file" accept="image/*" hidden (change)="onLogoSeleccionado($event)" />
                      <button mat-stroked-button type="button" (click)="inputLogo.click()">
                        {{ logoDataUrl() ? 'Cambiar logo' : 'Subir logo' }}
                      </button>
                      @if (logoDataUrl()) {
                        <button mat-button type="button" (click)="quitarLogo()">Quitar</button>
                      }
                    </div>
                  </div>
                  @if (errorLogo()) {
                    <p class="error-logo">{{ errorLogo() }}</p>
                  } @else {
                    <p class="campo-hint">PNG o JPG, hasta 500 KB.</p>
                  }
                </div>

                <div class="paleta-field">
                  <span class="campo-label">Paleta de colores</span>
                  <div class="paleta-grid">
                    @for (paleta of paletasPredefinidas; track paleta.nombre) {
                      <button
                        type="button"
                        class="paleta-swatch"
                        [class.paleta-swatch-activa]="colorPrimario() === paleta.primario && colorSecundario() === paleta.secundario"
                        (click)="elegirPaleta(paleta)"
                      >
                        <span class="paleta-colores">
                          <span class="paleta-mitad" [style.background]="paleta.primario"></span>
                          <span class="paleta-mitad" [style.background]="paleta.secundario"></span>
                        </span>
                        <span class="paleta-nombre">{{ paleta.nombre }}</span>
                        @if (colorPrimario() === paleta.primario && colorSecundario() === paleta.secundario) {
                          <mat-icon class="paleta-check" inline>check_circle</mat-icon>
                        }
                      </button>
                    }
                  </div>
                </div>

                <div class="colores-fields">
                  <label class="color-field">
                    <span class="campo-label">Primario (a medida)</span>
                    <input
                      type="color"
                      [value]="colorPrimario()"
                      (input)="colorPrimario.set($any($event.target).value)"
                    />
                    <span class="color-valor">{{ colorPrimario() }}</span>
                  </label>
                  <label class="color-field">
                    <span class="campo-label">Secundario (a medida)</span>
                    <input
                      type="color"
                      [value]="colorSecundario()"
                      (input)="colorSecundario.set($any($event.target).value)"
                    />
                    <span class="color-valor">{{ colorSecundario() }}</span>
                  </label>
                </div>
              </div>

              <p class="tema-hint">
                Podrás cambiar todo esto cuando quieras desde "Mi marca" y "Diseño de inicio de
                sesión", ya dentro de la plataforma.
              </p>

              <button
                mat-flat-button
                color="primary"
                class="full-width submit-btn"
                type="button"
                (click)="paso.set(4)"
              >
                Continuar
              </button>
            }

            @case (4) {
              <button mat-button type="button" class="back-link back-link-btn" (click)="paso.set(3)">
                <mat-icon>arrow_back</mat-icon>
                Volver
              </button>

              <h2>Diseño de páginas</h2>
              <p class="form-subtitle">El logo y los colores ya quedaron definidos, aquí solo eliges el estilo</p>

              <div class="resumen-marca">
                @if (logoDataUrl()) {
                  <img [src]="logoDataUrl()" alt="Logo elegido" class="resumen-logo" />
                } @else {
                  <div class="resumen-logo resumen-logo-vacio">
                    <mat-icon>image</mat-icon>
                  </div>
                }
                <span class="resumen-color" [style.background]="colorPrimario()"></span>
                <span class="resumen-color" [style.background]="colorSecundario()"></span>
                <span class="resumen-texto">Logo y colores elegidos en el paso anterior</span>
              </div>

              <div class="temas-pagina-lista">
                @for (opcion of opcionesPagina; track opcion.codigo) {
                  <button
                    type="button"
                    class="tema-pagina-item"
                    [class.tema-pagina-item-activo]="temaPagina.tema() === opcion.codigo"
                    (click)="temaPagina.elegir(opcion.codigo)"
                  >
                    <mat-icon>{{ opcion.icono }}</mat-icon>
                    <span class="tema-pagina-texto">
                      <strong>{{ opcion.nombre }}</strong>
                      <span>{{ opcion.descripcion }}</span>
                    </span>
                    @if (temaPagina.tema() === opcion.codigo) {
                      <mat-icon class="tema-pagina-check">check_circle</mat-icon>
                    }
                  </button>
                }
              </div>

              @if (errorCreacion()) {
                <p class="error-creacion">
                  <mat-icon inline>error_outline</mat-icon>
                  {{ errorCreacion() }}
                </p>
              }

              <button
                mat-flat-button
                color="primary"
                class="full-width submit-btn"
                type="button"
                [disabled]="creando()"
                (click)="crearEmpresa()"
              >
                @if (creando()) {
                  <mat-spinner diameter="20" />
                } @else {
                  Crear empresa
                }
              </button>

              <p class="nota-preview">
                <mat-icon>info_outline</mat-icon>
                La base de datos de tu empresa se crea en segundo plano — puede tardar unos segundos
                después de este paso.
              </p>
            }

            @case (5) {
              <div class="exito-panel">
                <p class="construyendo-texto">
                  Construyendo tu espacio para
                  <strong>{{ form.controls.nombreComercial.value || form.controls.nombreLegal.value || form.controls.identificador.value }}</strong>
                </p>
                <div class="construyendo-barra">
                  <div class="construyendo-barra-relleno"></div>
                </div>

                <div class="exito-tarjeta">
                  <mat-icon class="exito-icono">check_circle</mat-icon>
                  <h2>Empresa registrada</h2>
                  <p class="form-subtitle">
                    <strong>{{ form.controls.identificador.value }}</strong> quedó creada con estado
                    <code>{{ resultado()?.estado }}</code>. El aprovisionamiento de la base de datos
                    sigue en curso — intenta iniciar sesión en un momento con el identificador y la
                    contraseña maestra que definiste.
                  </p>
                  <a mat-flat-button color="primary" routerLink="/login" class="full-width submit-btn">
                    Ir a iniciar sesión
                  </a>
                </div>
              </div>
            }
          }
        </div>
      </section>
    </div>
  `,
  styles: [
    `
      :host {
        --brand-dark: #1e3a5f;
        --brand-light: #2563eb;
        display: block;
      }

      .registro-page {
        min-height: 100vh;
        display: flex;
        flex-direction: column;
        background: radial-gradient(circle at 20% -10%, #1e293b 0%, #0f172a 55%, #0b1120 100%);
      }

      .topbar {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 20px 32px;
      }

      .topbar-logo {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 18px;
        font-weight: 700;
        color: #f1f5f9;
      }

      .topbar-logo-icon {
        color: #60a5fa;
      }

      .topbar-link {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        font-size: 0.88rem;
        font-weight: 600;
        color: #93c5fd;
        text-decoration: none;
      }

      .topbar-link:hover {
        text-decoration: underline;
      }

      .stepper {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        padding: 8px 24px 28px;
      }

      .stepper-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 6px;
      }

      .stepper-circulo {
        width: 30px;
        height: 30px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 0.85rem;
        font-weight: 700;
        background: #1e293b;
        color: #64748b;
        transition: background 0.2s, color 0.2s;
      }

      .stepper-circulo mat-icon {
        font-size: 16px;
        width: 16px;
        height: 16px;
      }

      .stepper-item-activo .stepper-circulo {
        background: #3b82f6;
        color: #fff;
      }

      .stepper-item-hecho .stepper-circulo {
        background: #16a34a;
        color: #fff;
      }

      .stepper-texto {
        font-size: 0.72rem;
        font-weight: 600;
        color: #64748b;
      }

      .stepper-item-activo .stepper-texto {
        color: #f1f5f9;
      }

      .stepper-raya {
        flex: 0 0 40px;
        height: 2px;
        background: #1e293b;
        margin-bottom: 20px;
        transition: background 0.2s;
      }

      .stepper-raya-activa {
        background: #16a34a;
      }

      .form-panel {
        display: flex;
        align-items: flex-start;
        justify-content: center;
        padding: 8px 24px 64px;
        flex: 1;
      }

      .form-wrapper {
        width: 100%;
        max-width: 560px;
        padding: 32px 36px 40px;
        background: #fff;
        border-radius: 16px;
        box-shadow: 0 1px 2px rgba(0, 0, 0, 0.2), 0 24px 60px rgba(0, 0, 0, 0.45);
      }

      .form-wrapper-exito {
        text-align: center;
        background: transparent;
        box-shadow: none;
        max-width: 640px;
      }

      .back-link {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        font-size: 0.85rem;
        color: #64748b;
        text-decoration: none;
        margin-bottom: 32px;
      }

      .back-link-btn {
        padding: 0;
      }

      .back-link:hover {
        color: var(--brand-light);
      }

      .back-link mat-icon {
        font-size: 18px;
        width: 18px;
        height: 18px;
      }

      .form-wrapper h2 {
        font-size: 1.7rem;
        font-weight: 700;
        margin: 0 0 8px;
        color: #0f172a;
      }

      .form-subtitle {
        color: #64748b;
        margin: 0 0 28px;
        font-size: 0.94rem;
        line-height: 1.5;
      }

      .full-width {
        width: 100%;
      }

      .slug-preview {
        margin: -8px 0 20px;
      }

      .slug-linea {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 8px;
        background: white;
        border: 1px solid #e2e8f0;
        border-radius: 8px;
        padding: 10px 8px 10px 16px;
      }

      .slug-texto {
        font-size: 0.95rem;
        color: #334155;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .slug-texto strong {
        color: var(--brand-light);
      }

      .slug-hint,
      .campo-hint {
        margin: 8px 0 16px;
        font-size: 0.78rem;
        color: #94a3b8;
      }

      .slug-hint code,
      .campo-hint code {
        font-size: 0.9em;
        background: #eef1f6;
        border-radius: 4px;
        padding: 1px 4px;
      }

      .submit-btn {
        margin-top: 8px;
        height: 44px;
        font-size: 15px;
        background-color: var(--brand-light) !important;
      }

      .nota-preview {
        display: flex;
        align-items: flex-start;
        gap: 8px;
        margin: 16px 0 0;
        font-size: 0.78rem;
        color: #94a3b8;
        line-height: 1.4;
      }

      .nota-preview mat-icon {
        font-size: 16px;
        width: 16px;
        height: 16px;
        margin-top: 1px;
      }

      /* ---------- Paso 2: modulos ---------- */
      .cargando-modulos {
        display: flex;
        align-items: center;
        gap: 10px;
        color: #64748b;
        font-size: 0.88rem;
        padding: 20px 0;
      }

      .aviso-respaldo {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 0.8rem;
        color: #b45309;
        background: #fdf3e3;
        border: 1px solid #f2d9ac;
        border-radius: 8px;
        padding: 8px 12px;
        margin: 0 0 14px;
      }

      .modulos-lista {
        display: flex;
        flex-direction: column;
        gap: 10px;
        margin-bottom: 8px;
      }

      .modulo-item {
        display: flex;
        align-items: flex-start;
        gap: 10px;
        background: white;
        border: 1px solid #e2e8f0;
        border-radius: 10px;
        padding: 12px 14px;
        cursor: pointer;
      }

      .modulo-item-activo {
        border-color: var(--brand-light);
        box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
      }

      .modulo-texto {
        display: flex;
        flex-direction: column;
        gap: 2px;
        font-size: 0.88rem;
        color: #0f172a;
      }

      .modulo-desc {
        font-size: 0.78rem;
        color: #64748b;
        font-weight: 400;
      }

      /* ---------- Paso 3: temas de login + marca ---------- */
      .temas-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
        gap: 12px;
        margin-bottom: 24px;
      }

      .tema-card {
        text-align: left;
        background: white;
        border: 1px solid #e2e8f0;
        border-radius: 12px;
        padding: 12px;
        cursor: pointer;
        font: inherit;
        color: inherit;
        display: flex;
        flex-direction: column;
      }

      .tema-card:hover {
        border-color: #93c5fd;
      }

      .tema-card-activa {
        border-color: var(--brand-light);
        box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
      }

      .preview {
        height: 70px;
        border-radius: 8px;
        overflow: hidden;
        margin-bottom: 10px;
        background: #f1f5f9;
      }

      .preview-lateral {
        display: grid;
        grid-template-columns: 1fr 1fr;
        height: 100%;
      }

      .preview-panel {
        background: linear-gradient(135deg, var(--brand-dark) 0%, var(--brand-light) 100%);
      }

      .preview-form {
        display: flex;
        flex-direction: column;
        justify-content: center;
        gap: 6px;
        padding: 10px;
        background: #f8fafc;
      }

      .preview-centrado {
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #e2e8f0;
      }

      .preview-fondo {
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        background: linear-gradient(160deg, #0f172a 0%, var(--brand-dark) 45%, var(--brand-light) 100%);
      }

      .preview-tarjeta {
        width: 70%;
        background: white;
        border-radius: 6px;
        padding: 8px;
        display: flex;
        flex-direction: column;
        gap: 6px;
      }

      .preview-linea {
        height: 5px;
        border-radius: 3px;
        background: #cbd5e1;
      }

      .preview-linea.corta {
        width: 60%;
        height: 6px;
      }

      .preview-logo {
        max-width: 32px;
        max-height: 32px;
        object-fit: contain;
        border-radius: 4px;
        background: rgba(255, 255, 255, 0.85);
        padding: 3px;
      }

      .preview-logo-chica {
        align-self: center;
        margin-bottom: 4px;
      }

      .preview-linea.centrada {
        align-self: center;
      }

      .tema-card h3 {
        font-size: 0.85rem;
        font-weight: 700;
        margin: 0 0 2px;
        color: #0f172a;
      }

      .tema-card p {
        font-size: 0.72rem;
        color: #64748b;
        line-height: 1.3;
        margin: 0;
      }

      .tema-activo-badge {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        margin-top: 8px;
        font-size: 0.72rem;
        font-weight: 600;
        color: #16a34a;
      }

      .marca-fields {
        display: flex;
        flex-direction: column;
        gap: 18px;
        background: white;
        border: 1px solid #e2e8f0;
        border-radius: 12px;
        padding: 16px;
        margin-bottom: 8px;
      }

      .campo-label {
        display: block;
        font-size: 0.78rem;
        font-weight: 600;
        color: #475569;
        margin-bottom: 8px;
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
        object-fit: cover;
        border: 1px solid #e2e8f0;
      }

      .logo-preview-vacio {
        display: flex;
        align-items: center;
        justify-content: center;
        background: #f1f5f9;
        color: #94a3b8;
      }

      .logo-acciones {
        display: flex;
        align-items: center;
        gap: 6px;
      }

      .error-logo {
        margin: 8px 0 0;
        font-size: 0.78rem;
        color: #dc2626;
      }

      .paleta-field {
        margin-bottom: 18px;
      }

      .paleta-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(96px, 1fr));
        gap: 8px;
        margin-top: 8px;
      }

      .paleta-swatch {
        position: relative;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 6px;
        padding: 8px 6px;
        border: 1.5px solid #e2e8f0;
        border-radius: 10px;
        background: #fff;
        cursor: pointer;
        transition: border-color 0.15s, transform 0.1s;
      }

      .paleta-swatch:hover {
        border-color: #cbd5e1;
      }

      .paleta-swatch-activa {
        border-color: var(--brand-light);
        box-shadow: 0 0 0 1px var(--brand-light);
      }

      .paleta-colores {
        display: flex;
        width: 100%;
        height: 24px;
        border-radius: 6px;
        overflow: hidden;
      }

      .paleta-mitad {
        flex: 1;
      }

      .paleta-nombre {
        font-size: 0.68rem;
        font-weight: 600;
        color: #475569;
      }

      .paleta-check {
        position: absolute;
        top: -6px;
        right: -6px;
        font-size: 16px;
        width: 16px;
        height: 16px;
        color: #16a34a;
        background: #fff;
        border-radius: 50%;
      }

      .colores-fields {
        display: flex;
        gap: 20px;
        flex-wrap: wrap;
      }

      .color-field {
        display: flex;
        flex-direction: column;
        gap: 6px;
      }

      .color-field input[type='color'] {
        width: 46px;
        height: 32px;
        border: 1px solid #e2e8f0;
        border-radius: 6px;
        padding: 2px;
        cursor: pointer;
        background: none;
      }

      .color-valor {
        font-size: 0.72rem;
        color: #64748b;
        font-family: monospace;
      }

      .tema-hint {
        font-size: 0.78rem;
        color: #94a3b8;
        margin: 4px 0 20px;
      }

      /* ---------- Paso 4: resumen de marca + tema de pagina ---------- */
      .resumen-marca {
        display: flex;
        align-items: center;
        gap: 10px;
        background: white;
        border: 1px solid #e2e8f0;
        border-radius: 10px;
        padding: 10px 14px;
        margin-bottom: 20px;
      }

      .resumen-logo {
        width: 32px;
        height: 32px;
        border-radius: 8px;
        object-fit: cover;
      }

      .resumen-logo-vacio {
        display: flex;
        align-items: center;
        justify-content: center;
        background: #f1f5f9;
        color: #94a3b8;
      }

      .resumen-color {
        width: 18px;
        height: 18px;
        border-radius: 50%;
        border: 1px solid rgba(0, 0, 0, 0.1);
      }

      .resumen-texto {
        font-size: 0.78rem;
        color: #64748b;
        margin-left: 4px;
      }

      .temas-pagina-lista {
        display: flex;
        flex-direction: column;
        gap: 10px;
        margin-bottom: 24px;
      }

      .tema-pagina-item {
        display: flex;
        align-items: center;
        gap: 12px;
        text-align: left;
        background: white;
        border: 1px solid #e2e8f0;
        border-radius: 10px;
        padding: 12px 14px;
        cursor: pointer;
        font: inherit;
        color: #0f172a;
      }

      .tema-pagina-item:hover {
        border-color: #93c5fd;
      }

      .tema-pagina-item-activo {
        border-color: var(--brand-light);
        box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
      }

      .tema-pagina-texto {
        display: flex;
        flex-direction: column;
        gap: 2px;
        font-size: 0.88rem;
      }

      .tema-pagina-texto span {
        font-size: 0.76rem;
        color: #64748b;
        font-weight: 400;
      }

      .tema-pagina-check {
        margin-left: auto;
        color: #16a34a;
      }

      .error-creacion {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 0.82rem;
        color: #dc2626;
        background: #fef2f2;
        border: 1px solid #fecaca;
        border-radius: 8px;
        padding: 8px 12px;
        margin: 0 0 14px;
      }

      /* ---------- Paso 5: exito ---------- */
      .exito-panel {
        text-align: center;
        padding: 20px 0;
        width: 100%;
      }

      .construyendo-texto {
        font-size: 1.15rem;
        font-weight: 600;
        color: #f1f5f9;
        margin: 0 0 20px;
      }

      .construyendo-texto strong {
        color: #93c5fd;
      }

      .construyendo-barra {
        width: 100%;
        max-width: 280px;
        height: 4px;
        margin: 0 auto 32px;
        border-radius: 4px;
        background: #1e293b;
        overflow: hidden;
      }

      .construyendo-barra-relleno {
        height: 100%;
        width: 40%;
        border-radius: 4px;
        background: linear-gradient(90deg, #3b82f6, #93c5fd);
        animation: construyendo-avance 1.4s ease-in-out infinite;
      }

      @keyframes construyendo-avance {
        0% {
          transform: translateX(-100%);
        }
        100% {
          transform: translateX(350%);
        }
      }

      .exito-tarjeta {
        background: #fff;
        border-radius: 16px;
        padding: 28px 32px 32px;
        box-shadow: 0 1px 2px rgba(0, 0, 0, 0.2), 0 24px 60px rgba(0, 0, 0, 0.45);
      }

      .exito-icono {
        font-size: 48px;
        width: 48px;
        height: 48px;
        color: #16a34a;
        margin-bottom: 12px;
      }

      .exito-panel code {
        background: #eef1f6;
        border-radius: 4px;
        padding: 1px 6px;
      }

      @media (max-width: 900px) {
        .topbar {
          padding: 16px 20px;
        }

        .stepper {
          gap: 4px;
          padding: 4px 12px 20px;
        }

        .stepper-raya {
          flex-basis: 20px;
        }

        .stepper-texto {
          display: none;
        }

        .form-panel {
          padding: 4px 16px 48px;
        }

        .form-wrapper {
          padding: 24px 20px 28px;
        }
      }
    `,
  ],
})
export class RegistroEmpresaComponent {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly snackBar = inject(MatSnackBar);
  private readonly adminService = inject(AdminService);
  private readonly registroService = inject(RegistroEmpresaService);
  private readonly marcaPendiente = inject(MarcaPendienteService);

  // Codigo de modulo que llego por query param (ej: /registro?modulo=omnicanal),
  // usado desde el boton "Adquirir modulo" en el detalle de un modulo. Se
  // preselecciona en el paso 2 en cuanto el catalogo termina de cargar.
  private readonly moduloPreseleccionado = this.route.snapshot.queryParamMap.get('modulo');

  protected readonly temaLogin = inject(TemaLoginService);
  protected readonly temaPagina = inject(TemaPaginaService);

  protected readonly dominioBase = DOMINIO_BASE;
  protected readonly editandoIdentificador = signal(false);
  protected readonly paso = signal<PasoWizard>(1);
  protected readonly opcionesLogin = OPCIONES_TEMA_LOGIN;
  protected readonly opcionesPagina = OPCIONES_TEMA_PAGINA;

  protected readonly modulos = signal<Modulo[]>([]);
  protected readonly cargandoModulos = signal(true);
  protected readonly errorModulos = signal(false);
  protected readonly modulosSeleccionados = signal<string[]>([]);

  protected readonly logoDataUrl = signal<string | null>(null);
  protected readonly errorLogo = signal<string | null>(null);
  protected readonly colorPrimario = signal('#2563eb');
  protected readonly colorSecundario = signal('#1e3a5f');
  protected readonly paletasPredefinidas = PALETAS_PREDEFINIDAS;

  protected readonly creando = signal(false);
  protected readonly errorCreacion = signal<string | null>(null);
  protected readonly resultado = signal<RegistrarEmpresaResponse | null>(null);

  // Si el usuario edita el identificador a mano, dejamos de regenerarlo
  // automaticamente a partir del nombre.
  private identificadorTocadoManualmente = false;

  protected readonly form = this.fb.nonNullable.group({
    nombreLegal: ['', [Validators.required, Validators.maxLength(200)]],
    nombreComercial: ['', [Validators.maxLength(200)]],
    identificador: [
      '',
      [Validators.required, Validators.minLength(3), Validators.maxLength(40), Validators.pattern(PATRON_IDENTIFICADOR)],
    ],
    dominio: ['', [Validators.maxLength(191)]],
    contrasenaMaestra: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(100)]],
  });

  constructor() {
    this.cargarModulos();
  }

  private cargarModulos(): void {
    this.adminService.getModulos().subscribe({
      next: (modulos) => {
        this.modulos.set(modulos);
        this.cargandoModulos.set(false);
        this.aplicarModuloPreseleccionado(modulos);
      },
      error: () => {
        this.modulos.set(MODULOS_RESPALDO);
        this.errorModulos.set(true);
        this.cargandoModulos.set(false);
        this.aplicarModuloPreseleccionado(MODULOS_RESPALDO);
      },
    });
  }

  private aplicarModuloPreseleccionado(modulos: Modulo[]): void {
    if (!this.moduloPreseleccionado) {
      return;
    }
    const existe = modulos.some((m) => m.codigo === this.moduloPreseleccionado);
    if (existe && !this.modulosSeleccionados().includes(this.moduloPreseleccionado)) {
      this.modulosSeleccionados.set([...this.modulosSeleccionados(), this.moduloPreseleccionado]);
    }
  }

  elegirPaleta(paleta: PaletaPredefinida): void {
    this.colorPrimario.set(paleta.primario);
    this.colorSecundario.set(paleta.secundario);
  }

  onCambiarNombre(): void {
    if (this.identificadorTocadoManualmente) {
      return;
    }
    const identificador = generarIdentificador(this.form.controls.nombreLegal.value);
    this.form.controls.identificador.setValue(identificador, { emitEvent: false });
  }

  onEditarIdentificadorManual(): void {
    this.identificadorTocadoManualmente = true;
    const normalizado = generarIdentificador(this.form.controls.identificador.value);
    this.form.controls.identificador.setValue(normalizado, { emitEvent: false });
  }

  irAPaso2(): void {
    if (this.form.invalid) {
      return;
    }
    this.paso.set(2);
  }

  estaSeleccionado(codigo: string): boolean {
    return this.modulosSeleccionados().includes(codigo);
  }

  alternarModulo(codigo: string): void {
    const actuales = this.modulosSeleccionados();
    this.modulosSeleccionados.set(
      actuales.includes(codigo) ? actuales.filter((c) => c !== codigo) : [...actuales, codigo]
    );
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
    lector.onload = () => this.logoDataUrl.set(lector.result as string);
    lector.readAsDataURL(archivo);
  }

  quitarLogo(): void {
    this.logoDataUrl.set(null);
    this.errorLogo.set(null);
  }

  crearEmpresa(): void {
    if (this.form.invalid) {
      this.paso.set(1);
      return;
    }
    this.creando.set(true);
    this.errorCreacion.set(null);

    const valores = this.form.getRawValue();
    this.registroService
      .registrar({
        identificador: valores.identificador,
        nombreLegal: valores.nombreLegal,
        nombreComercial: valores.nombreComercial || null,
        dominio: valores.dominio || null,
        contrasenaMaestra: valores.contrasenaMaestra,
        modulosSolicitados: this.modulosSeleccionados(),
      })
      .subscribe({
        next: (respuesta) => {
          this.creando.set(false);
          this.resultado.set(respuesta);

          // No hay JWT todavia (el aprovisionamiento es async), asi que el
          // logo/colores quedan pendientes hasta el primer login exitoso
          // (ver AuthService.aplicarMarcaPendienteSiExiste).
          this.marcaPendiente.guardar(valores.identificador, {
            urlLogo: this.logoDataUrl(),
            colorPrimario: this.colorPrimario(),
            colorSecundario: this.colorSecundario(),
            dominioPropio: null,
          });

          this.paso.set(5);
        },
        error: (error: HttpErrorResponse) => {
          this.creando.set(false);
          this.errorCreacion.set(this.mensajeDeError(error));
        },
      });
  }

  private mensajeDeError(error: HttpErrorResponse): string {
    const mensaje = (error.error as { mensaje?: string } | null)?.mensaje;
    if (mensaje) {
      return mensaje;
    }
    if (error.status === 409) {
      return 'Ya existe una empresa con ese identificador.';
    }
    if (error.status === 0) {
      return 'No se pudo conectar con el servidor. Verifica tu conexión e intenta de nuevo.';
    }
    return 'No se pudo crear la empresa. Intenta de nuevo.';
  }
}
