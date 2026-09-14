# Módulo Usuarios

> Resumen vivo. Se actualiza in-place. Historial de decisiones en `decisiones/`.

## 1. Qué es

Usuario = cuenta de un empleado del tenant (correo, contraseña, nombre, estado). Autenticación (JWT, sesión) vive en el módulo `autenticacion`, separado.

## 2. Estructura de paquetes (post-ADR 0003)

usuarios/
- usuarios-domain/.../domain/
  - Usuario.java, UsuarioPerfil.java, EstadoCuenta.java, Correo.java, Contrasena.java, HashContrasena.java
  - CredencialesInvalidasException.java, UsuarioNoDisponibleException.java, CorreoYaRegistradoException.java, UsuarioNoEncontradoException.java
  - (sin puertos — dominio con dependencia cero, ver ADR 0003)
- usuarios-application/.../application/
  - GestionarUsuarioService.java
  - port/in/GestionarUsuario.java
  - port/out/RepositorioUsuarios.java, RepositorioUsuarioPerfiles.java, CifradorDeContrasenas.java
- usuarios-infrastructure/.../infrastructure/
  - seguridad/BCryptCifradorDeContrasenas.java
  - persistencia/UsuarioJpaEntity.java, UsuarioPerfilJpaEntity.java, EstadoCuentaEmbeddable.java, UsuarioMapper.java, SpringDataUsuarioRepository.java, SpringDataUsuarioPerfilRepository.java, RepositorioUsuariosJpaAdapter.java, RepositorioUsuarioPerfilesJpaAdapter.java
  - ConfiguracionUsuarios.java
  - web/UsuarioController.java, DTOs

## 3. Regla de dependencia

`domain` → nada. `application` → solo `domain`. `infrastructure` → `application` y `domain`. Verificado por `ArquitecturaHexagonalTest` (ArchUnit).

## 4. Contrato REST

Desde 2026-09-14, cada endpoint exige el permiso granular correspondiente (`@PreAuthorize`) — ver [módulo `roles`](../roles/README.md#5-catálogo-de-permisos-vigente).

| Método | Ruta | Permiso | Body | Respuesta |
|---|---|---|---|---|
| POST | `/api/v1/usuarios` | `usuarios:crear` | `CrearUsuarioRequest` | 201 + `UsuarioResponse` |
| GET | `/api/v1/usuarios` | `usuarios:leer` | — | `UsuarioResponse[]` |
| GET | `/api/v1/usuarios/{uuid}` | `usuarios:leer` | — | `UsuarioResponse` |
| PUT | `/api/v1/usuarios/{uuid}` | `usuarios:editar` | `ActualizarUsuarioRequest` | `UsuarioResponse` |
| PUT | `/api/v1/usuarios/{uuid}/activar` | `usuarios:activar` | — | 204 |
| PUT | `/api/v1/usuarios/{uuid}/desactivar` | `usuarios:desactivar` | — | 204 |
| PUT | `/api/v1/usuarios/{uuid}/perfil` | `usuarios:editar` | `ActualizarPerfilRequest` | — |

## 5. Pendientes conocidos

- Manejador de errores HTTP para `CorreoYaRegistradoException`/`UsuarioNoEncontradoException` (404/409).
- Persistencia del bloqueo por intentos fallidos tras `verificarCredenciales()` (falta `guardar()` en `AutenticarUsuarioService`).
- Campos de salud del perfil (`tipoSangre`, `notasMedicas`) — excluidos, pendientes de cifrado por columna.
- `ShellComponent` (frontend) sin resolver — no relacionado a este módulo.

## Historial de cambios

- 2026-09-05 — Carlos — CRUD completo, reconciliado con `autenticacion`.
- 2026-09-07 — Carlos — Puertos movidos de `domain` a `application` (ADR 0003).
- 2026-09-08 — Leidi — `Usuario` gana `esContrasenaTemporal` (mapeado a `seguridad.tbl_usuarios.es_contrasena_temporal`); `cambiarContrasena(...)` lo limpia. Lo usa el flujo de primer login — ver [ADR 0007 de aprovisionamiento](../aprovisionamiento/decisiones/2026-09-08-0007-contrasena-temporal-y-primer-login.md).
- 2026-09-14 — Leidi — `UsuarioController` protegido con `@PreAuthorize` por acción (`usuarios:leer/crear/editar/activar/desactivar`) — ver [módulo `roles`](../roles/README.md).