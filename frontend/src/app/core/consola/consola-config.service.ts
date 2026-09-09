import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ConfigCorreoPayload,
  ConfiguracionSmtp,
  ModuloCatalogo,
  ModuloPayload,
} from './config-plataforma.models';

@Injectable({ providedIn: 'root' })
export class ConsolaConfigService {
  private readonly http = inject(HttpClient);
  private readonly correoBase = `${environment.apiUrl}/consola/config-correo`;
  private readonly modulosBase = `${environment.apiUrl}/consola/modulos`;

  // --- Config de correo (SMTP) ---
  listarConfigCorreo(): Observable<ConfiguracionSmtp[]> {
    return this.http.get<ConfiguracionSmtp[]>(this.correoBase);
  }

  crearConfigCorreo(payload: ConfigCorreoPayload): Observable<ConfiguracionSmtp> {
    return this.http.post<ConfiguracionSmtp>(this.correoBase, payload);
  }

  actualizarConfigCorreo(id: string, payload: ConfigCorreoPayload): Observable<ConfiguracionSmtp> {
    return this.http.put<ConfiguracionSmtp>(`${this.correoBase}/${id}`, payload);
  }

  activarConfigCorreo(id: string): Observable<ConfiguracionSmtp> {
    return this.http.post<ConfiguracionSmtp>(`${this.correoBase}/${id}/activar`, {});
  }

  eliminarConfigCorreo(id: string): Observable<void> {
    return this.http.delete<void>(`${this.correoBase}/${id}`);
  }

  // --- Catálogo de módulos ---
  listarModulos(): Observable<ModuloCatalogo[]> {
    return this.http.get<ModuloCatalogo[]>(this.modulosBase);
  }

  crearModulo(payload: ModuloPayload): Observable<ModuloCatalogo> {
    return this.http.post<ModuloCatalogo>(this.modulosBase, payload);
  }

  actualizarModulo(id: string, payload: ModuloPayload): Observable<ModuloCatalogo> {
    return this.http.put<ModuloCatalogo>(`${this.modulosBase}/${id}`, payload);
  }

  eliminarModulo(id: string): Observable<void> {
    return this.http.delete<void>(`${this.modulosBase}/${id}`);
  }
}
