import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, RefreshRequest, RefreshResponse, UserInfo } from './models';

const TOKEN_KEY = 'mp_access_token';
const REFRESH_TOKEN_KEY = 'mp_refresh_token';
const USER_KEY = 'mp_user';
const EMPRESA_KEY = 'mp_identificador_empresa';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly currentUserSignal = signal<UserInfo | null>(this.readStoredUser());
  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isAuthenticated = computed(() => this.currentUserSignal() !== null);

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${environment.apiUrl}/auth/login`, request)
      .pipe(tap((res) => this.storeSession(res, request.identificadorEmpresa)));
  }

  refresh(): Observable<RefreshResponse> {
    const refreshToken = this.getRefreshToken();
    const identificadorEmpresa = this.getIdentificadorEmpresa();
    if (!refreshToken || !identificadorEmpresa) {
      throw new Error('No refresh token o identificadorEmpresa disponible');
    }

    const request: RefreshRequest = { refreshToken, identificadorEmpresa };
    return this.http
      .post<RefreshResponse>(`${environment.apiUrl}/auth/refresh`, request)
      .pipe(tap((res) => this.storeSession(res, identificadorEmpresa)));
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    localStorage.removeItem(EMPRESA_KEY);
    this.currentUserSignal.set(null);
    this.router.navigateByUrl('/login');
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  getIdentificadorEmpresa(): string | null {
    return localStorage.getItem(EMPRESA_KEY);
  }

  private storeSession(res: LoginResponse | RefreshResponse, identificadorEmpresa: string): void {
    localStorage.setItem(TOKEN_KEY, res.token);
    localStorage.setItem(REFRESH_TOKEN_KEY, res.refreshToken);
    localStorage.setItem(EMPRESA_KEY, identificadorEmpresa);

    const userInfo: UserInfo = { usuarioId: res.usuarioId };
    localStorage.setItem(USER_KEY, JSON.stringify(userInfo));
    this.currentUserSignal.set(userInfo);
  }

  private readStoredUser(): UserInfo | null {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? (JSON.parse(raw) as UserInfo) : null;
  }
}
