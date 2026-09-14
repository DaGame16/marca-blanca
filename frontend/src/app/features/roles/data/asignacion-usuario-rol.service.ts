import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Rol } from '../models/rol.model';

@Injectable({ providedIn: 'root' })
export class AsignacionUsuarioRolService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/usuarios`;

  listarRolesDeUsuario(usuarioUuid: string): Observable<Rol[]> {
    return this.http.get<Rol[]>(`${this.baseUrl}/${usuarioUuid}/roles`);
  }

  asignarRol(usuarioUuid: string, rolUuid: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${usuarioUuid}/roles/${rolUuid}`, {});
  }

  quitarRol(usuarioUuid: string, rolUuid: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${usuarioUuid}/roles/${rolUuid}`);
  }
}
