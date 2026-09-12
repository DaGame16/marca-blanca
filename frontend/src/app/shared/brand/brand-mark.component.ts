import { Component } from '@angular/core';

// Marca (isotipo) de Marca Blanca -- reemplaza el generico <mat-icon>hub</mat-icon>
// que se usaba como logo por defecto en el sidebar, la landing, el login y el
// wizard de registro. Trazo abstracto de "red/conexion" (una L quebrada en
// nodos + un trazo suelto) en vez del icono de Material Symbols genérico.
//
// Se dimensiona igual que un mat-icon: `width`/`height` en 1em, así que
// cualquier CSS existente que fijaba `font-size` sobre el selector viejo seguía
// funcionando -- solo hubo que cambiar los selectores de tag (`mat-icon`) por
// `app-brand-mark` donde aplicaba (ver shell.component.ts / home.component.ts).
@Component({
  selector: 'app-brand-mark',
  standalone: true,
  template: `
    <svg viewBox="0 0 40 40" fill="none" stroke="currentColor" stroke-linecap="round" aria-hidden="true">
      <line x1="10" y1="8" x2="10" y2="23" stroke-width="4" />
      <line x1="12" y1="30" x2="26" y2="30" stroke-width="4" />
      <line x1="28.5" y1="10.5" x2="32" y2="7" stroke-width="3.5" />
      <circle cx="10" cy="8" r="1.8" fill="currentColor" stroke="none" />
      <circle cx="10" cy="26" r="1.6" fill="currentColor" stroke="none" />
      <circle cx="30" cy="30" r="2.2" fill="currentColor" stroke="none" />
    </svg>
  `,
  styles: [`
    :host { display: inline-flex; align-items: center; justify-content: center; line-height: 0; }
    svg { width: 1em; height: 1em; display: block; }
  `],
})
export class BrandMarkComponent {}
