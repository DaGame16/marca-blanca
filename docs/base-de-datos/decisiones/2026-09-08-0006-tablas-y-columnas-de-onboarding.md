# ADR 0006 (BD) — Tablas y columnas del onboarding de empresas

**Fecha:** 2026-09-08
**Estado:** Aceptada
**Contexto:** flujo [`docs/backend/flujos/onboarding-de-empresas.md`](../../backend/flujos/onboarding-de-empresas.md)

## Resumen

Changesets agregados a la base de **control** (`db_portal_guajiranet_control`,
schema `plataforma`) y a la de **cliente** (`seguridad`) para soportar el wizard
público de registro de empresas.

## Control (`db/changelog/control/changes/`)

| Changeset | Cambio |
|---|---|
| `0012-agregar-datos-de-registro-empresas` | `tbl_empresas` += `representante_legal`, `correo_contacto`, `telefono`, `sitio_web` (nullable; el endpoint los exige) + `idx_empresas_correo_contacto` |
| `0013-ampliar-estados-empresa` | recrea `ck_empresas_estado` agregando `'borrador'` |
| `0014-agregar-precio-modulos` | `tbl_modulos` += `precio NUMERIC(12,2)`, `moneda VARCHAR(3)` (para mostrar el costo en el wizard; no se cobra) |
| `0015-agregar-variantes-ui-marca` | `tbl_empresas_marca` += `tipo_login SMALLINT`, `tipo_pantalla_principal SMALLINT` (CHECK 1..3) |
| `0016-crear-tabla-config-correo` | `tbl_config_correo` — config SMTP de la plataforma (remitente, host, puerto, usuario, `secreto_ref`, `seguridad`). Índice parcial "una sola activa". **Nota:** el `id` del changeset quedó `0018-...` (no coincide con el archivo) por renombre posterior a correrse; alinear en entorno limpio. |
| `0017-preparar-correo-de-bienvenida` | recrea `ck_aprovisionamiento_tareas_paso` agregando `'bienvenida_enviada'` + `tbl_empresas.bienvenida_enviada_en TIMESTAMPTZ` (idempotencia del correo) |

## Cliente (`db/changelog/cliente/seguridad/`)

| Changeset | Cambio |
|---|---|
| `0014-agregar-contrasena-temporal` | `seguridad.tbl_usuarios += es_contrasena_temporal BOOLEAN NOT NULL DEFAULT false`. Lo pone en `true` el paso `bienvenida_enviada` del pipeline; lo limpia el cambio de contraseña. |

## Descartado

Se diseñaron pero **no se aplicaron** `tbl_ordenes_pago` / `tbl_ordenes_pago_items`
y `tbl_config_pasarela_pago` — el flujo quedó sin pago
([ADR 0005 de aprovisionamiento](../../backend/modulos/aprovisionamiento/decisiones/2026-09-08-0005-onboarding-publico-sin-pasarela-de-pago.md)).

## Consecuencia operativa

Las bases `db_cliente_*` creadas antes de aplicar `cliente/seguridad/0014` a
`db_plantilla_maestra` no tienen `es_contrasena_temporal` — hay que recrearlas o
migrarlas antes de que su primer usuario admin pueda loguearse.
