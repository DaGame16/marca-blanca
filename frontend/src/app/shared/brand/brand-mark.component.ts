import { Component } from '@angular/core';

// Isotipo de LINELCA -- reemplaza el generico <mat-icon>hub</mat-icon> que se
// usaba como logo por defecto en el sidebar, la landing, el login y el wizard
// de registro. Reconstruye el mark oficial (escuadra de lineas + rombos como
// nodos, con un rombo central mas grande como acento) definido en el logo
// aprobado por el usuario.
//
// Se dimensiona igual que un mat-icon: `width`/`height` en 1em, así que
// cualquier CSS existente que fijaba `font-size` sobre el selector viejo seguía
// funcionando -- solo hubo que cambiar los selectores de tag (`mat-icon`) por
// `app-brand-mark` donde aplicaba (ver shell.component.ts / home.component.ts).
@Component({
  selector: 'app-brand-mark',
  standalone: true,
  template: `
    <svg viewBox="0 0 40 40" fill="none" stroke="currentColor" aria-hidden="true">
      <g stroke-width="1.8">
        <path d="M9 24 V8 H26 V15" />
        <path d="M9 24 H16.5" />
        <path d="M22 23 L31.5 33" />
      </g>
      <g stroke-width="1.7">
        <rect x="6.9" y="5.9" width="4.2" height="4.2" transform="rotate(45 9 8)" />
        <rect x="15.9" y="15.9" width="7.2" height="7.2" transform="rotate(45 19.5 19.5)" />
      </g>
      <g fill="currentColor" stroke="none">
        <rect x="15" y="22.3" width="3.4" height="3.4" transform="rotate(45 16.7 24)" />
        <rect x="28.4" y="29.7" width="4.6" height="4.6" transform="rotate(45 30.7 32)" />
      </g>
    </svg>
  `,
  styles: [`
    :host { display: inline-flex; align-items: center; justify-content: center; line-height: 0; }
    svg { width: 1em; height: 1em; display: block; }
  `],
})
export class BrandMarkComponent {}
