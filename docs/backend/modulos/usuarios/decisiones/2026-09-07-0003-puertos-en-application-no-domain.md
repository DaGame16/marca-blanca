# ADR 0003 — Puertos en Aplicación, no en Dominio

**Fecha:** 2026-09-07
**Estado:** Aceptado

## Contexto

El módulo `usuarios` definía `RepositorioUsuarios`, `RepositorioUsuarioPerfiles` y `CifradorDeContrasenas` en `usuarios-domain/.../domain/port/out/`. La arquitectura formalizada en `01-arquitectura.md` establece que los puertos (de entrada y salida) se definen únicamente en la capa de Aplicación — el Dominio no depende de nada, ni siquiera de sus propias interfaces técnicas.

## Decisión

Los 3 puertos se movieron a `usuarios-application/.../application/port/out/`, sin excepciones.

`CifradorDeContrasenas` era un caso especial: `Usuario.verificarCredenciales()` lo recibía como parámetro directo, lo cual habría obligado al dominio a importar algo de aplicación. Se resolvió cambiando la firma del método: en vez de recibir `Contrasena` + `CifradorDeContrasenas`, ahora recibe un `boolean contrasenaCorrecta` ya calculado por quien orquesta el caso de uso (`AdaptadorVerificadorDeUsuarios`, en `autenticacion`). El dominio decide *qué hacer* con el resultado, no *cómo* obtenerlo.

## Consecuencias

- `Usuario.java` ya no importa ningún puerto — el dominio queda con dependencia cero, verificable por ArchUnit.
- `AdaptadorVerificadorDeUsuarios.java` (módulo `autenticacion`) ajustó su import y ahora invoca el cifrador antes de llamar a `verificarCredenciales()`.
- `autenticacion-infrastructure/pom.xml` agregó `usuarios-application` como dependencia nueva.
- Cambio coordinado con Luis (dueño de `autenticacion`) antes de aplicar.