import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Permiso } from '../models/permiso.model';

@Injectable({ providedIn: 'root' })
export class PermisoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/permisos`;

  listar(): Observable<Permiso[]> {
    return this.http.get<Permiso[]>(this.baseUrl);
  }
}
