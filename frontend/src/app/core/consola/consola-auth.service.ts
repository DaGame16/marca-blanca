import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ConsolaCambiarContrasenaRequest,
  ConsolaLoginRequest,
  ConsolaLoginResponse,
  OperadorEnSesion,
} from './consola-auth.models';

// Claves propias, separadas de las del token de tenant (mp_*). Un operador y un
// usuario de empresa pueden convivir en el mismo navegador sin pisarse.
const TOKEN_KEY = 'consola_access_token';
const OPERADOR_KEY = 'consola_operador';

@Injectable({ providedIn: 'root' })
export class ConsolaAuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly operadorSignal = signal<OperadorEnSesion | null>(this.leerOperadorGuardado());
  readonly operador = this.operadorSignal.asReadonly();
  readonly estaAutenticado = computed(() => this.operadorSignal() !== null);

  // El backend (ConsolaAuthFilter) bloquea toda ruta distinta de
  // /api/v1/consola/auth/** mientras el token traiga pwd_temp=true. Se lee del
  // propio JWT para que sobreviva a un refresh de pagina.
  readonly debeCambiarContrasena = signal(this.leerPwdTempDelToken());

  login(request: ConsolaLoginRequest): Observable<ConsolaLoginResponse> {
    return this.http
      .post<ConsolaLoginResponse>(`${environment.apiUrl}/consola/auth/login`, request)
      .pipe(tap((res) => this.guardarSesion(res)));
  }

  cambiarContrasena(request: ConsolaCambiarContrasenaRequest): Observable<void> {
    return this.http
      .post<void>(`${environment.apiUrl}/consola/auth/cambiar-contrasena`, request)
      .pipe(tap(() => this.debeCambiarContrasena.set(false)));
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(OPERADOR_KEY);
    this.operadorSignal.set(null);
    this.debeCambiarContrasena.set(false);
    this.router.navigateByUrl('/consola/login');
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  private guardarSesion(res: ConsolaLoginResponse): void {
    localStorage.setItem(TOKEN_KEY, res.token);
    const operador: OperadorEnSesion = {
      operadorId: res.operadorId,
      correo: res.correo,
      rol: res.rol,
    };
    localStorage.setItem(OPERADOR_KEY, JSON.stringify(operador));
    this.operadorSignal.set(operador);
    this.debeCambiarContrasena.set(res.debeCambiarContrasena);
  }

  private leerOperadorGuardado(): OperadorEnSesion | null {
    const crudo = localStorage.getItem(OPERADOR_KEY);
    return crudo ? (JSON.parse(crudo) as OperadorEnSesion) : null;
  }

  // Decodifica el payload del JWT (sin validar la firma -- eso ya lo hizo el
  // backend) solo para leer pwd_temp y decidir a donde navegar.
  private leerPwdTempDelToken(): boolean {
    const token = this.getToken();
    if (!token) {
      return false;
    }
    try {
      const payloadBase64Url = token.split('.')[1];
      const payloadJson = atob(payloadBase64Url.replaceAll('-', '+').replaceAll('_', '/'));
      const payload = JSON.parse(payloadJson) as { pwd_temp?: boolean };
      return payload.pwd_temp === true;
    } catch {
      return false;
    }
  }
}
