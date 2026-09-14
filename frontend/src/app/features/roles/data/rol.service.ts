import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ActualizarRolRequest, CrearRolRequest, Rol } from '../models/rol.model';
import { Permiso } from '../models/permiso.model';

@Injectable({ providedIn: 'root' })
export class RolService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/roles`;

  listar(): Observable<Rol[]> {
    return this.http.get<Rol[]>(this.baseUrl);
  }

  crear(request: CrearRolRequest): Observable<Rol> {
    return this.http.post<Rol>(this.baseUrl, request);
  }

  actualizar(uuid: string, request: ActualizarRolRequest): Observable<Rol> {
    return this.http.put<Rol>(`${this.baseUrl}/${uuid}`, request);
  }

  eliminar(uuid: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${uuid}`);
  }

  listarPermisos(uuid: string): Observable<Permiso[]> {
    return this.http.get<Permiso[]>(`${this.baseUrl}/${uuid}/permisos`);
  }

  asignarPermiso(uuid: string, permisoUuid: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${uuid}/permisos/${permisoUuid}`, {});
  }

  quitarPermiso(uuid: string, permisoUuid: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${uuid}/permisos/${permisoUuid}`);
  }
}
