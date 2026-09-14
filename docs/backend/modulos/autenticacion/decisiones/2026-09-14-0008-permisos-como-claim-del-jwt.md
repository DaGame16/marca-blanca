# ADR 0008 — El JWT lleva los permisos efectivos como claim

**Fecha:** 2026-09-14
**Estado:** Aceptada

## Resumen

El access token (JWT) ahora incluye los permisos efectivos del usuario como un claim adicional (`permisos`, lista de nombres), junto a `empresa` (ver ADR 0005) y `pwd_temp`.

## Contexto

El módulo `roles` (nuevo) calcula los permisos efectivos de un usuario (unión de los permisos de sus roles, ajustada por sus permisos puntuales — ver su README). Hacía falta un mecanismo para que los controllers puedan exigir un permiso concreto (`@PreAuthorize("hasAuthority('roles:gestionar')")`) sin que cada endpoint tenga que consultar la base de datos.

## Opciones evaluadas

| Opción | Consideración |
|---|---|
| Consultar los permisos en cada request desde `JwtAuthFilter` | Autorización siempre al día, pero agrega una consulta a la base de datos en **cada** request autenticado — impacto de rendimiento en el filtro que corre antes de cualquier controller. |
| **Embeber los permisos como claim del JWT** (elegida) | Cero consultas adicionales por request — se calculan una vez al emitir el token (login/refresh) y se leen del token ya verificado, igual que `empresa`. El costo es staleness: un cambio de permisos tarda hasta que expire el access token en reflejarse. |

Con `app.jwt.expiracion-minutos` en 15 (default) y el refresh token regenerando siempre el access token desde la base (`RenovarTokenService` vuelve a llamar a `ConsultarPermisosDeUsuario` en cada refresh), el staleness máximo de un cambio de permisos es de 15 minutos — aceptable, y mismo trade-off que ya se acepta para `empresa`.

## Decisión

- `GeneradorDeToken.generarPara(DatosDeUsuario, Set<String> permisos, String identificadorEmpresa)` — el puerto ahora recibe los permisos, y `JwtGeneradorDeToken` los agrega como claim (`permisos`).
- Nuevo puerto `ConsultarPermisosDeUsuario` en `autenticacion-application` (port/out), implementado por `AdaptadorConsultarPermisosDeUsuario` en `autenticacion-infrastructure` — único archivo de `autenticacion` que conoce `roles-application` (mismo criterio de ACL que `AdaptadorVerificadorDeUsuarios` hacia `usuarios-domain`, pero separado: identidad y autorización son ACLs distintos).
- `AutenticarUsuarioService`/`RenovarTokenService` llaman a `ConsultarPermisosDeUsuario` antes de generar el token.
- `UsuarioAutenticado` gana el campo `permisos` — `JwtVerificadorDeToken` lo parsea del claim.
- `JwtAuthFilter` arma `List<SimpleGrantedAuthority>` desde `permisos` en vez del `List.of()` que usaba antes (cero authorities, sin autorización real).
- `SecurityConfig` gana `@EnableMethodSecurity`, habilitando `@PreAuthorize` en los controllers.

## Consecuencias

| Capa | Impacto |
|---|---|
| Dominio | Ninguno. |
| Aplicación | `AutenticarUsuarioService`/`RenovarTokenService` dependen de un puerto nuevo (`ConsultarPermisosDeUsuario`) — mismo patrón que ya dependían de `VerificadorDeUsuarios`. |
| Infraestructura | `autenticacion-infrastructure` pasa a depender de `roles-application` (solo desde `AdaptadorConsultarPermisosDeUsuario`). Primer endpoint protegido de verdad con `@PreAuthorize`: los del propio módulo `roles`; el resto de módulos sigue sin retrofit (ver "Pendientes conocidos" del README de `roles`). |
| Seguridad | Los endpoints de `roles` ya no son accesibles por cualquier usuario autenticado — exigen `roles:leer`/`roles:gestionar`. Revocar un permiso tarda hasta 15 minutos en tomar efecto si el usuario no vuelve a hacer login o refresh. |

## Cómo se podría revertir o evolucionar

Si el staleness de 15 minutos deja de ser aceptable para algún caso de uso (ej. revocar acceso de forma inmediata ante un incidente), la alternativa es invalidar también el refresh token del usuario (fuerza un nuevo login) o, para casos verdaderamente críticos, desactivar la cuenta (`Usuario.desactivar()`, ya existente en `usuarios`) en vez de solo quitarle un permiso.
