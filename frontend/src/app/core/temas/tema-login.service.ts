import { Injectable, signal } from '@angular/core';

export type TemaLogin = 'lateral' | 'centrado' | 'fondo';

const CLAVE_STORAGE = 'mp_tema_login';
const TEMA_POR_DEFECTO: TemaLogin = 'lateral';

/**
 * Selector de diseño de login, puramente visual por ahora.
 *
 * TODO: cuando exista el campo `temaLogin` en el backend (identidad-visual),
 * esto debe pasar a leerse/guardarse vía GET/PUT /marca de la empresa (igual
 * que colorPrimario, urlLogo, etc.) en vez de localStorage. Se deja aislado
 * en este servicio para que ese cambio no toque LoginComponent ni el
 * selector -- solo esta clase.
 */
@Injectable({ providedIn: 'root' })
export class TemaLoginService {
  private readonly _tema = signal<TemaLogin>(this.leerDeStorage());

  readonly tema = this._tema.asReadonly();

  elegir(tema: TemaLogin): void {
    this._tema.set(tema);
    try {
      localStorage.setItem(CLAVE_STORAGE, tema);
    } catch {
      // localStorage puede fallar (modo privado, cuotas); no es crítico para
      // esta preferencia puramente visual.
    }
  }

  private leerDeStorage(): TemaLogin {
    try {
      const valor = localStorage.getItem(CLAVE_STORAGE);
      if (valor === 'lateral' || valor === 'centrado' || valor === 'fondo') {
        return valor;
      }
    } catch {
      // ignorar y usar el valor por defecto
    }
    return TEMA_POR_DEFECTO;
  }
}
