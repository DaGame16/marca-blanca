import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../../../environments/environment';

/**
 * Interceptor que agrega el header X-Admin-Key a las peticiones de administración.
 * 
 * Solo se aplica a rutas que empiecen con /api/v1/admin/
 * 
 * Este es un mecanismo de protección interino hasta que se implemente
 * un sistema completo de identidad para administradores de plataforma.
 * 
 * Nota: La clave debe configurarse en las variables de entorno y
 * nunca debe estar hardcodeada en producción.
 */
export const adminInterceptor: HttpInterceptorFn = (req, next) => {
  // Solo intercepta requests a rutas de admin
  if (req.url.includes('/api/v1/admin/')) {
    const cloned = req.clone({
      setHeaders: {
        'X-Admin-Key': environment.adminKey
      }
    });
    return next(cloned);
  }

  return next(req);
};
