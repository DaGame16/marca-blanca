# Estructura Base del Frontend Web — Plataforma Marca Blanca

**Estado:** estructura base aprobada y fusionada a `develop` (repositorio `desarrolloneider/marca-blanca`, carpeta `frontend/`).
**Alcance de este documento:** describe la organización de carpetas y archivos del frontend Angular tal como quedó tras el merge de la PR de estructura base. No documenta lógica de negocio de Usuarios ni Omnicanal — esos módulos existen solo como directorios vacíos, listos para implementación.

---

## 1. Propósito y alcance

Este documento describe **cómo está organizado el código del frontend**, no qué hace cada módulo (eso vendrá cuando cada feature tenga contenido real). Su objetivo es que cualquier persona que se una al desarrollo del frontend entienda, sin necesidad de explicación verbal:

- Dónde va cada tipo de archivo
- Qué convenciones de nombres sigue el proyecto
- Qué está construido, qué está vacío a propósito, y qué quedó pendiente de decisión

## 2. Stack técnico base

| Aspecto | Valor |
|---|---|
| Framework | Angular 22 |
| Gestor de paquetes | pnpm |
| Estilo de componentes | Standalone (sin `NgModule`) |
| Manejo de estado | Angular Signals |
| Librería de UI | Angular Material + CDK |
| Lenguaje de estilos | SCSS |

## 3. Árbol de carpetas — `frontend/src/app/`

```
src/app/
├── core/
│   ├── auth/
│   │   ├── auth.guard.ts          → protege rutas sin sesión activa
│   │   ├── auth.interceptor.ts    → agrega el JWT a cada petición saliente
│   │   ├── auth.service.ts        → estado de sesión (signals), login/registro/logout
│   │   └── models.ts              → tipos de autenticación (AuthResponse, LoginRequest, etc.)
│   │
│   ├── guards/
│   │   └── auth.guard.ts          → variante generada durante el scaffolding inicial
│   │
│   ├── interceptors/
│   │   └── auth.interceptor.ts    → variante generada durante el scaffolding inicial
│   │
│   └── services/
│       └── session.service.ts     → variante generada durante el scaffolding inicial
│
├── shared/
│   └── components/                → componentes reutilizables entre features (vacío por ahora)
│
├── features/
│   ├── auth/
│   │   ├── login/
│   │   │   └── login.component.ts     → formulario de login con Angular Material
│   │   ├── pages/
│   │   │   └── login/
│   │   │       ├── login.html
│   │   │       ├── login.scss
│   │   │       ├── login.spec.ts
│   │   │       └── login.ts           → variante generada durante el scaffolding inicial
│   │   └── register/
│   │       └── register.component.ts  → formulario de registro con Angular Material
│   │
│   ├── home/
│   │   └── home.component.ts          → página de inicio (no enrutada actualmente)
│   │
│   ├── tareas/                        → feature de REFERENCIA, funcional, fuera del alcance del piloto
│   │   ├── data/
│   │   │   └── tarea.service.ts
│   │   ├── models/
│   │   │   └── tarea.model.ts
│   │   └── pages/
│   │       ├── detalle-tarea/
│   │       └── lista-tareas/
│   │
│   ├── usuarios/                      → módulo del piloto actual — estructura vacía
│   │   ├── data/.gitkeep
│   │   ├── models/.gitkeep
│   │   └── pages/.gitkeep
│   │
│   ├── omnicanal/                     → módulo del piloto actual — estructura vacía
│   │   ├── data/.gitkeep
│   │   ├── models/.gitkeep
│   │   └── pages/.gitkeep
│   │
│   └── 3cx/                           → integración futura — estructura vacía
│       ├── data/.gitkeep
│       ├── models/.gitkeep
│       └── pages/.gitkeep
│
├── layout/
│   └── shell.component.ts             → layout con toolbar (Material) — NO enrutado actualmente
│
├── app.component.ts                   → componente raíz ACTIVO (referenciado por main.ts)
├── app.config.ts                      → providers de la aplicación (router, HttpClient, interceptor)
├── app.routes.ts                      → definición de rutas de la aplicación
├── app.html / app.scss / app.spec.ts / app.ts   → residuo del scaffolding inicial, sin uso
```

## 4. Convención de nomenclatura de archivos

El proyecto usa **kebab-case** para nombres de archivo, con clases internas en **PascalCase**:

```
tarea.service.ts   →  export class TareaService
lista-tareas.ts    →  export class ListaTareas
```

**Nota de convención:** conviven dos estilos de sufijo en el árbol actual — algunos archivos llevan el sufijo de tipo en el nombre (`auth.service.ts`, `login.component.ts`) y otros lo omiten, siguiendo la convención más reciente del CLI de Angular 22 (`session.service.ts` con clase `Session`, `login.ts` con clase `Login`). Esta mezcla es resultado de que la estructura fue construida en dos momentos distintos por dos personas; no se ha unificado un único estándar todavía.

## 5. Organización por feature

Cada módulo de negocio sigue el mismo patrón interno, independientemente de si tiene contenido o está vacío:

```
features/<nombre-del-feature>/
├── data/       → servicios que llaman a la API (único punto de acceso a HttpClient)
├── models/     → interfaces y enums TypeScript del feature
└── pages/      → componentes de página, uno por responsabilidad
```

Regla de dependencia: ningún componente de `pages/` debe importar `HttpClient` directamente — solo el servicio de `data/` de su propio feature.

## 6. Duplicación existente en `core/` y `features/auth/`

El árbol actual contiene **dos implementaciones paralelas** de autenticación, resultado de que dos personas trabajaron el mismo módulo en momentos distintos sin coordinarse de antemano:

| Conjunto | Ubicación | Estado |
|---|---|---|
| A (activo) | `core/auth/*`, `features/auth/login/login.component.ts`, `features/auth/register/` | Usa Angular Material, localStorage para el token, es el que compila y corre hoy |
| B (residual) | `core/guards/`, `core/interceptors/`, `core/services/session.service.ts`, `features/auth/pages/login/login.ts` | Generado en el scaffolding inicial, sin Material, basado en signals puros; no está conectado a `app.config.ts` actual |

**Esto no está resuelto** — ambos conjuntos coexisten en el repositorio. Se recomienda decidir cuál conservar y eliminar el otro en una tarea de limpieza aparte, antes de que el módulo de autenticación crezca más.

## 7. Componentes sin enrutar

Dos piezas existen en el código pero no están conectadas a ninguna ruta activa:

- **`layout/shell.component.ts`** — layout con `MatToolbar`, `MatButton` e `MatIcon`. Pendiente de confirmar si se integrará como envoltorio visual de las páginas internas (header/navegación) o si es código en desuso. *Esta pregunta quedó abierta en la revisión de la PR de estructura base; no se ha confirmado su resolución al momento de escribir este documento.*
- **`features/home/home.component.ts`** — página de inicio sin ruta asignada.

## 8. Archivos residuales sin uso

`app.html`, `app.scss`, `app.spec.ts` y `app.ts` en la raíz de `app/` corresponden al scaffolding inicial (un componente raíz alternativo, más simple, sin Material). El componente raíz real es `app.component.ts`, referenciado desde `main.ts`. Estos archivos no rompen la compilación pero no cumplen ninguna función; son candidatos a eliminación en una limpieza posterior.

## 9. Estado de cada módulo respecto al piloto actual

| Módulo | Contenido | Alcance del piloto |
|---|---|---|
| `usuarios/` | Vacío (solo estructura) | ✅ Dentro del alcance — pendiente de construir |
| `omnicanal/` | Vacío (solo estructura) | ✅ Dentro del alcance — pendiente de construir |
| `3cx/` | Vacío (solo estructura) | Integración futura, no es parte del piloto actual |
| `tareas/` | Funcional (servicio, modelo, 2 páginas) | ❌ Fuera del alcance del piloto — se construyó como feature de referencia para validar el patrón de carpetas antes de que se confirmara el alcance real; se conserva en el repositorio a decisión del equipo |
| `auth/` | Funcional (dos implementaciones, ver sección 6) | Soporte transversal, no es un módulo del piloto en sí |

## 10. Pendientes conocidos (no resueltos en este documento)

1. Decidir cuál de las dos implementaciones de autenticación se conserva (sección 6).
2. Confirmar el destino de `shell.component.ts` (sección 7).
3. Eliminar los archivos residuales del scaffolding inicial (sección 8).
4. Definir el tipo `TipoTarea` (referenciado como pendiente en la documentación TO-BE original, sin definición formal) si el feature `tareas/` se mantiene.
