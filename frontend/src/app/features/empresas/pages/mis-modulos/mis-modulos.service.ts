import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { ModuloDeEmpresa } from '../../../../core/admin/models';

// Self-service (JWT normal, no X-Admin-Key): la empresa se resuelve en el
// backend a partir del token, nunca de un parametro que mande el cliente
// (ver MisModulosController, modulo modulos-empresa).
@Injectable({ providedIn: 'root' })
export class MisModulosService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/mi-empresa/modulos`;

  listar(): Observable<ModuloDeEmpresa[]> {
    return this.http.get<ModuloDeEmpresa[]>(this.baseUrl);
  }

  activar(codigo: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${codigo}/activar`, null);
  }

  desactivar(codigo: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${codigo}/desactivar`, null);
  }
}
