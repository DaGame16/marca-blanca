# ADR 0004 — Auditoría por triggers, inmutable

**Fecha:** 2026-09-03
**Estado:** Aceptada

## Resumen

La auditoría de escrituras se hace con triggers de fila en la base de datos, no
en el backend. La tabla de auditoría es inmutable incluso para el rol `owner`; la
única forma de borrar es una función de purga por retención.

## Contexto

El documento (§7) exige que cada base de cliente tenga su propio esquema de
auditoría, poblado por triggers de fila (INSERT/UPDATE/DELETE) en tablas críticas,
con el rastro protegido contra borrado incluso de quien tiene acceso DDL, y
retención regida por la Ley 1581 (punto de partida ~2 años).

## Opciones evaluadas

| Opción | Ventajas | Desventajas |
|---|---|---|
| Auditoría en el backend (interceptor / aspecto) | Sabe el usuario de app directo; fácil de enriquecer | No cubre cambios que no pasen por la app (jobs, SQL directo, migraciones); se puede olvidar en un endpoint nuevo |
| **Triggers de fila en la BD** (elegida) | Cubre *todo* cambio a la tabla, venga de donde venga; imposible de "olvidar" | El usuario de app hay que pasarlo por una variable de sesión; algo de overhead por escritura |

## Decisión

Módulo `cliente/auditoria/`:

- **`plataforma.tbl_auditoria`** — `esquema`, `tabla`, `operacion`,
  `registro_id`/`registro_uuid`, `datos_anteriores`/`datos_nuevos` (JSONB),
  `usuario_bd`, `usuario_app_id`, `txid`, `ejecutado_en`.
- **`fn_auditar()`** — función de trigger `SECURITY DEFINER`.
- **`fn_activar_auditoria(esquema, tabla)`** — helper. Sumar una tabla crítica =
  un changeset nuevo que la llame. Lista actual: 20 tablas (`auditoria/0005`).
- **Inmutabilidad**: triggers `BEFORE UPDATE/DELETE/TRUNCATE` que lanzan
  excepción + `REVOKE` de escritura a `app`/`lectura`/`PUBLIC`. La única vía de
  borrado es `fn_purgar_auditoria(interval)`, que levanta un flag de sesión que
  el trigger de inmutabilidad reconoce.
- **Silenciado en bloque**: `SET LOCAL plataforma.auditoria_activa = 'off'` en una
  transacción salta los triggers. Lo usan los scripts de migración para no
  registrar la carga inicial como 37 000 cambios.
- **Usuario de app**: el backend hace `SET plataforma.usuario_app_id = '<id>'` al
  inicio de cada request; `fn_auditar()` lo lee. *(Pendiente — código Java.)*

## Consecuencias

| Aspecto | Impacto |
|---|---|
| Cobertura | Cualquier cambio a una tabla auditada queda registrado, incluso desde `psql`. |
| Rendimiento | Un INSERT extra a `tbl_auditoria` por cada escritura auditada. Aceptable para el conjunto elegido (no incluye tablas de alto volumen como los turnos de chat). |
| Migración | Los scripts de transformación deben incluir `SET LOCAL plataforma.auditoria_activa = 'off'` — ya lo hacen. |
| Backend | Sin `SET plataforma.usuario_app_id`, la columna `usuario_app_id` queda NULL (el `usuario_bd` sí se registra siempre). |
| "Inmutable" real | El `owner` podría deshabilitar el trigger (`ALTER TABLE ... DISABLE TRIGGER`) — es un acto explícito y deliberado, que es el nivel realista de protección en PostgreSQL. |

## Cómo se podría revertir o evolucionar

- Sumar/quitar tablas de la lista: un changeset que llame a `fn_activar_auditoria`
  o haga `DROP TRIGGER trg_auditoria ON ...`.
- El plazo de retención se cambia en la llamada a `fn_purgar_auditoria(interval)`
  (falta definir con negocio/legal y agendar la purga).
- Los `event triggers` para cambios de estructura fuera de Liquibase (también
  mencionados en §7) quedan pendientes; se agregan como un changeset más del
  módulo.
