import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Modulo, ModuloDeEmpresa } from './models';

/**
 * Servicio para administración de módulos de empresas.
 * Requiere header X-Admin-Key para autenticación (manejado por AdminInterceptor).
 */
@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/admin`;

  /**
   * Obtiene el catálogo completo de módulos disponibles en la plataforma.
   */
  getModulos(): Observable<Modulo[]> {
    return this.http.get<Modulo[]>(`${this.apiUrl}/modulos`);
  }

  /**
   * Obtiene los módulos de una empresa específica con su estado (activo/inactivo).
   * @param empresaId UUID de la empresa
   */
  getModulosDeEmpresa(empresaId: string): Observable<ModuloDeEmpresa[]> {
    return this.http.get<ModuloDeEmpresa[]>(`${this.apiUrl}/empresas/${empresaId}/modulos`);
  }

  /**
   * Activa un módulo para una empresa.
   * La operación es idempotente - activar un módulo ya activo no genera error.
   * @param empresaId UUID de la empresa
   * @param codigo Código del módulo (ej: "usuarios", "omnicanal")
   */
  activarModulo(empresaId: string, codigo: string): Observable<void> {
    return this.http.post<void>(
      `${this.apiUrl}/empresas/${empresaId}/modulos/${codigo}/activar`,
      null
    );
  }

  /**
   * Desactiva un módulo para una empresa.
   * La operación es idempotente - desactivar un módulo ya inactivo no genera error.
   * @param empresaId UUID de la empresa
   * @param codigo Código del módulo
   */
  desactivarModulo(empresaId: string, codigo: string): Observable<void> {
    return this.http.post<void>(
      `${this.apiUrl}/empresas/${empresaId}/modulos/${codigo}/desactivar`,
      null
    );
  }
}
