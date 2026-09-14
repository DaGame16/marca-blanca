# Módulo Roles

> Resumen vivo. Se actualiza in-place. Historial de decisiones en `decisiones/`.

## 1. Qué es

Roles y permisos de los usuarios de un tenant. Bounded context propio, separado de `usuarios` (identidad) y `autenticacion` (login/JWT) — el esquema (`seguridad.tbl_roles`, `tbl_permisos`, `tbl_usuarios_roles`, `tbl_permisos_de_rol`, `tbl_permisos_de_usuario`) ya existía migrado del sistema legado; este módulo es el código que lo usa.

- **Rol**: agrupación de permisos, configurable por el tenant (CRUD completo). Un rol con `esDelSistema = true` no se puede renombrar ni eliminar.
- **Permiso**: catálogo fijo definido por la plataforma (lo que el código sabe hacer), **granular** — un permiso por acción real de un endpoint (`usuarios:crear` es distinto de `usuarios:editar`), no un genérico "gestionar" por módulo. Solo lectura desde la aplicación — se siembra por Liquibase (ver sección 5). Nombre = `modulo:accion`.
- **Permiso efectivo de un usuario** = unión de los permisos de todos sus roles, ajustada por `tbl_permisos_de_usuario` (concede o revoca uno puntual, por encima de lo que le dan sus roles).

## 2. Estructura de paquetes

roles/
- roles-domain/.../domain/
  - Rol.java, Permiso.java
  - RolNoEncontradoException, PermisoNoEncontradoException, RolYaExisteException, RolDelSistemaException, RolConUsuariosAsignadosException, UsuarioNoEncontradoException (propia de este contexto)
- roles-application/.../application/
  - GestionarRolesService, ConsultarPermisosService, GestionarAsignacionesDeUsuarioService, CalcularPermisosEfectivosService
  - port/in/GestionarRoles, ConsultarPermisos, GestionarAsignacionesDeUsuario, ObtenerPermisosEfectivosDeUsuario
  - port/out/RepositorioRoles, RepositorioPermisos, RepositorioRolesDeUsuario, RepositorioPermisosDeUsuario
- roles-infrastructure/.../infrastructure/
  - persistencia/ — entidades JPA de las 5 tablas + `UsuarioRefDeRoles` (mapeo mínimo de solo lectura de `tbl_usuarios`, igual criterio que `UsuarioRefDeAutenticacion` en `autenticacion`)
  - web/RolController, PermisoController, AsignacionUsuarioRolController, ManejadorErroresRoles
  - ConfiguracionRoles.java

Frontend (`frontend/src/app/features/roles/`):
- `models/rol.model.ts`, `permiso.model.ts`, `modulo-permiso.util.ts` (agrupa el catálogo por módulo para la UI, con título legible por módulo — ver sección 6).
- `data/rol.service.ts`, `permiso.service.ts`, `asignacion-usuario-rol.service.ts`.
- `pages/lista-roles/lista-roles.component.ts` — CRUD de roles + checklist de permisos agrupado por módulo.
- `core/guards/permiso.guard.ts` — guard de ruta reusado por todo el frontend (no es propio de este módulo, protege rutas de cualquier módulo — ver sección 7).
- `core/auth/auth.service.ts` — decodifica el claim `permisos` del JWT, expone `tienePermiso(nombre): boolean`.

## 3. Regla de dependencia

`roles.domain`/`roles.application` no dependen de ningún otro contexto (usuarios, autenticacion, empresas, etc.) — solo identifican al usuario por su `uuid`, nunca importan el tipo `Usuario`. Verificado por `ArquitecturaHexagonalTest` (`roles_dominio_y_aplicacion_no_dependen_de_otros_contextos`).

El único acoplamiento hacia `roles-application` desde otro módulo es el ACL de `autenticacion` (`AdaptadorConsultarPermisosDeUsuario`, en `autenticacion-infrastructure`) — igual patrón que `AdaptadorVerificadorDeUsuarios` hacia `usuarios-domain`.

## 4. Cómo llega la autorización al JWT y a la UI

Backend: ver [ADR de autenticación: permisos como claim del JWT](../autenticacion/decisiones/2026-09-14-0008-permisos-como-claim-del-jwt.md). En resumen: `AutenticarUsuarioService`/`RenovarTokenService` calculan los permisos efectivos del usuario (vía `ConsultarPermisosDeUsuario`) y los embeben en el claim `permisos` del JWT. `JwtAuthFilter` los convierte en `GrantedAuthority` del `SecurityContext`, y los controllers los exigen con `@PreAuthorize("hasAuthority('...')")` (`@EnableMethodSecurity` en `SecurityConfig`).

Frontend: `AuthService` decodifica ese mismo claim del JWT (sin validar firma — ya la validó el backend) y expone `tienePermiso(nombre: string): boolean`. Tres capas de uso:
1. **Guards de ruta** (`core/guards/permiso.guard.ts`, `permisoGuard('modulo:accion')`) — evita que alguien llegue a la pantalla escribiendo la URL a mano.
2. **Menú lateral** (`layout/shell.component.ts`) — cada link está envuelto en `@if (auth.tienePermiso('...'))`, así que un usuario sin el permiso ni siquiera ve la opción.
3. **Botones dentro de cada pantalla** — crear/editar/activar/desactivar quedan detrás de `@if`/`[disabled]` según el permiso puntual de esa acción (ver tabla de la sección 6).

Importante: el frontend gatea la UX, pero **la autorización real es el `@PreAuthorize` del backend** — ocultar un botón no reemplaza el chequeo del servidor, es la otra mitad (evita que alguien vea o intente algo que el backend igual le va a rechazar).

## 5. Catálogo de permisos vigente

| Módulo | Permiso | Qué protege |
|---|---|---|
| roles | `roles:leer` | `GET /api/v1/roles`, `GET /api/v1/permisos`, `GET /api/v1/usuarios/{uuid}/roles`, `GET /api/v1/usuarios/{uuid}/permisos` |
| roles | `roles:gestionar` | Crear/editar/eliminar rol, asignar/quitar permiso de un rol, asignar/quitar rol de un usuario, conceder/revocar permiso puntual |
| usuarios | `usuarios:leer` | `GET /api/v1/usuarios`, `GET /api/v1/usuarios/{uuid}` |
| usuarios | `usuarios:crear` | `POST /api/v1/usuarios` |
| usuarios | `usuarios:editar` | `PUT /api/v1/usuarios/{uuid}`, `PUT /api/v1/usuarios/{uuid}/perfil` |
| usuarios | `usuarios:activar` | `PUT /api/v1/usuarios/{uuid}/activar` |
| usuarios | `usuarios:desactivar` | `PUT /api/v1/usuarios/{uuid}/desactivar` |
| marca (identidad-visual) | `marca:leer` | `GET /api/v1/mi-empresa/marca` (pantallas "Identidad de marca" y "Experiencia de acceso") |
| marca (identidad-visual) | `marca:editar` | `PUT /api/v1/mi-empresa/marca` |
| modulos (modulos-empresa) | `modulos:leer` | `GET /api/v1/mi-empresa/modulos` (pantalla "Mis módulos") |
| modulos (modulos-empresa) | `modulos:activar` | `POST /api/v1/mi-empresa/modulos/{codigo}/activar` |
| modulos (modulos-empresa) | `modulos:desactivar` | `POST /api/v1/mi-empresa/modulos/{codigo}/desactivar` |
| omnicanal | `omnicanal:leer` | Conversaciones, análisis, estadísticas, reportes (`GET /api/v1/omnicanal/**`) |
| omnicanal | `omnicanal:configurar` | `GET/PUT /api/v1/omnicanal/config`, token LIWA, rotar secreto |
| omnicanal | `omnicanal:reprocesar` | Reintentar análisis IA, reprocesar todo, backfill de ads |

Sembrado por dos changesets de Liquibase: `0015-sembrar-permisos-del-modulo-roles.yaml` (los 2 de `roles`, primera versión del módulo) y `0016-sembrar-catalogo-granular-de-permisos.yaml` (los 13 restantes).

Módulos de tenant que **todavía no tienen ningún `@PreAuthorize`**: `aprovisionamiento` (self-service pre-login, no aplica), `correo` (solo usado desde `consola`, plano de operador, no de tenant). `consola` en sí queda fuera de alcance de este módulo — es el back-office de plataforma, con su propio `RolOperador` (`SUPER_ADMIN`/`SOPORTE`), un sistema de autorización totalmente distinto.

## 6. Cómo agregar un permiso nuevo (checklist obligatorio)

Ver [ADR 0001](decisiones/2026-09-14-0001-catalogo-de-permisos-como-strings.md) para el porqué de este checklist: el nombre del permiso es un string que vive en tres lugares sin verificación automática entre ellos — saltarse un paso deja el endpoint inaccesible (403 silencioso) o el botón del frontend visible pero roto.

Cuando un endpoint nuevo necesite protección por permiso, en el **mismo PR**:

1. **Sembrar el permiso Y otorgarlo al rol ADMIN en el mismo changeset de Liquibase** (`backend/bootstrap/.../db/changelog/cliente/seguridad/NNNN-....yaml`), con este patrón — el otorgamiento va adentro del changeset, no aparte, para que corra automáticamente en cualquier base de cliente (nueva o ya existente) la próxima vez que se aplique el changelog, sin paso manual:
   ```yaml
   databaseChangeLog:
     - changeSet:
         id: seguridad-NNNN-sembrar-permiso-<algo>
         author: <tu nombre>
         comment: Permiso <modulo:accion> para <endpoint/pantalla>.
         changes:
           - insert:
               schemaName: seguridad
               tableName: tbl_permisos
               columns:
                 - column: {name: nombre, value: "modulo:accion"}
                 - column: {name: descripcion, value: "Frase en español, en minúscula, que describe la acción"}
           # Otorga el permiso nuevo al rol ADMIN de esta base, si existe --
           # sin esto, un tenant ya aprovisionado antes de este changeset
           # necesitaría un INSERT manual aparte (ver ADR 0001).
           - sql:
               sql: >
                 INSERT INTO seguridad.tbl_permisos_de_rol (rol_id, permiso_id)
                 SELECT r.id, p.id FROM seguridad.tbl_roles r
                 CROSS JOIN seguridad.tbl_permisos p
                 WHERE r.nombre = 'ADMIN' AND p.nombre = 'modulo:accion'
                   AND NOT EXISTS (
                     SELECT 1 FROM seguridad.tbl_permisos_de_rol pr
                     WHERE pr.rol_id = r.id AND pr.permiso_id = p.id)
         preConditions:
           - onFail: MARK_RAN
           - sqlCheck:
               expectedResult: "0"
               sql: SELECT COUNT(*) FROM seguridad.tbl_permisos WHERE nombre = 'modulo:accion'
         rollback:
           - delete: {schemaName: seguridad, tableName: tbl_permisos, where: "nombre = 'modulo:accion'"}
   ```
   Agregarlo al `include:` de `seguridad.changelog.yaml`. Para empresas nuevas, `EjecutorDdlPostgres.aplicarSemilla` ya lo cubre de todas formas (le otorga TODO el catálogo vigente al aprovisionar) — este `INSERT` extra en el changeset es lo que cierra el hueco para las bases **ya existentes**.
2. **Backend**: `@PreAuthorize("hasAuthority('modulo:accion')")` en el método del controller.
3. **Frontend**: si la acción tiene una ruta propia, agregar `permisoGuard('modulo:accion')` a su `canActivate` en `app.routes.ts`; si es un botón dentro de una pantalla ya protegida, envolverlo en `@if (authService.tienePermiso('modulo:accion'))` o usarlo en `[disabled]`. Si abre una pantalla/sección nueva del menú, agregar el link a `layout/shell.component.ts` detrás del mismo `@if`.
4. **Correr `mvn liquibase:update@aplicar-cliente`** contra cada base de cliente en el ambiente que corresponda (dev/staging/prod) — es rutina de deploy, no un paso especial, pero sin este paso el changeset no se aplicó y el permiso no existe todavía en esa base.

## 7. Estado del retrofit de `@PreAuthorize` por módulo

| Módulo | Estado |
|---|---|
| `roles` | ✅ Completo (desde la creación del módulo) |
| `usuarios` | ✅ Completo |
| `identidad-visual` (`MarcaController`) | ✅ Completo |
| `modulos-empresa` (`MisModulosController`, self-service) | ✅ Completo |
| `omnicanal` (`OmnicanalController`, `OmnicanalConfigController`) | ✅ Completo en backend. **Frontend parcial**: la ruta de configuración y el botón "Guardar" del panel embebido ya están gateados con `omnicanal:configurar`; los botones de reprocesar/backfill **dentro** del panel principal (`omnicanal-liwa-panel.component.ts` y sus sub-paneles) todavía no están ocultos por `omnicanal:reprocesar` — el backend ya los rechaza igual (403), es solo que el botón sigue visible para quien no tiene el permiso. Pendiente. |
| `modulos-empresa` (`ModulosAdminController`, `/api/v1/admin/**`) | Fuera de alcance — plano de plataforma/operador, se protege con `X-Admin-Key`, no con roles de tenant. |
| `aprovisionamiento`, `correo` | Sin endpoints de tenant autenticado que necesiten permiso (self-service pre-login o solo consumidos por `consola`). |
| `consola` | Fuera de alcance — sistema de autorización propio (`RolOperador`), plano de operador de plataforma, no de tenant. |

## 8. Bootstrap de permisos del rol ADMIN

**Empresas nuevas: automático, sin pasos manuales.** `EjecutorDdlPostgres.aplicarSemilla` (módulo `aprovisionamiento`, paso `SEMILLA_APLICADA` del pipeline) crea el rol `ADMIN` y, en el mismo paso, le otorga **todo** el catálogo vigente de `tbl_permisos` con un `INSERT ... SELECT` (no una lista fija de nombres) — así que cualquier permiso que se agregue en el futuro queda automáticamente cubierto para toda empresa que se aprovisione después de agregarlo. El primer usuario admin (`enviarBienvenida` → `sembrarUsuarioAdmin`) ya queda con el rol `ADMIN` completo desde el primer login.

**Empresas ya aprovisionadas antes de agregar un permiso nuevo:** desde este changeset en adelante, el otorgamiento va empaquetado en el propio changeset de Liquibase (ver sección 6, patrón a seguir) — corre solo al aplicar el changelog contra esa base, sin script manual aparte. Los changesets `0015` y `0016` (ya aplicados en este repo) no llevaban ese `INSERT` extra todavía — para cualquier base de cliente que los tenga aplicados sin el otorgamiento, correr una vez:
```sql
insert into seguridad.tbl_permisos_de_rol (rol_id, permiso_id)
select r.id, p.id from seguridad.tbl_roles r
cross join seguridad.tbl_permisos p
where r.nombre = 'ADMIN'
  and not exists (select 1 from seguridad.tbl_permisos_de_rol pr
                   where pr.rol_id = r.id and pr.permiso_id = p.id);
```
(idempotente, se puede re-ejecutar sin riesgo). Tenants migrados del sistema legado (ej. GuajiraNet): el nombre del rol admin es el que traiga cada tenant desde su propia migración, no necesariamente `ADMIN` — identificarlo primero (`SELECT nombre FROM seguridad.tbl_roles WHERE es_del_sistema = true`) y ajustar el `WHERE r.nombre = '...'`.

## 9. Contrato REST del módulo (CRUD de roles)

| Método | Ruta | Permiso | Body | Respuesta |
|---|---|---|---|---|
| POST | `/api/v1/roles` | `roles:gestionar` | `CrearRolRequest` | 201 + `RolResponse` |
| GET | `/api/v1/roles` | `roles:leer` | — | `RolResponse[]` |
| GET | `/api/v1/roles/{uuid}` | `roles:leer` | — | `RolResponse` |
| PUT | `/api/v1/roles/{uuid}` | `roles:gestionar` | `ActualizarRolRequest` | `RolResponse` |
| DELETE | `/api/v1/roles/{uuid}` | `roles:gestionar` | — | 204 |
| GET | `/api/v1/roles/{uuid}/permisos` | `roles:leer` | — | `PermisoResponse[]` |
| POST | `/api/v1/roles/{uuid}/permisos/{permisoUuid}` | `roles:gestionar` | — | 204 |
| DELETE | `/api/v1/roles/{uuid}/permisos/{permisoUuid}` | `roles:gestionar` | — | 204 |
| GET | `/api/v1/permisos` | `roles:leer` | — | `PermisoResponse[]` (catálogo completo) |
| GET | `/api/v1/usuarios/{uuid}/roles` | `roles:leer` | — | `RolResponse[]` |
| POST | `/api/v1/usuarios/{uuid}/roles/{rolUuid}` | `roles:gestionar` | — | 204 |
| DELETE | `/api/v1/usuarios/{uuid}/roles/{rolUuid}` | `roles:gestionar` | — | 204 |
| GET | `/api/v1/usuarios/{uuid}/permisos` | `roles:leer` | — | `{nombre: esConcedido}` (ajustes puntuales) |
| POST | `/api/v1/usuarios/{uuid}/permisos/{permisoUuid}/conceder` | `roles:gestionar` | — | 204 |
| POST | `/api/v1/usuarios/{uuid}/permisos/{permisoUuid}/revocar` | `roles:gestionar` | — | 204 |
| DELETE | `/api/v1/usuarios/{uuid}/permisos/{permisoUuid}` | `roles:gestionar` | — | 204 (quita el ajuste puntual, vuelve a lo que dan los roles) |

## 10. Otros pendientes conocidos

- Gating de frontend de los botones de reprocesar/backfill dentro del panel de omnicanal (ver sección 7).
- Sin verificación automática de que todo `@PreAuthorize("hasAuthority('...')")` del código tenga su fila correspondiente en el catálogo — ver "Cómo se podría evolucionar" en el ADR 0001.
- Igual que `usuarios` (ver su README): falta manejador de errores HTTP homologado entre módulos — `ManejadorErroresRoles` es propio de este módulo, no compartido.

## Historial de cambios

- 2026-09-14 — Leidi — Módulo nuevo: CRUD de roles, catálogo de permisos, asignación usuario↔rol y ajustes puntuales de permiso, permisos como claim del JWT.
- 2026-09-14 — Leidi — `EjecutorDdlPostgres.aplicarSemilla` otorga automáticamente todo el catálogo de permisos al rol `ADMIN` al aprovisionar una empresa nueva.
- 2026-09-14 — Leidi — Catálogo ampliado a permisos granulares por acción (15 en total, changeset `0016`): `@PreAuthorize` agregado a `UsuarioController`, `MarcaController`, `MisModulosController`, `OmnicanalController`, `OmnicanalConfigController`. Frontend: menú lateral y botones de cada pantalla gateados por permiso, catálogo agrupado por módulo en la UI de Roles (antes se veía como una lista plana de códigos crípticos). ADR 0001 documenta el riesgo de desincronización del catálogo de permisos y el checklist para agregar uno nuevo (sección 6).
