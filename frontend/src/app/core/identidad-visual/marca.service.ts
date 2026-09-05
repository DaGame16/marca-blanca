import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { MarcaDeEmpresa } from './models';

// Self-service: la empresa se resuelve del JWT en el backend
// (JwtAuthFilter), nunca se manda como parametro desde aca.
@Injectable({ providedIn: 'root' })
export class MarcaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/mi-empresa/marca`;

  obtener(): Observable<MarcaDeEmpresa> {
    return this.http.get<MarcaDeEmpresa>(this.baseUrl);
  }

  actualizar(marca: MarcaDeEmpresa): Observable<void> {
    return this.http.put<void>(this.baseUrl, marca);
  }
}
