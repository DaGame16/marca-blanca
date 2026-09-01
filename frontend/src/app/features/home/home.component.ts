import { Component, inject } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [MatCardModule],
  template: `
    <mat-card>
      <mat-card-header>
        <mat-card-title>Bienvenido{{ auth.currentUser()?.fullName ? ', ' + auth.currentUser()?.fullName : '' }}</mat-card-title>
      </mat-card-header>
      <mat-card-content>
        <p>Este es el punto de partida del panel. Reemplázalo por tus features.</p>
      </mat-card-content>
    </mat-card>
  `,
})
export class HomeComponent {
  protected readonly auth = inject(AuthService);
}
