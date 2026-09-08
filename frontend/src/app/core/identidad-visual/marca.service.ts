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

  // Publico (sin JWT) -- lo usa la pantalla de login, que todavia no tiene
  // sesion, para pintar el logo/colores/variante de la empresa del
  // subdominio (ver MarcaPublicaController, backend).
  obtenerPublica(identificadorEmpresa: string): Observable<MarcaDeEmpresa> {
    return this.http.get<MarcaDeEmpresa>(`${environment.apiUrl}/empresas/${identificadorEmpresa}/marca`);
  }
}
