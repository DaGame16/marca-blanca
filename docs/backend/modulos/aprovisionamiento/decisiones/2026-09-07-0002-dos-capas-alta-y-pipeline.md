# ADR 0002 — Dos capas: alta (rol app) y pipeline (rol owner)

**Fecha:** 2026-09-07
**Estado:** Aceptada

## Resumen

El aprovisionamiento se parte en dos responsabilidades con roles de motor
distintos, siguiendo la sección 2.3 del documento de Arquitectura de Base de
Datos.

| Capa | Rol | Qué hace |
|---|---|---|
| **1 — Alta** | `guajiranet_app` (sin DDL) | Registra la empresa en `plataforma.tbl_empresas` (`estado = pendiente_aprovisionamiento`) y escribe un evento en el outbox, en una sola transacción. Endpoint `POST /api/v1/admin/empresas`, responde `202`. |
| **2 — Pipeline** | `guajiranet_owner` (DDL) | Consume el evento del outbox y ejecuta la saga: `CREATE DATABASE ... TEMPLATE`, semilla, roles por-tenant, `tbl_empresa_conexiones`, `tbl_empresa_esquema_version`, `tbl_empresa_modulos`, y `estado = activa`. |

## Contexto

Una vulnerabilidad en el backend público no debe poder crear ni borrar bases.
Además el pipeline necesita su propio log y reintentos por paso.

## Decisión

- Capa 1 corre en el backend público. La transacción "guardar empresa + outbox"
  la abre un decorador de infraestructura (`RegistrarEmpresaTransaccional`); la
  capa de aplicación queda libre de Spring.
- Capa 2 corre hoy en el mismo proceso (un `@Scheduled` que sondea el outbox),
  pero toda su DDL pasa por un `DataSource` de mantenimiento aparte
  (`owner @ base 'postgres'`, autocommit — `CREATE DATABASE` no admite
  transacción). Nunca usa las unidades de persistencia JPA de la app.
- La saga guarda un checkpoint (`tbl_aprovisionamiento_tareas.paso_actual`)
  después de cada paso; un reintento reanuda desde el último paso completado.

## Consecuencias

- Extraer la Capa 2 a un deployable separado (con su propio IAM/secreto en
  QA/PROD) es un cambio de infraestructura, no de código: el sondeador y el
  ejecutor DDL ya están aislados en el sub-paquete `infrastructure/pipeline`.
- En DEV, si el pipeline muere entre reclamar un evento y terminarlo, la fila del
  outbox queda en `procesando`; hace falta un barrido que la devuelva a
  `pendiente` (pendiente, ver README §8).
