# ADR 0006 — El wizard escribe la config de la empresa; no pasa por los otros módulos como dueños

**Fecha:** 2026-09-08
**Estado:** Aceptada

## Resumen

Durante el wizard de onboarding, `aprovisionamiento`:

- **Módulos**: invoca los puertos de entrada públicos de `modulos-empresa`
  (`ActivarModuloDeEmpresa` / `DesactivarModuloDeEmpresa` / `ListarModulosDeEmpresa`)
  a través de un adaptador ACL (`PuenteModulosEmpresa`).
- **Colores / logo / variantes de UI**: escribe `plataforma.tbl_empresas_marca`
  con su **propio mapeo JPA** (`MarcaDeAprovisionamientoEntity`,
  `@Entity(name = "MarcaDeAprovisionamiento")`), sin pasar por `identidad-visual`.

## Contexto

- `identidad-visual` ya tiene un caso de uso `ActualizarMarcaDeEmpresa`, pero su
  repositorio resuelve la empresa con `... where estado = 'activa'`. Durante el
  wizard la empresa está en `borrador`, así que ese puerto lanzaría
  "empresa no encontrada".
- `identidad-visual.MarcaController` además saca la empresa del **JWT** del
  usuario logueado (self-service sobre una empresa activa). El wizard es público
  y sin sesión.
- Ya hay 3+ mapeos mínimos de `tbl_empresas` en distintos módulos
  (`empresas`, `identidad-visual`, `modulos-empresa`) — la duplicación de mapeo
  de tablas de `plataforma` es un costo aceptado del aislamiento entre módulos
  (ver ADR 0001 de `modulos-empresa`).

## Decisión

- Para **módulos**: reutilizar `modulos-empresa` vía ACL (no duplicar la lógica
  de activación). El adaptador traduce `ModuloDeEmpresa` (dominio de ese módulo)
  a un `Set<String>` de códigos; esa traducción vive solo en el adaptador.
- Para **`tbl_empresas_marca`**: `aprovisionamiento` tiene su propio
  `MarcaDeAprovisionamientoEntity` de escritura, con nombre de entidad Hibernate
  explícito para no colisionar con `EmpresaMarcaEntity` de `identidad-visual`
  (misma tabla, distinto bounded context). El endpoint
  `PUT /api/v1/registro/empresas/{id}/personalizacion` hace upsert 1:1.
- La variante de UI (`tipo_login`, `tipo_pantalla_principal`) es **autoridad del
  backend**: se persiste acá y el frontend solo renderiza la variante activa.

## Consecuencias

- Cuarto mapeo de una tabla de `plataforma` en el backend. Aceptado.
- `identidad-visual` no se tocó: sigue sirviendo la marca a empresas ya activas.
- Cuando la empresa se activa, la fila de `tbl_empresas_marca` que escribió el
  wizard es la que `identidad-visual` lee — sin migración ni handoff.
