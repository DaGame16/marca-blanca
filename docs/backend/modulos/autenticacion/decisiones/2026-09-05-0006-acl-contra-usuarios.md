# ADR 0006 — Anti-Corruption Layer entre `autenticacion` y `usuarios`

**Fecha:** 2026-09-05
**Estado:** Aceptada

## Resumen

`autenticacion` deja de importar el dominio de `usuarios` (`Usuario`, `Correo`, `Contrasena`, `RepositorioUsuarios`, `CifradorDeContrasenas`, y sus excepciones) directo. Ahora depende únicamente de un puerto propio (`VerificadorDeUsuarios`), implementado por un único adaptador (`AdaptadorVerificadorDeUsuarios`) — el único archivo de todo el módulo que sigue conociendo el dominio de `usuarios`.

## Contexto

Leyendo el código real se confirmó que la dependencia directa no estaba en un solo lugar — eran **6 archivos**: los dos `pom.xml` de `autenticacion`, `AutenticarUsuarioService`, `RenovarTokenService`, el propio puerto `GeneradorDeToken` (que hablaba en términos de `Usuario`, el tipo ajeno), `ConfiguracionAutenticacion`, y hasta `ManejadorErroresAuth` (el manejo de errores HTTP también estaba acoplado). En vocabulario de *context mapping* (DDD), esto era un **Shared Kernel** entre ambos módulos — válido como decisión consciente, pero acopla.

**La motivación dejó de ser teórica en medio de esta misma sesión de trabajo:** el CRUD de usuarios de un compañero (`feature/usuarios-crud`) se fusionó a `develop` y tuvo que revertirse con un hotfix — coincidiendo con un bug ya advertido (`Usuario.getId()` podía devolver `Long` en vez de `UUID`, rompiendo el JWT). Con la dependencia directa que existía hasta ese momento, un cambio así en `usuarios` podía romper la compilación de `autenticacion` sin ningún aviso previo.

## Opciones evaluadas

| Opción | Consideración |
|---|---|
| Exponer un puerto nuevo del lado de `usuarios-application` (hoy vacío) | Requiere coordinar con quien construya el CRUD de usuarios (Carlos); un archivo puesto de antemano ahí podría estorbarle. |
| **Adaptador único en `autenticacion-infrastructure`, cero archivos en `usuarios`** (elegida) | `usuarios-application` queda completamente libre — Carlos puede construir el CRUD sin encontrar nada puesto de antemano ni tener que coordinar con lo que ya existe en `autenticacion`. |

## Decisión

- `autenticacion-domain`: `CredencialesInvalidasException`, `UsuarioNoDisponibleException` propias — reemplazan a las de `usuarios.domain` en todo lo que hace `autenticacion`.
- `autenticacion-application/port/out`: `DatosDeUsuario` (record mínimo: `id` + `correo`, nada del dominio ajeno), `VerificadorDeUsuarios` (el puerto propio: `verificarCredenciales(...)`, `buscarPorId(...)`).
- `autenticacion-infrastructure`: `AdaptadorVerificadorDeUsuarios` — implementa el puerto usando `RepositorioUsuarios`/`CifradorDeContrasenas`/`Usuario` de `usuarios.domain` por dentro, **atrapa** las excepciones ajenas y lanza las propias. Es el único punto de todo el módulo con ese conocimiento.
- `GeneradorDeToken.generarPara(...)` cambia de firma: recibe `DatosDeUsuario`, no `Usuario`.
- Se quita la dependencia a `usuarios-domain` del `pom.xml` de `autenticacion-application` — `autenticacion-infrastructure` la conserva a propósito, es el único punto permitido.

## Consecuencias

| Capa | Impacto |
|---|---|
| Dominio | `autenticacion-domain` gana 2 excepciones propias. |
| Aplicación | `AutenticarUsuarioService`/`RenovarTokenService`/`ConfiguracionAutenticacion` ya no importan nada de `usuarios` — solo `VerificadorDeUsuarios`/`DatosDeUsuario`, ambos propios. |
| Infraestructura | Un archivo nuevo (`AdaptadorVerificadorDeUsuarios`) concentra toda la traducción. |
| Duplicación | `DatosDeUsuario` vs. `Usuario` — mismo campo `correo`/`id` representado dos veces, en dos tipos distintos. Aceptado a propósito: es exactamente el punto del patrón, no un descuido. |

## Cómo se podría revertir o evolucionar

Si en el futuro `usuarios-application` deja de estar vacío y Carlos construye ahí una capa de aplicación real, se podría reevaluar si el adaptador debería consumir esa capa de aplicación en vez de los puertos de dominio directo — sin que eso cambie nada del lado de `autenticacion`, ya que el puerto `VerificadorDeUsuarios` queda igual.
