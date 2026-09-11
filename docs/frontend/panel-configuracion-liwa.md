# Pantalla de configuración del módulo Liwa

**Fecha:** 2026-09-11

`LiwaService` ya tenía desde su traducción los métodos HTTP para configurar
el módulo (`obtenerConfig`, `actualizarConfig`, `guardarTokenLiwa`,
`eliminarTokenLiwa`, `rotarSecretoWebhook`, contra
`/api/v1/omnicanal/config`, ver `OmnicanalConfigController` en el backend)
con un comentario explícito: *"Sirven de base para una futura pantalla de
Configuración -- no construida todavía"*. Esta es esa pantalla.

## Dónde vive

Nueva pestaña **"Configuración"** en el panel de Liwa
(`panel/omnicanal/liwa`), junto a Conversaciones/Análisis/Gráficas/KPIs/
Asesores/Meta Ads — mismo patrón de pestañas (`ngSwitch` sobre `vista`) que
ya usaba el panel, no una ruta aparte.

- Componente: `features/omnicanal-liwa/components/config-panel.component.ts`
  (`LiwaConfigPanelComponent`).
- Consume `VistaConfig` / `ActualizarConfigPayload` (`models/liwa.model.ts`),
  ya existentes.

## Qué permite configurar

1. **Inteligencia artificial** — activar/desactivar el análisis automático
   de casos archivados, y el modelo de OpenAI a usar.
2. **Integración con Liwa** — URL base del bot, el custom field que marca
   leads de Meta Ads, y el **token de autenticación** (se guarda/elimina en
   su propio endpoint, no viaja en el PUT general de configuración — nunca
   se vuelve a mostrar en texto plano una vez guardado, solo un badge de
   "Token configurado" / "Sin token").
3. **Webhook** — URL y secreto que Liwa usa para avisar cuando archiva una
   conversación nueva (de solo lectura, con botón de copiar y de mostrar/
   ocultar el secreto) y un botón para **rotarlo** (con confirmación —
   invalida el anterior de inmediato).

## Decisiones de implementación

- El campo de token es un `<input type="password">` que se limpia después
  de guardar — nunca se rellena con el valor existente (el backend tampoco
  lo devuelve, según el contrato `VistaConfig.liwaTokenConfigurado: boolean`).
- Guardar la config general (IA/modelo/URL/custom field) y guardar/rotar el
  token/secreto son **operaciones independientes**, cada una con su propio
  estado de carga y mensaje — evita que rotar el secreto por accidente
  dispare un guardado del resto del formulario o viceversa.
- `copiar()` usa `navigator.clipboard` directo (sin librería) — funciona en
  cualquier navegador moderno servido por HTTPS (o `localhost` en dev).

## Pendiente

- No hay validación de formato para "URL base de Liwa" más allá de lo que
  ya valide el backend — se podría agregar un `Validators.pattern` si
  llegan URLs mal escritas con frecuencia.
