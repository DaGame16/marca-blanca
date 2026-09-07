import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  ActualizarPerfilRequest,
  ActualizarUsuarioRequest,
  CrearUsuarioRequest,
  Usuario,
} from '../models/usuario.model';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/usuarios`;

  listar(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(this.baseUrl);
  }

  consultar(uuid: string): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.baseUrl}/${uuid}`);
  }

  crear(request: CrearUsuarioRequest): Observable<Usuario> {
    return this.http.post<Usuario>(this.baseUrl, request);
  }

  actualizar(uuid: string, request: ActualizarUsuarioRequest): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.baseUrl}/${uuid}`, request);
  }

  activar(uuid: string): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/${uuid}/activar`, {});
  }

  desactivar(uuid: string): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/${uuid}/desactivar`, {});
  }

  actualizarPerfil(uuid: string, request: ActualizarPerfilRequest): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/${uuid}/perfil`, request);
  }
}
