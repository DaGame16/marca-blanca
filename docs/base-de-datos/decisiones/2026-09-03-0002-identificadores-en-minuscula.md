# ADR 0002 — Identificadores en minúscula

**Fecha:** 2026-09-03
**Estado:** Aceptada

## Resumen

Todos los identificadores físicos (tablas, constraints, índices, funciones,
triggers) se escriben en minúscula, aunque el documento de arquitectura los liste
con prefijos en mayúscula.

## Contexto

El documento (§5) define prefijos en mayúscula: `TBL`, `FN`, `VW`, `TRG`, `IDX`.
Los primeros changesets se escribieron así (`TBL_usuarios`, `PK_usuarios`).

Al aplicarlos se detectó que **Liquibase 5.x entrecomilla cualquier identificador
con mayúsculas**: `tableName: TBL_usuarios` genera `CREATE TABLE "TBL_usuarios"`,
que en PostgreSQL es un nombre **sensible a mayúsculas**. A partir de ahí, toda
consulta manual, vista, función, política RLS y todo el SQL del ETL tendría que
escribir `"TBL_usuarios"` con comillas, para siempre. Síntoma concreto: una FK
con `references: seguridad.TBL_usuarios` (que Liquibase **no** entrecomilla en ese
atributo) no encontraba la tabla `"TBL_usuarios"`.

## Opciones evaluadas

| Opción | Ventajas | Desventajas |
|---|---|---|
| Mantener mayúsculas + usar sub-atributos de FK (`referencedTableName`, etc.) para que Liquibase entrecomille consistente | Respeta el texto literal del documento | Deja el "impuesto de las comillas" en todo el SQL a mano — diario, con RLS, funciones de auditoría y un ETL grande |
| **Todo en minúscula** (elegida) | El SQL a mano nunca necesita comillas | Se aparta de la caja literal del documento (no del prefijo) |

## Decisión

Todos los identificadores físicos en minúscula: tablas `tbl_...`, constraints
`pk_` / `uq_` / `fk_` / `ck_...`, índices `idx_...`, funciones `fn_...`, triggers
`trg_...`.

El documento fija el **prefijo**, no la caja — `tbl_` / `idx_` cumplen la
convención.

## Consecuencias

| Aspecto | Impacto |
|---|---|
| SQL a mano | Nunca necesita comillas para nombres de objeto. |
| Trabajo hecho | Hubo que reescribir los changesets ya aplicados (era temprano: 3 tablas de prueba). |
| Consistencia | El changelog de control se hizo desde el inicio con este criterio. |

## Cómo se podría revertir o evolucionar

No hay motivo para revertir. Si el documento de arquitectura se actualiza para
exigir mayúsculas de forma literal, habría que evaluar el costo de las comillas en
todo el SQL operativo antes de aceptarlo.
