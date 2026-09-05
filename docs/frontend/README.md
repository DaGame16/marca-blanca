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
│   │   ├── auth.guard.ts          → variante residual del scaffolding inicial, sin usar (ver sección 4)
│   │   ├── auth.interceptor.ts    → idem
│   │   ├── auth.service.ts        → estado de sesión (signals): login, refresh, logout
│   │   └── models.ts              → LoginRequest/LoginResponse/RefreshRequest/RefreshResponse/UserInfo
│   ├── admin/
│   │   ├── admin.service.ts       → catálogo de módulos y módulos por empresa (activar/desactivar)
│   │   └── models.ts              → Modulo, ModuloDeEmpresa, Empresa
│   ├── guards/
│   │   └── auth.guard.ts          → guard activo, usado en app.routes.ts
│   ├── interceptors/
│   │   ├── auth.interceptor.ts    → agrega el JWT (Authorization: Bearer) a cada petición saliente
│   │   ├── refresh-token.interceptor.ts → renueva el token cuando expira
│   │   └── admin.interceptor.ts   → agrega X-Admin-Key a peticiones a /api/v1/admin/**
│   └── services/
│       └── session.service.ts     → residual del scaffolding inicial, sin usar (ver sección 4)
│
├── shared/
│   └── components/                → componentes reutilizables entre features (vacío por ahora)
│
├── features/
│   ├── auth/
│   │   └── login/login.component.ts   → login con diseño split-screen (Angular Material)
│   ├── home/home.component.ts         → landing pública, con ruta `''` (header + hero + módulos + footer)
│   ├── admin/
│   │   └── pages/modulos-admin/modulos-admin.component.ts → panel de catálogo de módulos, ruta `admin/modulos`
│   ├── empresas/
│   │   └── pages/mis-modulos/mis-modulos.component.ts     → pantalla "Mis módulos" estilo Odoo Apps, ruta `mis-modulos`
│   ├── omnicanal/
│   │   └── pages/detalle/omnicanal-detalle.component.ts   → página de detalle del módulo, ruta `modulos/omnicanal`
│   ├── 3cx/
│   │   └── pages/detalle/pbx-3cx-detalle.component.ts     → página de detalle del módulo, ruta `modulos/pbx-3cx`
│   ├── tareas/                        → feature de referencia, fuera del alcance del piloto (ver sección 6)
│   └── usuarios/                      → módulo del piloto — estructura vacía
│
├── layout/
│   └── shell.component.ts             → layout con toolbar (Material) — sin enrutar (ver sección 5)
│
├── app.component.ts    → componente raíz activo (referenciado por main.ts)
├── app.config.ts        → providers de la aplicación (router, HttpClient + interceptores)
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

## 4. Resuelto — duplicación en `core/` y `features/auth/`

La duplicación de autenticación mencionada en versiones anteriores de este documento quedó resuelta: la implementación activa es `core/auth/auth.service.ts` (con soporte de refresh token) + `core/guards/auth.guard.ts` + `core/interceptors/auth.interceptor.ts` + `core/interceptors/refresh-token.interceptor.ts`, conectada desde `app.config.ts` y `app.routes.ts`.

`core/auth/auth.guard.ts`, `core/auth/auth.interceptor.ts` y `core/services/session.service.ts` son el remanente del scaffolding inicial (basado en signals puros, sin Material, nunca conectado a `app.config.ts`). Nada los importa hoy. **Pendiente:** borrarlos en un commit de limpieza aparte.

Las viejas `features/auth/pages/login/` y `features/auth/register/` (huérfanas, sin ruta) se eliminaron.

## 5. Pendiente — componentes sin enrutar

- **`layout/shell.component.ts`** — layout con `MatToolbar`. Sin confirmar si se integrará como envoltorio de las páginas internas o si es código en desuso.

`features/home/home.component.ts` ya tiene ruta asignada (`''`) y dejó de estar pendiente.

## 6. Estado de cada módulo frente al piloto actual

| Módulo | Contenido | Alcance del piloto |
|---|---|---|
| `usuarios/` | Vacío | ✅ Dentro del alcance |
| `omnicanal/` | Página de detalle (`pages/detalle/`) + ruta pública `modulos/omnicanal`. `data/` y `models/` aún vacíos. | ✅ Dentro del alcance — es uno de los dos módulos vendibles |
| `3cx/` | Página de detalle (`pages/detalle/`) + ruta pública `modulos/pbx-3cx`. `data/` y `models/` aún vacíos. | ✅ Dentro del alcance — es el otro módulo vendible |
| `admin/` | Panel de catálogo de módulos (`modulos-admin.component.ts`), ruta `admin/modulos`, protegido con `X-Admin-Key` (interceptor) en vez de un rol de usuario real | Herramienta interna, no pensada para el cliente final |
| `empresas/` | Pantalla "Mis módulos" (`mis-modulos.component.ts`), ruta `mis-modulos`, estilo Odoo Apps: cada empresa activa/desactiva sus propios módulos desde su propia sesión | ✅ Dentro del alcance — es el flujo de autoservicio real para el cliente |
| `tareas/` | Funcional | ❌ Fuera del alcance — construido como feature de referencia para validar el patrón de carpetas antes de que se confirmara el alcance real; se conserva a decisión del equipo |
| `auth/` | Funcional (login con diseño split-screen, refresh token) | Soporte transversal, no es un módulo del piloto en sí |

**Nota — solo Omnicanal y PBX 3CX son módulos vendibles al cliente.** `usuarios/` y `empresas/` son capacidades propias de la plataforma (gestión de usuarios, autoservicio de módulos), no productos que se ofrezcan por separado; la landing (`home.component.ts`) solo publicita Omnicanal y PBX 3CX por esa razón.

## 7. `features/empresas/pages/mis-modulos/mis-modulos.component.ts` — notas de implementación

Pantalla de autoservicio tipo "Apps" de Odoo: buscador + grilla de módulos, cada uno con botón "Instalar" o chip "Instalado ✓" (que se vuelve "Desinstalar ✕" al pasar el mouse). Usa `AdminService` (`core/admin/`) contra los mismos endpoints `/admin/...` que ya usa el panel interno, autenticados con `X-Admin-Key` vía `admin.interceptor.ts`.

**Pendiente conocido:** el ID de la empresa está hardcodeado (`empresaId = '00000000-0000-0000-0000-000000000001'`, con TODO en el código). Falta que backend defina cómo exponer el ID de la empresa del usuario logueado (¿en el JWT? ¿en `UserInfo`?) para reemplazar ese valor por el real. Hasta entonces, la ruta solo está protegida por `authGuard` (sesión activa), no por pertenencia real a la empresa.

## 8. Pendientes conocidos

1. Borrar el remanente de autenticación del scaffolding inicial: `core/auth/auth.guard.ts`, `core/auth/auth.interceptor.ts`, `core/services/session.service.ts` (sección 4).
2. Confirmar el destino de `shell.component.ts` (sección 5).
3. Definir el tipo `TipoTarea`, referenciado en la documentación TO-BE original sin definición formal, si `tareas/` se mantiene.
4. Reemplazar el `empresaId` hardcodeado en `mis-modulos.component.ts` por el de la empresa del usuario logueado (sección 7).
5. Agregar un guard real de administrador a `admin/modulos` cuando exista un sistema de roles (hoy solo depende de `X-Admin-Key`, sin control de acceso en el frontend — ver TODO en `app.routes.ts`).
