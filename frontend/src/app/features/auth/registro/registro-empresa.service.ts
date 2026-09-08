import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  FinalizarRegistroResponse,
  PersonalizacionRequest,
  RegistrarEmpresaRequest,
  RegistrarEmpresaResponse,
} from './registro-empresa.models';

// Wizard publico de registro (AltaEmpresaController y demas controladores en
// aprovisionamiento-infrastructure/web) -- todo bajo /api/v1/registro/empresas,
// sin JWT ni X-Admin-Key: un prospecto se registra solo, sin sesion previa.
@Injectable({ providedIn: 'root' })
export class RegistroEmpresaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/registro/empresas`;

  // Paso 1: crea la empresa en estado "borrador" y devuelve el identificador
  // (subdominio) que el backend genero a partir del nombre.
  registrar(request: RegistrarEmpresaRequest): Observable<RegistrarEmpresaResponse> {
    return this.http.post<RegistrarEmpresaResponse>(this.baseUrl, request);
  }

  // Paso 2: activa un modulo sobre la empresa en borrador. Se llama una vez
  // por cada modulo que el usuario selecciono.
  activarModulo(empresaId: string, codigo: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${empresaId}/modulos/${codigo}/activar`, null);
  }

  // Pasos 3-5: guarda colores/logo/variantes de UI. Reemplazo total -- hay
  // que mandar todo lo acumulado, no solo lo que cambio.
  personalizar(empresaId: string, request: PersonalizacionRequest): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/${empresaId}/personalizacion`, request);
  }

  // Paso 6: dispara el aprovisionamiento (clonar BD, activar modulos,
  // enviar correo de bienvenida con la contraseña temporal, etc). Sin esta
  // llamada la empresa se queda en borrador para siempre.
  finalizar(empresaId: string): Observable<FinalizarRegistroResponse> {
    return this.http.post<FinalizarRegistroResponse>(`${this.baseUrl}/${empresaId}/finalizar`, null);
  }
}
