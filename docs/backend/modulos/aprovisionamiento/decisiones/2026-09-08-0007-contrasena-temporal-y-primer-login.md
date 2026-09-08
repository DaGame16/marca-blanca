# ADR 0007 — Contraseña temporal en el alta, cambio forzado en el primer login

**Fecha:** 2026-09-08
**Estado:** Aceptada
**Módulos afectados:** aprovisionamiento, autenticacion, usuarios

## Resumen

El usuario admin de una empresa nueva se crea con una **contraseña temporal
generada por el sistema**, marcada como tal (`seguridad.tbl_usuarios.es_contrasena_temporal`).
En el primer login el backend **obliga** a cambiarla antes de dejar usar
cualquier otro endpoint.

## Contexto

El correo de bienvenida debe llevar una contraseña que el cliente pueda usar de
inmediato, pero por seguridad no puede quedarse con una contraseña que generó y
envió la plataforma.

Requisito: el texto plano de la contraseña **no se persiste** en ningún lado —
solo existe en el instante en que se genera y se manda por correo.

## Decisión

### Generación (aprovisionamiento)

- La contraseña temporal se genera en el **último paso del pipeline**
  (`enviarBienvenida`), no en el registro. Ahí mismo se siembra el usuario admin
  en `db_cliente_<slug>` (hash BCrypt, `es_contrasena_temporal = true`), se envía
  el correo con el texto plano, y se marca `tbl_empresas.bienvenida_enviada_en`
  (idempotencia). El plano nunca sale del scope de ese método.

### Señalización (autenticacion)

- `verificarCredenciales` devuelve `debeCambiarContrasena` en `DatosDeUsuario`.
- Fluye a `ResultadoAutenticacion` → `LoginResponse.debeCambiarContrasena` (para
  que el frontend rutee a la pantalla de cambio).
- El JWT lleva el claim `pwd_temp`. `JwtVerificadorDeToken` lo lee →
  `UsuarioAutenticado.debeCambiarContrasena`.

### Enforcement (autenticacion)

- `JwtAuthFilter`: si el token tiene `pwd_temp` y la ruta **no** empieza con
  `/api/v1/auth/`, responde `403`. Ese token solo sirve para
  `POST /api/v1/auth/cambiar-contrasena`.
- `POST /api/v1/auth/cambiar-contrasena` (Bearer token): verifica la actual,
  guarda la nueva, `Usuario.cambiarContrasena(...)` limpia `es_contrasena_temporal`.
  El siguiente login emite un token sin `pwd_temp`.
- El acceso a `usuarios` sigue pasando **solo** por `AdaptadorVerificadorDeUsuarios`
  (único archivo de `autenticacion` que conoce `usuarios-domain` — ADR 0006 de
  `autenticacion`). Se le agregó el método `cambiarContrasena`.

## Consecuencias

- El usuario admin **no existe** hasta que corre el paso `bienvenida_enviada` del
  pipeline: entre `empresa_activada` y `bienvenida_enviada` la empresa está
  `activa` pero sin nadie que pueda loguearse. Es una ventana breve.
- El enforcement es por claim en el JWT (no consulta la BD en cada request). Un
  token viejo con `pwd_temp` sigue bloqueado aunque la contraseña ya se haya
  cambiado, hasta que expire o se renueve — comportamiento aceptable.
- Bases de cliente creadas antes de aplicar `cliente/seguridad/0014` a la
  plantilla no tienen la columna `es_contrasena_temporal` — hay que recrearlas
  o migrarlas.
