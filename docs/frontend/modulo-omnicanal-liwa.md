# Módulo Omnicanal → Liwa (unificación + panel real)

**Fecha:** 2026-09-10

`features/omnicanal/` (el placeholder original, sin lógica real — solo un
componente que mostraba "Soy omnicanal") se retira por completo. El módulo
que el backend conoce como `omnicanal` (`tbl_modulos`) es en realidad **Liwa**:
analítica de conversaciones de WhatsApp con IA (ver
`docs/frontend/login-personalizado-y-config-correo.md` y la página de
detalle del módulo para la descripción de negocio completa). Todo el código
real del panel vive ahora en `features/omnicanal-liwa/`.

## Por qué un solo módulo, no dos

`panel/omnicanal` y `panel/omnicanal/liwa` no son dos productos — son el
mismo panel. Antes existían dos carpetas (`omnicanal/` con el placeholder,
`omnicanal-liwa/` con el panel real) y era fácil que alguien navegara a la
ruta vieja y viera el placeholder en vez del panel de verdad (justo el bug
que motivó este documento — ver "Bugs encontrados y corregidos" abajo).
Ahora solo hay una carpeta, una ruta funcional (`panel/omnicanal/liwa`) y
`omnicanal/` no existe más en el árbol de `features/`.

## Estructura

```
frontend/src/app/features/omnicanal-liwa/
├── components/
│   ├── analisis/analisis-ia-panel.component.ts   panel de analisis IA (resumen + tabla + detalle)
│   ├── analisis-ia-resumen.component.ts
│   ├── analisis-ia-tabla.component.ts
│   ├── analisis-ia-detalle.component.ts
│   ├── asesores-panel.component.ts                ranking/detalle de desempeño por asesor
│   ├── ads-panel.component.ts                     ROI de conversaciones que vinieron de Meta Ads
│   ├── case-reports-panel.component.ts            reportes de casos archivados
│   ├── dashboard-calidad.component.ts              indicadores tipo call center (FCR, esfuerzo, sentimiento)
│   ├── kpis-calidad-panel.component.ts / kpi-card.component.ts
│   ├── calidad-charts.components.ts / actividad-line-chart.component.ts
│   ├── conversation-drawer.component.ts            vista de un chat individual
│   ├── liwa-table.component.ts / liwa-summary-modal.component.ts
│   └── pending-panel.component.ts
├── data/
│   ├── liwa.service.ts                             unico punto de acceso HTTP de este modulo
│   └── liwa-resumen.util.ts
├── models/
│   └── liwa.model.ts                               tipos + LiwaMotivoIA/MOTIVO_IA_LABELS/RESULTADO_LABELS
└── pages/
    ├── detalle/omnicanal-detalle.component.ts       pagina publica "conocer la solucion" (ver shared/components/modulo-detalle)
    └── panel/omnicanal-liwa-panel.component.ts       panel real, ya instalado, dentro del Shell
```

## Rutas

```ts
// Publica, antes de comprar/loguearse -- carga perezosa (no infla el bundle inicial)
{
  path: 'modulos/omnicanal',
  loadComponent: () => import('.../omnicanal-liwa/pages/detalle/omnicanal-detalle.component')
    .then(m => m.OmnicanalDetalleComponent),
}

// Dentro del Shell (empresa logueada, modulo ya instalado) -- tambien perezosa,
// y protegida por moduloActivoGuard('omnicanal') (ver mas abajo)
{
  path: 'panel/omnicanal/liwa',
  loadComponent: () => import('.../omnicanal-liwa/pages/panel/omnicanal-liwa-panel.component')
    .then(m => m.OmnicanalLiwaPanelComponent),
  canActivate: [moduloActivoGuard('omnicanal')],
}
```

`core/guards/modulo-activo.guard.ts` es nuevo: un `CanActivateFn` factory
(`moduloActivoGuard(codigo)`) que redirige a `/mis-modulos` si la empresa no
tiene ese módulo activo — evita que alguien entre al panel de Liwa por URL
directa sin haberlo instalado.

## Bugs encontrados y corregidos en esta migración

1. **`rutaPanel()` en `mis-modulos.component.ts` apuntaba a la ruta vieja.**
   Devolvía `/panel/omnicanal` para el código `omnicanal`, pero esa ruta ya
   no existe (solo `/panel/omnicanal/liwa`) — el botón "Abrir" del módulo
   instalado llevaba a una URL que no coincidía con nada, y Angular caía en
   el wildcard de vuelta al home. Corregido: `omnicanal` → `/panel/omnicanal/liwa`
   (igual que ya hacía `3cx` → `/panel/pbx-3cx`).

2. **Bug de tipos que `tsc --noEmit` no detectaba pero `ng build` sí rechazaba**
   en `asesores-panel.component.ts`: `MOTIVO_IA_LABELS[a.motivoContacto]`
   indexaba un `Record<LiwaMotivoIA, string>` con un `string` sin acotar
   (`motivoContacto` es texto libre que devuelve la IA, no siempre calza con
   una de las llaves conocidas — ver el comentario en `liwa.model.ts`).
   Se extrajo un método `etiquetaMotivo(motivo)` que hace el cast explícito
   y devuelve un texto de respaldo si no hay match, en vez de indexar
   directo en la plantilla.
   **Lección**: `npx tsc --noEmit -p tsconfig.app.json` (lo que se corre en
   este repo para validar antes de desplegar) **no** replica todas las
   verificaciones del compilador de plantillas de Angular — un error de
   este tipo solo aparece con `ng build`. Antes de dar por bueno un cambio
   grande de UI, correr el build real, no solo el typecheck.

3. **Typo de CSS** en el mismo archivo: `overflow-x-auto;` (sin dos puntos,
   sin valor) en vez de `overflow-x: auto;` — `ng build` lo señala como
   error de sintaxis CSS.

4. **Bundle inicial pasó el límite de error** (1.13 MB contra un tope de
   1 MB en `angular.json`) porque el panel de Liwa completo (dashboards,
   gráficas, varios componentes pesados) se importaba de forma **eager**
   (`component:` directo) en `app.routes.ts` en vez de perezosa. Se cambió
   a `loadComponent` para las dos rutas de este módulo (detalle y panel),
   igual que ya hace `consola/` — bajó el bundle inicial a ~929 KB.

## Pendiente

- Warning (no bloqueante) de compilación: `LiwaPendingPanelComponent` está
  en el arreglo `imports` de `OmnicanalLiwaPanelComponent` pero no se usa en
  su plantilla — confirmar si el componente todavía hace falta o se puede
  quitar del arreglo.
- El presupuesto de bundle inicial (`angular.json`, `maximumWarning: 500kb`)
  sigue mostrando warning (~929 KB) aunque ya no falla el build — si la app
  sigue creciendo, va a valer la pena revisar qué más se puede volver
  perezoso (o subir el warning threshold conscientemente).
