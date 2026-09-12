# Módulo Omnicanal → Liwa (unificación + panel real + fidelidad visual + tiempo real)

**Fecha:** 2026-09-12 (actualizado — ver historial en `git log -- docs/frontend/modulo-omnicanal-liwa.md`)

`features/omnicanal/` (el placeholder original, sin lógica real — solo un
componente que mostraba "Soy omnicanal") se retiró por completo. El módulo
que el backend conoce como `omnicanal` (`tbl_modulos`) es en realidad **Liwa**:
analítica de conversaciones de WhatsApp con IA (ver
`docs/frontend/login-personalizado-y-config-correo.md` y la página de
detalle del módulo para la descripción de negocio completa). Todo el código
real del panel vive en `features/omnicanal-liwa/`.

Esta versión del documento cubre tres rondas de trabajo sobre el mismo
módulo: la migración original desde `guajiranet` (React/Next.js) a Angular,
una segunda pasada de **fidelidad visual pixel-a-pixel** contra ese mismo
original, y la adición de **tiempo real por WebSocket** (reemplazando el
polling) más un **seeder de datos demo**. Se referencia el proyecto original
como "GUAJIRANETISP-ERP-WEB" en los comentarios del código.

## Por qué un solo módulo, no dos

`panel/omnicanal` y `panel/omnicanal/liwa` no son dos productos — son el
mismo panel. Solo hay una carpeta, una ruta funcional (`panel/omnicanal/liwa`)
y `omnicanal/` no existe más en el árbol de `features/`.

## Estructura

```
frontend/src/app/
├── core/
│   └── realtime/
│       └── omnicanal-socket.service.ts   cliente STOMP (@stomp/stompjs) del socket de Omnicanal
├── shared/
│   └── animations/
│       └── scroll-reveal.directive.ts    directiva GSAP ScrollTrigger reutilizable (appScrollReveal)
└── features/omnicanal-liwa/
    ├── components/
    │   ├── analisis/analisis-ia-panel.component.ts   panel de analisis IA (resumen + tabla + detalle)
    │   ├── analisis-ia-resumen.component.ts
    │   ├── analisis-ia-tabla.component.ts
    │   ├── analisis-ia-detalle.component.ts
    │   ├── asesores-panel.component.ts                ranking/detalle de desempeño por asesor
    │   ├── ads-panel.component.ts                     ROI de conversaciones que vinieron de Meta Ads
    │   ├── case-reports-panel.component.ts            reportes de casos archivados (el mas grande)
    │   ├── dashboard-calidad.component.ts              indicadores tipo call center (FCR, esfuerzo, sentimiento)
    │   ├── kpis-calidad-panel.component.ts / kpi-card.component.ts
    │   ├── calidad-charts.components.ts / actividad-line-chart.component.ts
    │   ├── conversation-drawer.component.ts            vista de un chat individual
    │   ├── liwa-table.component.ts / liwa-summary-modal.component.ts
    │   └── pending-panel.component.ts                  huerfano, ver "Pendientes"
    ├── data/
    │   ├── liwa.service.ts                             unico punto de acceso HTTP de este modulo
    │   └── liwa-resumen.util.ts
    ├── models/
    │   └── liwa.model.ts                               tipos + LiwaMotivoIA/MOTIVO_IA_LABELS/RESULTADO_LABELS
    ├── shared/
    │   └── liwa-paleta.ts                              colores hex EXACTOS del original (referencia unica)
    └── pages/
        ├── detalle/omnicanal-detalle.component.ts       pagina publica "conocer la solucion"
        └── panel/omnicanal-liwa-panel.component.ts       panel real, dentro del Shell
```

## Rutas

```ts
// Publica, antes de comprar/loguearse -- carga perezosa
{
  path: 'modulos/omnicanal',
  loadComponent: () => import('.../omnicanal-liwa/pages/detalle/omnicanal-detalle.component')
    .then(m => m.OmnicanalDetalleComponent),
}

// Dentro del Shell (empresa logueada, modulo ya instalado) -- tambien perezosa,
// protegida por moduloActivoGuard('omnicanal')
{
  path: 'panel/omnicanal/liwa',
  loadComponent: () => import('.../omnicanal-liwa/pages/panel/omnicanal-liwa-panel.component')
    .then(m => m.OmnicanalLiwaPanelComponent),
  canActivate: [moduloActivoGuard('omnicanal')],
}
```

## Fidelidad visual contra el original (GUAJIRANETISP-ERP-WEB)

Se rehicieron los componentes vista por vista para igualar colores, radios,
tipografía y layout exactos del original (Next.js + Tailwind + Recharts/
Chart.js). Estado por pestaña:

| Pestaña | Estado | Notas |
|---|---|---|
| **Conversaciones** | ✅ Fiel + probado en vivo | Header, tabs, filtros de fecha, KPIs, tabla con búsqueda/filtros/**paginación de 15** (no existía antes), drawer con revelado progresivo de mensajes y resumen del contacto, gráfica de actividad real con Chart.js. |
| **Análisis y casos** | ✅ Fiel + probado en vivo | Resumen de 3 tarjetas, tabla paginada de 15 con filtros, modal de detalle con burbujas WhatsApp. `case-reports-panel` (el más grande, calco de `CaseReportsPanel.tsx`) con dona SVG clicable, mapa de calor motivo×municipio, cobertura por municipio, export CSV, modal de resumen de contacto. |
| **Gráficas** | ✅ Fiel + probado en vivo | `dashboard-calidad` + `calidad-charts`: KPIs con semáforo, barras de motivos, dona de sentimiento, mapa de calor, **scatter real** (CSAT vs FCR, antes era una tabla) y **línea multi-serie real** (tendencia por municipio, antes era una lista de barras) con Chart.js. Whitelist de municipios (`albania, hatonuevo, fonseca, san juan, villa martin, mushaisa, molino`) igual al original; casos sin municipio se cuentan aparte, no se mezclan con "Otros". |
| **KPIs** | 🟡 Visualmente correcto, no verificado a fondo | 11 tarjetas con semáforo de color; falta probar que los semáforos cambien bien al variar el rango de fechas. |
| **Asesores** | 🟡 Sin revisar a fondo esta ronda | Ya existía una traducción funcional (506 líneas vs 698 del original) de una ronda anterior; no se comparó línea por línea contra el spec ni se probó en vivo en esta pasada. |
| **Meta Ads** | 🟡 Sin revisar a fondo esta ronda | Igual que Asesores — ya bastante avanzado de antes, visto renderizar bien una vez, sin prueba exhaustiva de filtros/paginación/modal. |

**Librería de gráficas:** se unificó en **Chart.js vía `ng2-charts`** (el
original mezclaba Chart.js y Recharts). `provideCharts(withDefaultRegisterables())`
va en los `providers` de cada componente que grafica (no en `app.config.ts`),
para que la librería solo se cargue dentro del chunk lazy de este módulo y
no infle el bundle inicial (ver presupuesto en `angular.json`).

### Bug recurrente encontrado (y su porqué) — léelo antes de tocar componentes async

Esta app **no confía en que `zone.js` dispare la detección de cambios**
para callbacks de `setTimeout` ni continuaciones de `async`/`await` que no
pasan por un evento (click, ngModel) manejado directamente por Angular. El
síntoma es que el estado del componente cambia correctamente (verificable
con `ng.getComponent(el)` en consola) pero la vista nunca se actualiza.

**Todo componente que muta estado dentro de un `setTimeout` o después de un
`await` debe llamar `this.cdr.markForCheck()` explícito** (inyectando
`ChangeDetectorRef`) — es el patrón que ya usan `ads-panel.component.ts` y
la mayoría de los paneles de pestaña; dos bugs reales de esta ronda
(`conversation-drawer` pegado en "Cargando...", el modal de resumen de
contacto de `case-reports-panel` que nunca aparecía) fueron exactamente
esto. La excepción es cuando el estado vive en una **signal** (`signal()`)
en vez de una propiedad plana — las signals sí disparan CD por su cuenta
(ver `config-panel.component.ts`).

## Tiempo real (WebSocket/STOMP)

Reemplaza el polling de 20s que tenía el panel antes. Arquitectura completa:

- **Backend**: puerto `NotificadorEventosOmnicanal` (application) con un solo
  método `notificarCambio()` — el frontend no distingue tipos de evento, así
  que no hace falta más. Implementado por un adaptador STOMP
  (`NotificadorEventosOmnicanalStomp`, infrastructure) que publica en
  `/topic/empresa/{identificadorEmpresa}/omnicanal`. Se llama desde
  `RepositorioAnalisisEscritor` (cuando termina un análisis IA) y
  `RecibirConversacionArchivadaService` (cuando se archiva/actualiza una
  conversación). Endpoint STOMP configurado en
  `backend/bootstrap/.../tiemporeal/ConfiguracionWebSocket.java`, con
  autenticación por JWT en el handshake (query param, un WebSocket nativo
  no admite headers custom) y autorización por tenant (una empresa no puede
  suscribirse al topic de otra).
- **Frontend**: `core/realtime/omnicanal-socket.service.ts` (cliente
  `@stomp/stompjs`), expone una signal `revision` que sube en cada evento.
  El panel principal (`omnicanal-liwa-panel.component.ts`) se suscribe en
  `arrancarCargaDeDatos()` y reacciona con un `effect()` en el constructor;
  queda un polling de **respaldo muy espaciado (3 min)**, solo por si el
  socket se desconecta y no logra reconectar solo.

**Pendiente de verificar:** probar con dos empresas reales logueadas
simultáneamente que cada una solo reciba sus propios eventos (requiere
generar tráfico real o correr el seeder para dos empresas a la vez).

## Seeder de datos demo

`backend/omnicanal/omnicanal-infrastructure/.../persistencia/cliente/SembradorDeDatosLiwaDemo.java`
— genera conversaciones/turnos/casos/análisis realistas (motivos, municipios,
sentimientos y resultados con distribuciones creíbles) para poder probar el
panel sin tráfico real de WhatsApp. Vive en el paquete `persistencia.cliente`
a propósito, para poder usar los constructores package-private de las
entidades JPA de ese módulo.

Apagado por defecto; se activa con la property `app.seed.omnicanal.empresa`
(el identificador/slug de la empresa). Es idempotente — si la empresa ya
tiene conversaciones, no hace nada. Ejemplo (con la imagen de Docker ya
construida):

```bash
docker compose run --rm --no-deps -e APP_SEED_OMNICANAL_EMPRESA=<identificador_empresa> backend
```

Opcional: `APP_SEED_OMNICANAL_CONVERSACIONES=100` para sembrar más o menos
que las 60 por defecto.

## Animaciones

- **Scroll suave global**: `scroll-behavior: smooth` en `styles.scss` (raíz
  del frontend), respeta `prefers-reduced-motion`.
- **Entrada de pestaña**: fade + slide-up de 0.32s al cambiar de vista
  dentro del panel (clase `.vista-contenido`, CSS puro).
- **Scroll reveal con GSAP**: directiva reutilizable `appScrollReveal`
  (`shared/animations/scroll-reveal.directive.ts`, usa `gsap` +
  `ScrollTrigger`) que anima la aparición (fade + slide-up) de un elemento
  cuando entra al viewport al hacer scroll, con `[appScrollRevealDelay]`
  opcional para escalonar varias tarjetas. Aplicada en las tarjetas de
  KPIs/actividad/tabla del panel principal, las 6 tarjetas de
  `case-reports-panel` y las tarjetas de `dashboard-calidad`. Respeta
  `prefers-reduced-motion` (se salta la animación si el usuario lo tiene
  activado).

## Efecto del sidebar (fuera de este módulo, pero de la misma ronda de trabajo)

`frontend/src/app/layout/shell.component.ts` (el Shell global de la app, no
exclusivo de Liwa) tiene un efecto "spotlight" en el menú lateral: cada link
sigue al cursor con un resplandor radial al pasar el mouse
(`onSpotlight()` actualiza variables CSS `--x`/`--y` en `mousemove`, un
`::before` con `radial-gradient` las usa y solo se muestra en `:hover`).
Color de fondo del sidebar: `#101a2d` (el original de la plataforma, no se
tocó).

## Bugs encontrados y corregidos (acumulado de todas las rondas)

1. **`rutaPanel()` en `mis-modulos.component.ts` apuntaba a la ruta vieja**
   (`/panel/omnicanal` en vez de `/panel/omnicanal/liwa`) — corregido.
2. **Bug de tipos que `tsc --noEmit` no detectaba pero `ng build` sí** en
   `asesores-panel.component.ts` (indexar `MOTIVO_IA_LABELS` con un
   `string` sin acotar). **Lección:** correr `ng build` real antes de dar
   por bueno un cambio grande de UI, no solo `tsc --noEmit`.
3. **Bundle inicial pasó el límite de error** (1.13 MB contra 1 MB) por
   importar el panel de forma eager — se cambió a `loadComponent`.
4. **Drawer de conversación pegado en "Cargando..."** y **modal de resumen
   de contacto que nunca aparecía** (`case-reports-panel`) — el bug de
   `markForCheck()` documentado arriba.
5. **Color "Neutral" salía azul en vez de gris** en `case-reports-panel` —
   el mapa de colores usaba la llave `neutro`, los datos reales traen
   `neutral`.
6. **La dona "Casos por motivo" quedaba fuera del viewport** — el grid de
   2 columnas de `case-reports-panel` necesitaba `minmax(0, 1fr)` en vez de
   `1fr` para no expandirse con el contenido ancho del heatmap vecino
   (gotcha clásico de CSS Grid con tablas anchas dentro).

## Pendientes conocidos

1. **`LiwaPendingPanelComponent` sin usar** — sigue en el arreglo `imports`
   de `OmnicanalLiwaPanelComponent` pero no aparece en su plantilla
   (warning `NG8113` en cada build). Confirmar si hace falta o borrarlo.
2. **Bundle inicial en ~932 KB** (warning, límite duro 1 MB) — sigue
   creciendo con cada gráfica nueva; revisar qué más hacer lazy si sigue
   subiendo.
3. **Asesores y Meta Ads** sin verificación exhaustiva en vivo esta ronda
   (ver tabla de arriba).
4. **KPIs**: confirmar que los semáforos de color reaccionen bien a
   cambios de rango de fechas.
5. **Socket con dos empresas simultáneas** sin probar (ver sección de
   Tiempo real).
