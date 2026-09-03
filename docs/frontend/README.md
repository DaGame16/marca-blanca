# Frontend — Plataforma Marca Blanca

**A cargo de:** Carlos
**Stack:** Angular 22, standalone components, Signals, Angular Material + CDK, pnpm, SCSS

Este documento describe cómo está organizado el código del frontend hoy. Se actualiza in-place cada vez que la estructura cambia — no es una foto histórica (para eso están las decisiones fechadas en `decisiones/`, cuando corresponda).

---

## 1. Árbol de carpetas — `frontend/src/app/`

```
src/app/
├── core/
│   ├── auth/
│   │   ├── auth.guard.ts          → protege rutas sin sesión activa
│   │   ├── auth.interceptor.ts    → agrega el JWT a cada petición saliente
│   │   ├── auth.service.ts        → estado de sesión (signals), login/registro/logout
│   │   └── models.ts              → tipos de autenticación
│   ├── guards/                    → variante generada durante el scaffolding inicial (ver sección 4)
│   ├── interceptors/              → idem
│   └── services/                  → idem
│
├── shared/
│   └── components/                → componentes reutilizables entre features (vacío por ahora)
│
├── features/
│   ├── auth/
│   │   ├── login/login.component.ts       → formulario de login (Angular Material)
│   │   ├── pages/login/                   → variante del scaffolding inicial (ver sección 4)
│   │   └── register/register.component.ts
│   ├── home/home.component.ts             → sin ruta asignada (ver sección 5)
│   ├── tareas/                            → feature de referencia, fuera del alcance del piloto (ver sección 6)
│   ├── usuarios/                          → módulo del piloto — estructura vacía
│   ├── omnicanal/                         → módulo del piloto — estructura vacía
│   └── 3cx/                               → integración futura — estructura vacía
│
├── layout/
│   └── shell.component.ts                 → layout con toolbar (Material) — sin enrutar (ver sección 5)
│
├── app.component.ts    → componente raíz activo (referenciado por main.ts)
├── app.config.ts        → providers de la aplicación
└── app.routes.ts        → definición de rutas
```

## 2. Convención de nomenclatura

Kebab-case para archivos, PascalCase para clases:

```
tarea.service.ts   →  export class TareaService
```

## 3. Organización por feature

```
features/<nombre>/
├── data/       → servicios que llaman a la API (único punto de acceso a HttpClient)
├── models/     → interfaces y enums TypeScript
└── pages/      → componentes de página
```

Ningún componente de `pages/` importa `HttpClient` directamente — solo el servicio de `data/` de su propio feature.

## 4. Pendiente — duplicación en `core/` y `features/auth/`

Existen dos implementaciones paralelas de autenticación, construidas en momentos distintos sin coordinación previa:

| Conjunto | Ubicación | Estado |
|---|---|---|
| Activo | `core/auth/*`, `features/auth/login/login.component.ts`, `features/auth/register/` | Usa Angular Material, localStorage para el token; es el que compila y corre hoy (referenciado desde `app.component.ts`) |
| Residual | `core/guards/`, `core/interceptors/`, `core/services/session.service.ts`, `features/auth/pages/login/login.ts` | Del scaffolding inicial, basado en signals puros, sin Material; no conectado a `app.config.ts` actual |

**Sin resolver.** Pendiente decidir cuál conservar y eliminar el otro.

## 5. Pendiente — componentes sin enrutar

- **`layout/shell.component.ts`** — layout con `MatToolbar`. Sin confirmar si se integrará como envoltorio de las páginas internas o si es código en desuso.
- **`features/home/home.component.ts`** — sin ruta asignada.

## 6. Estado de cada módulo frente al piloto actual

| Módulo | Contenido | Alcance del piloto |
|---|---|---|
| `usuarios/` | Vacío | ✅ Dentro del alcance |
| `omnicanal/` | Vacío | ✅ Dentro del alcance |
| `3cx/` | Vacío | Integración futura, fuera del piloto actual |
| `tareas/` | Funcional | ❌ Fuera del alcance — construido como feature de referencia para validar el patrón de carpetas antes de que se confirmara el alcance real; se conserva a decisión del equipo |
| `auth/` | Funcional (ver sección 4) | Soporte transversal, no es un módulo del piloto en sí |

## 7. Pendientes conocidos

1. Decidir cuál implementación de autenticación se conserva (sección 4).
2. Confirmar el destino de `shell.component.ts` (sección 5).
3. Definir el tipo `TipoTarea`, referenciado en la documentación TO-BE original sin definición formal, si `tareas/` se mantiene.
