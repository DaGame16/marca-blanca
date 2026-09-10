# Corrección de issues de SonarQube (frontend)

**Fecha:** 2026-09-10

Se corrigieron los 38 issues `OPEN` reportados por SonarQube en el frontend
(los `CLOSED` del reporte ya estaban resueltos o descartados de antes, no se
tocaron). Casi todos caían en `features/omnicanal-liwa/` — el módulo más
nuevo y más grande del frontend.

## Por categoría

### Bugs reales (severidad CRITICAL/BUG)

**`Array.sort()` sin comparador en arreglos de texto** (3 casos —
`case-reports-panel.component.ts`, `dashboard-calidad.component.ts` x2).
`sort()` sin argumentos compara por código UTF-16, no alfabéticamente — con
acentos o mayúsculas el orden queda mal. Se agregó
`.sort((a, b) => a.localeCompare(b))`.

### Bloqueantes de convención Angular (BLOCKER)

**Outputs nombrados `on...`** (`onClose`, `onSeleccionar` en 4 componentes:
`analisis-ia-detalle`, `conversation-drawer`, `liwa-summary-modal`,
`liwa-table`). La guía de Angular prohíbe ese prefijo porque confunde el
nombre del evento emitido con el nombre del manejador que normalmente lo
escucha (`(close)="onClose()"` se lee raro si el Output *también* se llama
`onClose`). Renombrados a `close` y `seleccionado` — en
`liwa-summary-modal.component.ts` específicamente a `seleccionado` (no
`seleccionar`) porque ya existía un método con ese nombre en la misma clase.
Se actualizaron los ~10 bindings de plantilla en los componentes padres que
los consumían.

### Cognitive Complexity (CRITICAL)

Dos funciones superaban el límite de 15 (17 y 32):

- `LiwaTablaAnalisisIaComponent.filtrados` — una cadena de 7 `if` dentro de
  un `.filter()`. Se extrajo cada condición a su propio método
  (`pasaFiltroResultado`, `pasaFiltroFcr`, etc.) combinados con `&&` en un
  `pasaFiltros()` que orquesta.
- `LiwaCaseReportsPanelComponent.visibles` — un `switch` implícito de 6
  ramas (`if (this.filtro === 'x')`) con lógica propia en cada una. Se
  extrajo cada rama a su propio método y se armó un mapa
  `Filtro -> predicado` (`pasaFiltroActivo`) en vez de la cadena de `if`.

En ambos casos el comportamiento es idéntico — es una extracción de
métodos, no un cambio de lógica.

### Seguridad (VULNERABILITY)

**`Math.random()` para generar ids de mensaje** en
`liwa.service.ts` (parseo de historial de WhatsApp pegado a mano). No es un
uso criptográfico real, pero Sonar lo marca igual porque no hay forma de
distinguir "id de UI" de "token de sesión" solo mirando el código. Se
cambió a `crypto.randomUUID()`, disponible en cualquier navegador moderno,
sin cambiar el formato visible del id.

### Regex con backtracking cuadrático (MAJOR)

`LINEA_MENSAJE` en `liwa.service.ts` (`/^(.+?)\s*\(([^)]+)\):\s?([\s\S]*)$/`)
tenía un grupo `.+?` que se solapaba con la clase `\s` justo después,
dándole al motor de regex más de una forma de dividir la misma entrada —
la firma clásica de ReDoS. El nombre de un turno nunca trae paréntesis
("Autor (fecha): texto"), así que se acotó el primer grupo a `[^()]+?`
(excluye `(` explícitamente) y se quitó el `\s*` redundante (el nombre ya
se recorta con `.trim()` al usarlo).

### Preferencias de API moderna (MINOR, mecánico)

- `isNaN` / `isFinite` / `parseInt` globales → `Number.isNaN` /
  `Number.isFinite` / `Number.parseInt` (evitan la coerción implícita de
  las versiones globales).
- `String#match()` sin flag `/g` → `RegExp#exec()` (mismo resultado, es la
  forma que recomienda el linter cuando no se itera sobre todas las
  coincidencias).
- `array[array.length - 1]` → `array.at(-1)`.
- `String#replace(/x/g, y)` → `String#replaceAll(x, y)` (más explícito
  sobre la intención de reemplazar todas las ocurrencias).
- Ternarios anidados (`a ? x : b ? y : z`) → funciones con `if` secuenciales
  con `return` temprano. Mismo resultado, más fácil de leer y de depurar
  con breakpoints.
- `String(valorDesconocido)` sobre un `unknown` que podía ser un objeto →
  se acota a `string | number | boolean` antes de convertir, con un valor
  de respaldo (`''` o `'sin_clasificar'` según el caso) en vez de arriesgar
  `"[object Object]"` en pantalla.
- Encadenamiento opcional (`a && a.b`, `!a || !a.b`) → `a?.b`.
- `:root` duplicado en `styles.scss` — dos bloques separados con variables
  CSS distintas; se fusionaron en uno solo.

## Verificación

- `npx tsc --noEmit -p tsconfig.app.json` — sin errores.
- `ng build --configuration production` — build exitoso (quedan 2 warnings
  preexistentes sin relación con este trabajo: presupuesto de bundle y un
  import no usado en `omnicanal-liwa-panel.component.ts`, ver
  `docs/frontend/modulo-omnicanal-liwa.md`).
- Contenedor Docker reconstruido y verificado corriendo.
