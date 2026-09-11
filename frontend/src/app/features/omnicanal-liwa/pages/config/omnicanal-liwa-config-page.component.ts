import { Component } from '@angular/core';
import { LiwaConfigPanelComponent } from '../../components/config-panel.component';

// Acceso directo a la configuracion del modulo Liwa desde el menu lateral
// (seccion CONFIGURACIÓN) -- el mismo LiwaConfigPanelComponent que se
// muestra automaticamente dentro del panel cuando el modulo todavia no
// esta configurado, pero accesible en cualquier momento (ya configurado o
// no) sin tener que volver a esa pantalla de bienvenida.
@Component({
  selector: 'app-omnicanal-liwa-config-page',
  standalone: true,
  imports: [LiwaConfigPanelComponent],
  template: `
    <div class="pagina">
      <h1>Configuración de Liwa</h1>
      <p>Token de integración, webhook y ajustes de análisis con IA del módulo de conversaciones.</p>
      <app-liwa-config-panel />
    </div>
  `,
  styles: [`
    .pagina { display: flex; flex-direction: column; gap: 4px; }
    h1 { margin: 0; font-size: 1.3rem; font-weight: 800; color: #0f172a; }
    p { margin: 0 0 20px; font-size: 0.85rem; color: #64748b; }
  `],
})
export class OmnicanalLiwaConfigPageComponent {}
