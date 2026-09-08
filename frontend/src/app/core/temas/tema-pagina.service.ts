import { Injectable, signal } from '@angular/core';

export type TemaPagina = 'clasico' | 'compacto' | 'amplio';

const CLAVE_STORAGE = 'mp_tema_pagina';
const TEMA_POR_DEFECTO: TemaPagina = 'clasico';

/**
 * Selector de estilo de las paginas generales (fuera del login), puramente
 * visual por ahora -- igual que TemaLoginService. Pendiente: hoy ninguna
 * otra pantalla lee este valor todavia; cuando se decida aplicarlo (por ejemplo
 * variando densidad/espaciado del layout base), hacerlo leyendo esta misma
 * senal en vez de duplicar el mecanismo de guardado.
 */
@Injectable({ providedIn: 'root' })
export class TemaPaginaService {
  private readonly _tema = signal<TemaPagina>(this.leerDeStorage());

  readonly tema = this._tema.asReadonly();

  elegir(tema: TemaPagina): void {
    this._tema.set(tema);
    try {
      localStorage.setItem(CLAVE_STORAGE, tema);
    } catch {
      // preferencia puramente visual; no es critico si no se puede guardar.
    }
  }

  private leerDeStorage(): TemaPagina {
    try {
      const valor = localStorage.getItem(CLAVE_STORAGE);
      if (valor === 'clasico' || valor === 'compacto' || valor === 'amplio') {
        return valor;
      }
    } catch {
      // ignorar y usar el valor por defecto
    }
    return TEMA_POR_DEFECTO;
  }
}
