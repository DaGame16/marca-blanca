# ADR 0001 — Consolidar el envío de correo en el módulo `correo`, eliminando la duplicación con `aprovisionamiento`

**Fecha:** 2026-09-08
**Estado:** Aceptada
**Módulos afectados:** correo, aprovisionamiento

## Resumen

El envío real del correo de bienvenida pasa a hacerse siempre a través del
módulo `correo` (puerto `EnviarCorreoDeBienvenida`). Antes existían **dos
implementaciones distintas** para el mismo correo, y la que de verdad corría
en el pipeline no era la del módulo `correo`.

## Contexto

`correo` ya existía como módulo propio: arquitectura hexagonal completa
(`domain`/`application`/`infrastructure`), plantilla HTML separada del código,
tests. Pero no estaba conectado a ningún flujo real — nada lo llamaba.

El correo de bienvenida que **de verdad se enviaba**, al terminar el pipeline
de aprovisionamiento, vivía reimplementado a mano dentro de
`EjecutorDdlPostgres.enviarBienvenida()` (en `aprovisionamiento`):

- armaba su propio `JavaMailSenderImpl` desde cero, sin pasar por `correo`,
- el cuerpo del correo era **texto plano hardcodeado directo en el código
  Java** — sin plantilla,
- sí leía la configuración SMTP real de `plataforma.tbl_config_correo`, algo
  que el módulo `correo` en ese momento no hacía (leía propiedades estáticas
  de `application.yml`, sin uso real).

Se detectó al revisar `tbl_config_correo` para ajustar el módulo `correo` a
como está la base de datos hoy — ahí se encontraron las dos implementaciones
compitiendo, y se confirmó que la que corría en producción no era la que
tenía arquitectura, plantilla ni tests.

## Decisión

**Se mueve el `cómo se envía` hacia `correo`. Se deja en `aprovisionamiento`
el `qué pasa antes y después` del envío.**

Concretamente, `EjecutorDdlPostgres.enviarBienvenida()` conserva:
- el chequeo de idempotencia contra `tbl_empresas.bienvenida_enviada_en`,
- la generación de la contraseña temporal,
- `sembrarUsuarioAdmin` (crear el usuario admin en la base del cliente),
- el `update` final que marca `bienvenida_enviada_en`.

Y ahora, en vez de armar el correo a mano, llama a
`EnviarCorreoDeBienvenida.ejecutar(...)` (puerto de `correo`).

Del lado de `correo`, `AdaptadorProveedorCorreoSmtp` (antes basado en
propiedades estáticas) pasa a leer `tbl_config_correo` — la misma consulta
que antes vivía en `aprovisionamiento`, movida a donde corresponde
arquitectónicamente.

`aprovisionamiento-infrastructure` pasa a depender de
`correo-application` (solo el puerto de entrada, no el modelo interno de
`correo`) y deja de depender de `spring-boot-starter-mail` directo — ya no
construye ningún objeto de correo por sí mismo.

## Consecuencias

- **El nombre de la propiedad de la clave SMTP no cambió**:
  `app.aprovisionamiento.smtp-password` sigue siendo el nombre, aunque ahora
  la lee una clase de `correo`. Se decidió así a propósito, para no romper
  ninguna variable de entorno ya configurada en ningún ambiente real — el
  costo es una inconsistencia cosmética de nombres entre módulos, aceptada
  conscientemente.
- El comportamiento de "sin config activa → loguear y seguir, no lanzar
  excepción" se preservó tal cual estaba — para no tumbar el pipeline de alta
  de una empresa solo porque el correo no está configurado en ese ambiente
  todavía.
- Quedan pendientes, fuera de alcance de este cambio: qué es en la práctica
  `secreto_ref` (la columna existe, pensada para apuntar a un vault real,
  pero ningún código la lee todavía — la clave sigue viniendo de una
  variable de entorno aparte). El CRUD de la tabla se resuelve en la ADR 0002.
