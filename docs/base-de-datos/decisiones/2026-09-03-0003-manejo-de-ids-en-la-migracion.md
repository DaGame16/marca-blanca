# ADR 0003 — Manejo de IDs en la migración

**Fecha:** 2026-09-03
**Estado:** Aceptada

## Resumen

En la migración se generan `id` (BIGINT) y `uuid` nuevos en cada fila. El ID de
texto del sistema viejo (cuid de Prisma) se usa solo como columna temporal
`id_origen` durante la transformación y se elimina al validar — no queda en las
tablas vivas.

## Contexto

El sistema viejo usa PKs de texto tipo cuid (`clx8k2...`). El modelo nuevo exige
(documento §3.2) dos identificadores por tabla: `id` autoincremental + `uuid`. La
data vieja es un grafo conectado por esos cuid (las FKs), así que durante la
migración hay que poder resolver "cuid viejo → id nuevo" para reconstruir las
relaciones.

## Opciones evaluadas

| Opción | Ventajas | Desventajas |
|---|---|---|
| Conservar el cuid como PK (texto) | Migración trivial, cero remapeo | Incompatible con §3.2; peor para índices y joins |
| Dejar `id_legado` permanente en cada tabla | Puente de trazabilidad siempre disponible | Columna muerta en decenas de tablas; el `uuid` nuevo ya es el identificador externo estable |
| **`id_origen` temporal + se archiva el mapeo** (elegida) | Tablas vivas limpias; scripts de transformación autosuficientes | Si un integrador externo guarda cuid viejos al corte, hay que darle una vía de resolución aparte |

## Decisión

- Se generan **`id` y `uuid` nuevos** en cada fila migrada.
- El cuid viejo se guarda en una **columna temporal `id_origen`** que agrega el
  script de transformación (`migracion/transform/NN-*.sql`), se usa para resolver
  las FKs por join, y se **elimina** en el paso `NN-*-limpieza.sql`.
- El mapeo `cuid → id nuevo`, si hace falta reconciliar, se re-deriva por una
  clave natural estable (`correo` para usuarios, `id_visible` para tareas,
  `numero_documento` para clientes, `nombre` para cuadrillas) o se archiva aparte.
- Los sistemas externos deben migrarse para referenciar el **`uuid` nuevo**, no el
  `id` numérico ni el cuid viejo.

## Consecuencias

| Aspecto | Impacto |
|---|---|
| Scripts de transformación | Cada uno agrega sus `id_origen`, migra, y limpia — se pueden volver a correr sobre una base recién clonada. |
| FKs entre módulos aún no migrados | Ej. `tareas.proceso_venta_id` antes de la ola de ventas: queda como columna sin FK, y la FK se agrega en un changeset posterior. |
| Trazabilidad al sistema viejo | Vía la clave natural, no por columna. Un `SELECT` extra cuando se necesite. |

## Cómo se podría revertir o evolucionar

Si aparece un consumidor externo que todavía guarde cuid viejos al momento del
corte, se resuelve con **una sola** tabla de lookup (`cuid → uuid nuevo`) con
fecha de retiro, no reintroduciendo una columna por tabla.
