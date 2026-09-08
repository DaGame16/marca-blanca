import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    router.navigate(['/login']);
    return false;
  }

  // El backend rechaza (403) cualquier ruta protegida distinta de /auth/**
  // mientras el usuario siga con la contraseña temporal (JwtAuthFilter). Se
  // manda a cambiarla antes de dejarlo entrar a cualquier pantalla del shell.
  if (authService.debeCambiarContrasena()) {
    router.navigate(['/cambiar-contrasena']);
    return false;
  }

  return true;
};