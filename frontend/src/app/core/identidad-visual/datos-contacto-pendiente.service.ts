import { Injectable } from '@angular/core';

export interface DatosContactoPendiente {
  correo: string;
  telefono: string;
}

const PREFIJO = 'mp_contacto_pendiente_';

/**
 * Guarda, por identificador de empresa, el correo y telefono del
 * representante legal capturados en el registro -- el backend
 * (RegistrarEmpresaRequest) todavia no acepta estos campos, asi que se
 * quedan aqui hasta que el equipo de backend amplie el contrato. Mismo
 * patron que MarcaPendienteService.
 *
 * Pendiente (backend): cuando RegistrarEmpresaRequest incluya correo/telefono,
 * enviarlos directo en el POST /api/v1/admin/empresas y eliminar este
 * servicio (o usarlo solo como respaldo si el POST falla).
 */
@Injectable({ providedIn: 'root' })
export class DatosContactoPendienteService {
  guardar(identificadorEmpresa: string, datos: DatosContactoPendiente): void {
    try {
      localStorage.setItem(PREFIJO + identificadorEmpresa, JSON.stringify(datos));
    } catch {
      // no critico -- si falla, el usuario puede volver a ingresar estos datos
      // manualmente cuando el backend los soporte.
    }
  }

  obtener(identificadorEmpresa: string): DatosContactoPendiente | null {
    try {
      const raw = localStorage.getItem(PREFIJO + identificadorEmpresa);
      return raw ? (JSON.parse(raw) as DatosContactoPendiente) : null;
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
