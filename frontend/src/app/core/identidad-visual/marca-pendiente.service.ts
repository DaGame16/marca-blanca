import { Injectable } from '@angular/core';
import { MarcaDeEmpresa } from './models';

const PREFIJO = 'mp_marca_pendiente_';

/**
 * Guarda, por identificador de empresa, la marca (logo/colores) que el
 * usuario eligio durante el registro -- ANTES de que exista una sesion con
 * la que llamar a PUT /mi-empresa/marca (ese endpoint es self-service y saca
 * la empresa del JWT, y en el registro todavia no hay JWT porque el
 * aprovisionamiento es asincrono).
 *
 * AuthService revisa esto despues de cada login exitoso: si hay una marca
 * pendiente para el identificadorEmpresa con el que se acaba de loguear, la
 * aplica via MarcaService y la borra. Si el usuario no eligio logo/colores
 * en el registro, simplemente no hay nada que aplicar.
 */
@Injectable({ providedIn: 'root' })
export class MarcaPendienteService {
  guardar(identificadorEmpresa: string, marca: MarcaDeEmpresa): void {
    try {
      localStorage.setItem(PREFIJO + identificadorEmpresa, JSON.stringify(marca));
    } catch {
      // localStorage puede fallar (modo privado, cuotas, logo muy pesado);
      // no es critico -- el usuario puede configurar su marca a mano desde
      // "Mi marca" despues de iniciar sesion.
    }
  }

  obtener(identificadorEmpresa: string): MarcaDeEmpresa | null {
    try {
      const raw = localStorage.getItem(PREFIJO + identificadorEmpresa);
      return raw ? (JSON.parse(raw) as MarcaDeEmpresa) : null;
    } catch {
      return null;
    }
  }

  limpiar(identificadorEmpresa: string): void {
    try {
      localStorage.removeItem(PREFIJO + identificadorEmpresa);
    } catch {
      // ignorar
    }
  }
}
