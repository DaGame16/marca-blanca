# ADR 0001 — Módulo separado para el aprovisionamiento

**Fecha:** 2026-09-07
**Estado:** Aceptada

## Resumen

El alta de una empresa cliente y el aprovisionamiento de su base de datos física
viven en un módulo Maven propio, `aprovisionamiento`, no dentro de `empresas`.

## Contexto

`empresas` se redujo a propósito el 2026-09-05 a una sola responsabilidad:
resolver a qué base física va cada petición en runtime. `Empresa` ahí es un
`record` de lectura, sin comportamiento.

El aprovisionamiento es lo contrario: un agregado `Empresa` con reglas
(`registrar`, `activar`), una saga de varios pasos, DDL con rol `owner`, un
outbox y un job. Meterlo en `empresas` volvería a cargar ese módulo con dos
responsabilidades sin relación, justo lo que se acababa de separar.

## Decisión

Contexto nuevo `aprovisionamiento` con sus tres capas
(`aprovisionamiento-domain` / `-application` / `-infrastructure`), mismo patrón de
split que `modulos-empresa` e `identidad-visual`. Tiene su propio mapeo de
escritura de `plataforma.tbl_empresas` (`EmpresaDeAprovisionamientoEntity`, con
`@Entity(name=...)` explícito para no colisionar en Hibernate con los otros
mapeos de esa tabla).

## Consecuencias

- Cuarta clase que mapea `tbl_empresas` (ya había tres de solo lectura). Es la
  única de escritura y la que gobierna el ciclo de vida.
- `empresas` sigue sin exponer endpoints ni cargar responsabilidades nuevas.
- El pipeline (Capa 2) puede extraerse a un deployable aparte sin tocar `empresas`.
