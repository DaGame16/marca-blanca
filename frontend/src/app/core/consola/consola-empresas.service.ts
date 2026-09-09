import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { EmpresaConsola } from './empresas.models';

@Injectable({ providedIn: 'root' })
export class ConsolaEmpresasService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/consola/empresas`;

  listar(): Observable<EmpresaConsola[]> {
    return this.http.get<EmpresaConsola[]>(this.base);
  }

  suspender(empresaId: string): Observable<void> {
    return this.http.post<void>(`${this.base}/${empresaId}/suspender`, {});
  }

  reactivar(empresaId: string): Observable<void> {
    return this.http.post<void>(`${this.base}/${empresaId}/reactivar`, {});
  }
}
