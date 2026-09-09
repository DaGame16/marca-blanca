# ADR 0002 — CRUD administrativo de `tbl_config_correo`

**Fecha:** 2026-09-08
**Estado:** Aceptada
**Módulos afectados:** correo

## Resumen

Se agrega un CRUD protegido por `X-Admin-Key` para administrar la
configuración SMTP de la plataforma (`tbl_config_correo`) desde la app, en
vez de únicamente por SQL directo.

## Contexto

Hasta ahora, la única fila de `tbl_config_correo` se creaba/editaba a mano
por SQL. Sin un CRUD, cualquier rotación de credenciales, cambio de
proveedor SMTP, o ajuste de configuración requería acceso directo a la base
de control.

## Decisión

Se sigue el mismo patrón ya usado por `modulos-empresa` para endpoints
administrativos: rutas bajo `/api/v1/admin/**`, protegidas por
`ClaveAdminInterceptor` (registrado globalmente — un módulo nuevo bajo esa
ruta queda protegido automáticamente, sin configuración de seguridad
adicional).

Se agregan 6 endpoints (crear, listar, buscar por id, actualizar, activar,
eliminar). La regla de negocio "como máximo una configuración activa a la
vez" — ya garantizada a nivel de base por un índice único parcial
(`uq_config_correo_activa`) — se refuerza también en la capa de aplicación:
`activar()` desactiva explícitamente cualquier otra fila activa antes de
activar la solicitada, respetando el orden que exige ese índice.

`eliminar()` rechaza borrar la configuración activa (`409 Conflict`) — hay
que activar otra primero. Evita dejar la plataforma sin ninguna
configuración SMTP activa por accidente.

## Consecuencias

- `correo-infrastructure` pasa de `spring-boot-starter-jdbc` a
  `spring-boot-starter-data-jpa` (ya tenía necesidad de JDBC para el
  adaptador SMTP; ahora suma una entidad JPA real para el CRUD).
- `ConfiguracionCorreoJpaRepository` se declaró `public` a propósito desde
  el principio — evita repetir el bug ya conocido de Spring Boot DevTools
  con interfaces `JpaRepository` no públicas.
- El CRUD no valida formato de correo/host más allá de lo que la base ya
  exige (`NOT NULL`) — una validación más fina con `jakarta.validation`
  queda pendiente, no se agregó ahora para no sumar una dependencia sin
  confirmar que el proyecto ya la trae.
- `secreto_ref` se administra por este CRUD como texto libre — sigue sin
  existir una resolución real contra un vault (ver ADR 0001, sección
  "Consecuencias").
