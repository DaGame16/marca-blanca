import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ActualizarDatosPayload,
  ActualizarMarcaPayload,
  EmpresaDetalle,
} from './empresa-detalle.models';
import { EmpresaConsola } from './empresas.models';

@Injectable({ providedIn: 'root' })
export class ConsolaEmpresasService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/consola/empresas`;

  listar(): Observable<EmpresaConsola[]> {
    return this.http.get<EmpresaConsola[]>(this.base);
  }

  detalle(empresaId: string): Observable<EmpresaDetalle> {
    return this.http.get<EmpresaDetalle>(`${this.base}/${empresaId}`);
  }

  suspender(empresaId: string): Observable<void> {
    return this.http.post<void>(`${this.base}/${empresaId}/suspender`, {});
  }

  reactivar(empresaId: string): Observable<void> {
    return this.http.post<void>(`${this.base}/${empresaId}/reactivar`, {});
  }

  guardarDatos(empresaId: string, payload: ActualizarDatosPayload): Observable<void> {
    return this.http.put<void>(`${this.base}/${empresaId}/datos`, payload);
  }

  guardarMarca(empresaId: string, payload: ActualizarMarcaPayload): Observable<void> {
    return this.http.put<void>(`${this.base}/${empresaId}/marca`, payload);
  }

  activarModulo(empresaId: string, codigo: string): Observable<void> {
    return this.http.post<void>(`${this.base}/${empresaId}/modulos/${codigo}`, {});
  }

  desactivarModulo(empresaId: string, codigo: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${empresaId}/modulos/${codigo}`);
  }
}
