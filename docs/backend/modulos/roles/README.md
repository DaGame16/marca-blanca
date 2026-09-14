# Módulo Roles

> Resumen vivo. Se actualiza in-place. Historial de decisiones en `decisiones/`.

## 1. Qué es

Roles y permisos de los usuarios de un tenant. Bounded context propio, separado de `usuarios` (identidad) y `autenticacion` (login/JWT) — el esquema (`seguridad.tbl_roles`, `tbl_permisos`, `tbl_usuarios_roles`, `tbl_permisos_de_rol`, `tbl_permisos_de_usuario`) ya existía migrado del sistema legado; este módulo es el código que lo usa.

- **Rol**: agrupación de permisos, configurable por el tenant (CRUD completo). Un rol con `esDelSistema = true` no se puede renombrar ni eliminar.
- **Permiso**: catálogo fijo definido por la plataforma (lo que el código sabe hacer). Solo lectura desde la aplicación — se siembra por Liquibase (ver `0015-sembrar-permisos-del-modulo-roles.yaml`).
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

## 3. Regla de dependencia

`roles.domain`/`roles.application` no dependen de ningún otro contexto (usuarios, autenticacion, empresas, etc.) — solo identifican al usuario por su `uuid`, nunca importan el tipo `Usuario`. Verificado por `ArquitecturaHexagonalTest` (`roles_dominio_y_aplicacion_no_dependen_de_otros_contextos`).

El único acoplamiento hacia `roles-application` desde otro módulo es el ACL de `autenticacion` (`AdaptadorConsultarPermisosDeUsuario`, en `autenticacion-infrastructure`) — igual patrón que `AdaptadorVerificadorDeUsuarios` hacia `usuarios-domain`.

## 4. Cómo llega la autorización al JWT

Ver [ADR de autenticación: permisos como claim del JWT](../autenticacion/decisiones/2026-09-14-0008-permisos-como-claim-del-jwt.md). En resumen: `AutenticarUsuarioService`/`RenovarTokenService` calculan los permisos efectivos del usuario (vía `ConsultarPermisosDeUsuario`) y los embeben en el claim `permisos` del JWT. `JwtAuthFilter` los convierte en `GrantedAuthority` del `SecurityContext`, y los controllers los exigen con `@PreAuthorize("hasAuthority('...')")` (`@EnableMethodSecurity` en `SecurityConfig`).

## 5. Contrato REST

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

## 6. Bootstrap de permisos del rol ADMIN

**Empresas nuevas (aprovisionadas a partir de este cambio): automático, sin pasos manuales.** `EjecutorDdlPostgres.aplicarSemilla` (módulo `aprovisionamiento`, paso `SEMILLA_APLICADA` del pipeline) crea el rol `ADMIN` y, en el mismo paso, le otorga **todo** el catálogo vigente de `tbl_permisos` con un `INSERT ... SELECT` (no una lista fija de nombres) — así que cualquier permiso que se agregue en el futuro (de este módulo o de cualquier otro) queda automáticamente cubierto para toda empresa que se aprovisione después de agregarlo, sin tocar ese método de nuevo. El primer usuario admin (`enviarBienvenida` → `sembrarUsuarioAdmin`, paso `BIENVENIDA_ENVIADA`) ya queda con el rol `ADMIN` completo desde el primer login.

**Empresas ya aprovisionadas o migradas antes de este cambio: requieren un `INSERT` único, manual.** Dos casos:

- Tenants aprovisionados por este mismo pipeline (rol siempre llamado `ADMIN`, `es_del_sistema = true`): correr una vez contra la base de esa empresa
  ```sql
  insert into seguridad.tbl_permisos_de_rol (rol_id, permiso_id)
  select r.id, p.id from seguridad.tbl_roles r
  cross join seguridad.tbl_permisos p
  where r.nombre = 'ADMIN'
    and not exists (select 1 from seguridad.tbl_permisos_de_rol pr
                     where pr.rol_id = r.id and pr.permiso_id = p.id);
  ```
  (el mismo `INSERT` que corre `aplicarSemilla` — es idempotente, se puede re-ejecutar sin riesgo).
- Tenants migrados del sistema legado (ej. GuajiraNet): el nombre del rol admin es el que traiga cada tenant desde su propia migración, no necesariamente `ADMIN` — hay que identificarlo primero (`SELECT nombre FROM seguridad.tbl_roles WHERE es_del_sistema = true`) y ajustar el `WHERE r.nombre = '...'` del `INSERT` de arriba antes de correrlo.

## 7. Otros pendientes conocidos

- **Retrofit de `@PreAuthorize` en otros módulos**: por ahora solo los endpoints del propio módulo `roles` exigen permiso. `usuarios`, `empresas`, etc. siguen abiertos a cualquier usuario autenticado — habilitarlo ahí es trabajo futuro, una vez este módulo esté probado en producción.
- Igual que `usuarios` (ver su README): falta manejador de errores HTTP homologado entre módulos — `ManejadorErroresRoles` es propio de este módulo, no compartido.

## Historial de cambios

- 2026-09-14 — Leidi — Módulo nuevo: CRUD de roles, catálogo de permisos, asignación usuario↔rol y ajustes puntuales de permiso, permisos como claim del JWT.
- 2026-09-14 — Leidi — `EjecutorDdlPostgres.aplicarSemilla` otorga automáticamente todo el catálogo de permisos al rol `ADMIN` al aprovisionar una empresa nueva — cierra el hueco de bootstrap para tenants nuevos (ver sección 6).
