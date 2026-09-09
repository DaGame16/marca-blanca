import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { refreshTokenInterceptor } from './core/interceptors/refresh-token.interceptor';
import { adminInterceptor } from './core/interceptors/admin.interceptor';
import { consolaAuthInterceptor } from './core/interceptors/consola-auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([
      adminInterceptor,         // Primero: agrega X-Admin-Key a rutas /admin
      consolaAuthInterceptor,   // Segundo: token de operador en /consola/** (y corta ahí)
      authInterceptor,          // Tercero: Authorization Bearer de tenant (ignora /consola/**)
      refreshTokenInterceptor   // Cuarto: renovación de tokens de tenant
    ]))
  ]
};
