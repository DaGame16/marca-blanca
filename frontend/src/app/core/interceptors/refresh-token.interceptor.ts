import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../auth/auth.service';

/**
 * Interceptor que maneja automáticamente la renovación de tokens cuando expiran.
 * Si una petición falla con 401, intenta renovar el token y reintentar la petición original.
 */
export const refreshTokenInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: unknown) => {
      // Solo manejamos errores HTTP 401 (Unauthorized)
      if (!(error instanceof HttpErrorResponse) || error.status !== 401) {
        return throwError(() => error);
      }

      // No intentamos renovar si la petición ya es de login o refresh
      const isAuthEndpoint = req.url.includes('/auth/login') || req.url.includes('/auth/refresh');
      if (isAuthEndpoint) {
        return throwError(() => error);
      }

      // Intentamos renovar el token
      return authService.refresh().pipe(
        switchMap(() => {
          // Token renovado exitosamente, reintentar la petición original
          const token = authService.getToken();
          if (!token) {
            authService.logout();
            return throwError(() => error);
          }

          // Clonar la petición con el nuevo token
          const clonedReq = req.clone({
            setHeaders: { Authorization: `Bearer ${token}` },
          });
          return next(clonedReq);
        }),
        catchError((refreshError) => {
          // Si falla la renovación, hacer logout
          authService.logout();
          return throwError(() => refreshError);
        })
      );
    })
  );
};
