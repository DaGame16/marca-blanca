# ADR 0004 — Credenciales por tenant y `secreto_ref`

**Fecha:** 2026-09-07
**Estado:** Aceptada (implementación parcial en DEV)

## Resumen

Cada base de cliente recibe sus propios roles de login
(`cli_<slug>_app` / `cli_<slug>_lectura`), miembros de los roles de grupo
`guajiranet_app` / `guajiranet_lectura`. La contraseña real vive en un vault;
`plataforma.tbl_empresa_conexiones.secreto_ref` guarda la **referencia**, nunca la
clave en claro.

## Contexto

Si se filtra la credencial de aplicación de un cliente, el atacante debe entrar a
**una** base, no a todas. También hace falta rotar y revocar por cliente sin
tocar a los demás. Es la lectura literal del "aislamiento total por cliente" del
principio rector del documento de BD.

## Decisión

- Los privilegios viven en un solo sitio: los roles de grupo `guajiranet_app` /
  `guajiranet_lectura` (los definen los changesets de cliente). Los roles
  `cli_<slug>_*` solo aportan identidad y heredan por `GRANT ... TO`.
- El pipeline (`crearRolesDeTenant`) los crea de forma idempotente en la conexión
  de mantenimiento (owner).

## Estado en DEV vs. QA/PROD

| | DEV (hoy) | QA/PROD (pendiente, es infra) |
|---|---|---|
| Contraseña de `cli_<slug>_*` | = el nombre del rol (misma convención que `0000-crear-roles-motor`) | generada aleatoria por el aprovisionador |
| `secreto_ref` | `'dev-local'` | id del secreto en AWS Secrets Manager |
| Rol que usa el enrutador | `guajiranet_app` compartido (hardcoded en `EnrutadorDataSourcePorEmpresa`) | `cli_<slug>_app`, resuelto desde `secreto_ref` |

## Consecuencias

- El enrutador multi-tenant todavía no usa los roles por-tenant; migrarlo a
  resolver `secreto_ref` contra el vault es trabajo de infraestructura para
  QA/PROD, sin cambios en este módulo.
