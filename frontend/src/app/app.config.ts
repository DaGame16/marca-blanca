import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { refreshTokenInterceptor } from './core/interceptors/refresh-token.interceptor';
import { adminInterceptor } from './core/interceptors/admin.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([
      adminInterceptor,        // Primero: agrega X-Admin-Key a rutas /admin
      authInterceptor,         // Segundo: agrega Authorization Bearer
      refreshTokenInterceptor  // Tercero: maneja renovación de tokens
    ]))
  ]
};
