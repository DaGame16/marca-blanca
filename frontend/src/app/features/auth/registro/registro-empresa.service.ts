import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { RegistrarEmpresaRequest, RegistrarEmpresaResponse } from './registro-empresa.models';

// Llama a POST /api/v1/admin/empresas -- va bajo /admin, asi que el
// adminInterceptor le agrega el header X-Admin-Key automaticamente (mismo
// mecanismo interino que ya usa AdminService para el catalogo de modulos).
@Injectable({ providedIn: 'root' })
export class RegistroEmpresaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/admin/empresas`;

  registrar(request: RegistrarEmpresaRequest): Observable<RegistrarEmpresaResponse> {
    return this.http.post<RegistrarEmpresaResponse>(this.baseUrl, request);
  }
}
