import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../core/auth/auth.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, MatToolbarModule, MatButtonModule, MatIconModule],
  template: `
    <mat-toolbar color="primary">
      <span>Mi Proyecto</span>
      <span class="spacer"></span>
      @if (auth.currentUser(); as user) {
        <span class="user">{{ user.fullName }}</span>
      }
      <button mat-icon-button (click)="auth.logout()" aria-label="Cerrar sesión">
        <mat-icon>logout</mat-icon>
      </button>
    </mat-toolbar>
    <main class="content">
      <router-outlet />
    </main>
  `,
  styles: [
    `
      .spacer { flex: 1 1 auto; }
      .user { margin-right: 12px; font-size: 14px; }
      .content { padding: 24px; }
    `,
  ],
})
export class ShellComponent {
  protected readonly auth = inject(AuthService);
}
