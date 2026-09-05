# ADR 0001 — Módulo separado, no una funcionalidad más de `empresas`

**Fecha:** 2026-09-05
**Estado:** Aceptada

## Resumen

El catálogo de módulos y su activación/desactivación por empresa se extrae de `empresas` a un módulo Maven propio, `modulos-empresa` — cero dependencia entre los dos, en cualquier dirección.

## Contexto

`empresas` cargaba dos responsabilidades sin relación real entre sí: resolver la conexión multi-tenant (`Empresa`, `EmpresaConexion`, `ContextoEmpresaActual`) y administrar el catálogo de módulos activables (`Modulo`, `ModuloDeEmpresa`). Se confirmó, leyendo el dominio real, que **ninguna clase de `Modulo`/`ModuloDeEmpresa` referencia `Empresa`** — reciben `UUID empresaId` como dato simple. Y las tablas (`tbl_modulos`, `tbl_empresa_modulos` vs. `tbl_empresas`, `tbl_empresa_conexiones`) ya estaban separadas desde los changelogs de Liquibase de Leidi — no hacía falta ningún cambio de base de datos, solo de organización del código Java.

Modelo de referencia: **Odoo** — módulos instalables, independientes entre sí, ninguno depende del otro para existir.

## Decisión

Se crean 3 módulos Maven nuevos (`modulos-empresa-domain`, `-application`, `-infrastructure`), moviendo el código tal cual desde `empresas` (mismos records, mismos casos de uso, mismo contrato REST bajo `/api/v1/admin/**`, misma protección por clave compartida).

**El único punto real de acoplamiento que existía** — una consulta JPQL contra la entidad `EmpresaEntity` de `empresas`, para resolver el id interno de una empresa a partir de su UUID — se resuelve con el mismo patrón que ya usaba `identidad-visual`: un mapeo propio, mínimo y de solo lectura de `tbl_empresas` (`EmpresaRefEntity`, renombrada después a `EmpresaRefDeModulosEmpresa` — ver más abajo).

## Consecuencias

| Capa | Impacto |
|---|---|
| `empresas-infrastructure` | Ya no necesita `spring-boot-starter-web` — dejó de exponer cualquier endpoint. |
| `bootstrap` | `@EntityScan`, `@EnableJpaRepositories` y `.packages(...)` de la unidad de persistencia "control" deben incluir el paquete nuevo — son 3 lugares distintos, los 3 tienen que actualizarse juntos o la app compila bien y falla recién al arrancar. |
| Duplicación aceptada | Ahora existen **3 mapeos mínimos** de `tbl_empresas` (`empresas`, `identidad-visual`, `modulos-empresa`) — duplicación de código a propósito, aceptada como el costo de la independencia real entre módulos. Evaluado explícitamente reemplazarlo por una clase compartida en `shared-kernel`; se descartó porque eso reintroduciría el mismo tipo de acoplamiento que motivó separar `autenticacion` de `usuarios` (ver ADR 0006 de `autenticacion`) — un cambio de `tbl_empresas` rompería los 3 módulos a la vez, sin aviso, en vez de romper cada uno por separado y de forma visible. |

## Bug encontrado en el camino (no de diseño — de nombres)

Al converger `identidad-visual` y `modulos-empresa` en la misma unidad de persistencia, Hibernate rechazó el arranque: los dos módulos, sin coordinar entre sí (justo por ser independientes), habían llamado `EmpresaRefEntity` a su propio mapeo mínimo — mismo nombre de clase, mismo nombre de entidad JPA, dos clases distintas. **No es un acoplamiento** (cero import cruzado, confirmado) — es una colisión de nombre en el catálogo interno de Hibernate. Se resolvió renombrando ambas clases explícitamente: `EmpresaRefDeIdentidadVisual` y `EmpresaRefDeModulosEmpresa`.

## Cómo se podría revertir o evolucionar

Si algún día `tbl_empresas` cambia de forma con la suficiente frecuencia como para que mantener 3 mapeos sea una carga real (y no solo teórica), se puede introducir una clase compartida en `shared-kernel` — pero recién ahí, cuando el costo de la duplicación supere al costo del acoplamiento, no antes.
