# Onboarding de configuración del módulo Liwa

**Fecha:** 2026-09-11

Antes, al abrir el panel de Liwa sin tener el token configurado, se veía el
dashboard vacío con un banner "No se pudo cargar el reporte" — la
configuración era una pestaña más entre las otras 6, fácil de no notar. Se
cambió a un flujo de dos partes:

## 1. Onboarding automático dentro del panel

`OmnicanalLiwaPanelComponent.ngOnInit` ahora primero llama a
`liwa.obtenerConfig()` y revisa `liwaTokenConfigurado`:

- **Sin token** → se muestra únicamente `LiwaConfigPanelComponent` (ni
  tabs, ni filtros de fecha, ni llamadas a `/chats`/`/analisis` que de
  todas formas iban a fallar). Cuando `LiwaConfigPanelComponent` guarda un
  token por primera vez, emite su nuevo `Output() guardado` y el panel
  pasa a la vista normal sin recargar la página (`alConfigurar()`).
- **Con token** → arranca todo como antes (carga de chats, polling cada
  20s, tabs de Conversaciones/Análisis/Gráficas/KPIs/Asesores/Ads).

La pestaña "Configuración" que vivía dentro del `<nav class="tabs">` se
quitó — dejaba de tener sentido tenerla ahí una vez que el onboarding la
maneja automáticamente y existe el acceso permanente del punto 2.

## 2. Acceso permanente desde el menú lateral

Nueva entrada **"Configuración de Liwa"** en la sección `CONFIGURACIÓN` del
sidebar (`layout/shell.component.ts`), junto a "Identidad de marca" y
"Experiencia de acceso" — mismo lugar donde el usuario ya espera encontrar
ajustes de la plataforma, no escondida dentro de un módulo específico.

- Ruta nueva: `panel/omnicanal/liwa/config`, protegida por el mismo
  `moduloActivoGuard('omnicanal')` que ya protegía el panel.
- Página nueva y delgada:
  `features/omnicanal-liwa/pages/config/omnicanal-liwa-config-page.component.ts`
  — solo un título + `<app-liwa-config-panel />`. El componente de
  configuración en sí (`components/config-panel.component.ts`) se
  reutiliza tal cual, sin duplicar código entre el onboarding y este
  acceso directo.

## Por qué dos entradas al mismo componente en vez de una sola

El onboarding (dentro de `panel/omnicanal/liwa`) tiene que aparecer
automáticamente la primera vez, sin que el usuario sepa que existe una
ruta de configuración. El acceso del sidebar tiene que estar disponible
**siempre**, incluso con el módulo ya configurado (para rotar el secreto
del webhook o cambiar el modelo de IA más adelante). Son dos necesidades
distintas resueltas con el mismo componente de UI, no dos flujos separados.
