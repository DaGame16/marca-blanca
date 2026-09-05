# ADR 0005 — El JWT lleva la empresa como claim

**Fecha:** 2026-09-04
**Estado:** Aceptada

## Resumen

El access token (JWT) ahora incluye el identificador de la empresa del usuario como un claim adicional (`empresa`), junto al `subject` (id de usuario) que ya llevaba.

## Contexto

Dos problemas separados, resueltos juntos:

1. **El login no funcionaba de verdad.** `ContextoEmpresaActual` (el que le dice al enrutador multi-tenant a qué base conectarse) nunca se establecía en ningún punto del código — se había construido la pieza que lo lee (`EnrutadorDataSourcePorEmpresa`) pero nunca la que lo escribe. Cualquier login real fallaba con `IllegalStateException: No hay empresa activa en el contexto de la peticion`. No se detectó antes porque las pruebas de esta rama solo confirmaban que la aplicación arrancaba, nunca que un login real completara — faltaban datos de prueba para probarlo de punta a punta.

2. **Un endpoint self-service (`identidad-visual`, ver su propia ADR) necesitaba saber de qué empresa es el usuario autenticado, sin depender de un parámetro que mande el cliente.** Si el endpoint recibiera el identificador de empresa como parámetro de la URL o del body sin verificar nada, cualquier usuario logueado podría, cambiando ese valor, tocar datos de una empresa que no es la suya.

## Opciones evaluadas

| Opción | Consideración |
|---|---|
| Setear `ContextoEmpresaActual` solo en login/refresh, dejar el JWT como estaba | Resuelve el problema 1, pero no el 2 — ningún endpoint posterior al login sabría a qué empresa pertenece el usuario sin recibirlo (sin verificar) del cliente |
| **Agregar la empresa como claim del JWT** (elegida) | Resuelve los dos: login/refresh siguen fijando el contexto desde el body (que ya reciben), y cualquier request posterior con JWT válido lo saca del propio token, firmado — el cliente no puede falsificarlo sin invalidar la firma |

## Decisión

- `GeneradorDeToken.generarPara(Usuario, String identificadorEmpresa)` — el puerto ahora recibe la empresa, y `JwtGeneradorDeToken` la agrega como claim (`empresa`).
- `VerificadorDeToken.verificar(String)` devuelve `Optional<UsuarioAutenticado>` (antes `Optional<UUID>`) — un registro nuevo que trae el id de usuario **y** la empresa juntos.
- `JwtAuthFilter`, en cada request con JWT válido, establece `ContextoEmpresaActual` (limpiándolo después, `finally`) y deja la empresa disponible como atributo del request — así cualquier controller puede leerla sin volver a decodificar el token.
- `AuthController` (login y refresh) sigue estableciendo `ContextoEmpresaActual` directo desde el body del request, antes de que exista ningún JWT que leer — necesario porque en esos dos casos el usuario todavía no tiene una sesión.

## Consecuencias

| Capa | Impacto |
|---|---|
| Dominio | Ninguno. |
| Aplicación | `AutenticarUsuarioService`/`RenovarTokenService` ahora dependen de `empresas-application` (para leer `ContextoEmpresaActual`, ya establecido por el controller antes de llamarlos) — primera dependencia cruzada de `autenticacion-application` hacia otro módulo de aplicación. |
| Infraestructura | `RefreshRequest` ahora exige `identificadorEmpresa` — cambio de contrato, rompe compatibilidad con clientes que ya lo estuvieran llamando (ninguno todavía, no hay frontend conectado). |
| Seguridad | Un endpoint self-service ya no puede recibir engañado un `empresaId` ajeno — la empresa viene siempre del JWT ya verificado. |

## Cómo se podría revertir o evolucionar

Si en el futuro un usuario necesita pertenecer a más de una empresa a la vez (hoy el modelo asume una sola), este claim tendría que pasar de un valor único a una lista, y el cliente debería indicar con cuál empresa quiere operar en cada request — cambio de diseño mayor, no cubierto por esta decisión.
