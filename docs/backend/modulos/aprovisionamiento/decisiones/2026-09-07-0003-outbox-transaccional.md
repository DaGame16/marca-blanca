# ADR 0003 — Outbox transaccional para el evento de aprovisionamiento

**Fecha:** 2026-09-07
**Estado:** Aceptada

## Resumen

La Capa 1 no llama directamente al pipeline ni publica a una cola externa:
escribe una fila en `plataforma.tbl_eventos_salientes` dentro de la misma
transacción que crea la empresa. El pipeline la consume después.

## Contexto

Hace falta que "empresa creada" y "aprovisionamiento solicitado" sean atómicos:
nunca una empresa registrada sin su solicitud, ni una solicitud sin empresa.

- Eventos in-process de Spring: se pierden si el proceso muere entre el commit y
  el handler.
- Publicar a Redis/SQS desde el backend: no es atómico con el `INSERT`.

## Decisión

Patrón **transactional outbox**. `tbl_eventos_salientes` es genérico
(`tipo_evento`, `agregado_tipo`, `agregado_id`, `payload` JSONB, `estado`,
`intentos`, `disponible_en`, `bloqueado_*`), sin FK al agregado. El sondeador
reclama filas con `UPDATE ... WHERE id IN (SELECT ... FOR UPDATE SKIP LOCKED)` —
varias instancias no procesan la misma fila. Entrega **al menos una vez**; la
idempotencia de cada paso del pipeline cubre el "una vez de más". Backoff
exponencial en `disponible_en`, y `estado = fallido` al agotar `max_intentos`
(nunca un limbo silencioso).

## Consecuencias

- Sin infraestructura de mensajería nueva. Si en el futuro hay más consumidores,
  el sondeador puede reenviar a SQS sin que el outbox deje de ser la fuente.
- El índice caliente del sondeador es parcial
  (`idx_eventos_salientes_pendientes`), así la tabla puede acumular millones de
  eventos procesados sin degradar el poll.
