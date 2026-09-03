# ADR 0001 — Estructura de los changelogs de Liquibase

**Fecha:** 2026-09-03
**Estado:** Aceptada

## Resumen

Los changelogs se parten en dos árboles según la base de datos que cambian
(`control/` y `cliente/`), y dentro de `cliente/` se organizan por módulo, en
carpetas, con numeración e `id` propios de cada módulo.

## Contexto

El diseño (documento de arquitectura §2, §11) exige: una base PostgreSQL por
cliente, una base de control transversal, todas las bases de cliente con la misma
estructura, y Liquibase en YAML como única fuente de verdad del esquema. Había que
decidir cómo organizar físicamente los changelogs para que eso escale a decenas
de tablas y varios módulos sin volverse un pilón ilegible.

## Opciones evaluadas

| Opción | Ventajas | Desventajas |
|---|---|---|
| **Un solo changelog plano** (`changes/0001..0099`) | Simple al principio, un orden lineal | Ilegible a partir de ~40 archivos; el número global no dice a qué módulo pertenece cada cambio |
| `includeAll` por carpeta | Menos `include` que escribir | Menos control del orden, que aquí importa por las FKs cross-schema |
| Un changelog por cliente | — | Rompe "todas las bases iguales"; multiplica el mantenimiento |
| **Carpeta por módulo con include explícito** (elegida) | Cada módulo se lee y revisa solo; espeja los módulos Maven del backend; orden explícito | Reorganizar cambia rutas, y Liquibase identifica changesets por `id` + `author` + ruta |

## Decisión

**Dos árboles, separados por base de datos:**

- `db/changelog/control/` → cambia `db_portal_guajiranet_control`. Lo aplica la
  app al arrancar (`spring.liquibase.change-log`).
- `db/changelog/cliente/` → cambia `db_plantilla_maestra` (y por clonación, cada
  `db_cliente_*`). Lo aplica el `liquibase-maven-plugin` a demanda; en el futuro,
  el job de aprovisionamiento.

**Dentro de `cliente/`, una carpeta por módulo** (`base/`, `seguridad/`,
`omnicanal/`, `producto/`, `cliente/`, `historico/`, `auditoria/`), cada una con
su `<modulo>.changelog.yaml`, numeración `NNNN-...` que reinicia en `0001`, e `id`
de changeset `<modulo>-NNNN-descripcion` igual al nombre del archivo.

El `cliente/db.changelog-master.yaml` solo incluye los módulos, **en orden de
dependencia**: `base → seguridad → omnicanal → producto → cliente → historico →
auditoria`.

**`producto/` se sub-divide** (`grants/`, `catalogos/`, `clientes/`, `tareas/`,
`inventario/`, `compras/`, `ventas/`) porque el núcleo ERP es un solo schema pero
~53 tablas; ahí el `id` es `producto-<area>-NNNN-descripcion`.

Las rutas de los dos `db.changelog-master.yaml` **no cambian nunca** — agregar un
módulo no toca `application.yml` ni el `pom.xml`.

## Consecuencias

| Aspecto | Impacto |
|---|---|
| Lectura / revisión | Cada módulo es autocontenible; un PR de un módulo no mueve archivos de otro. |
| Orden | El orden de módulos en el master es una dependencia implícita: al agregar changesets con FKs cross-schema hay que respetarlo. |
| QA / PROD | Liquibase identifica changesets por `id` + `author` + **ruta**. En DEV no importa (las bases se recrean). Una reorganización futura sobre bases ya migradas exige `changelogSync` o mantener rutas estables. |

## Cómo se podría revertir o evolucionar

Aplanar de nuevo sería un `git mv` masivo + re-`id`; solo tiene sentido si el
proyecto se achica. Para sumar un módulo nuevo: carpeta + `<modulo>.changelog.yaml`
+ una línea `include` en el master, en la posición de orden correcta.
