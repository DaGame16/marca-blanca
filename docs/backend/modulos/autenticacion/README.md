# Módulo Autenticación

> Resumen vivo. Se actualiza *in place* cada vez que el módulo cambia.

## 1. Qué es y qué NO es

**Autenticación = todo lo relacionado a la sesión:** login, access token (JWT), refresh token, su rotación. Separado de `usuarios` (2026-09-03) porque "quién sos" (identidad) y "cómo probás que seguís siendo vos entre requests" (sesión) son responsabilidades distintas.

Depende de `usuarios-domain` (necesita `Usuario`, `Correo`, `Contrasena`, `RepositorioUsuarios`, `CifradorDeContrasenas`) y de `empresas-application` (necesita `ContextoEmpresaActual` — ver sección 4) — nunca al revés.

**NO le corresponde:** verificar si una contraseña es correcta (vive en `Usuario.verificarCredenciales()`), ni resolver a qué base de datos conectarse (eso es `empresas`).

## 2. Estructura de paquetes

```
autenticacion/
├── autenticacion-domain/.../autenticacion/domain/
│   └── TokenDeRefrescoInvalidoException.java
├── autenticacion-application/.../autenticacion/application/
│   ├── AutenticarUsuarioService.java, RenovarTokenService.java
│   ├── ResultadoAutenticacion.java
│   ├── GeneradorTokenDeRefresco.java   <- utilidad interna, package-private
│   └── port/
│       ├── in/AutenticarUsuario.java, RenovarToken.java
│       └── out/GeneradorDeToken.java, VerificadorDeToken.java,
│              UsuarioAutenticado.java, AlmacenDeTokensDeRefresco.java
└── autenticacion-infrastructure/.../autenticacion/infrastructure/
    ├── seguridad/
    │   ├── JwtGeneradorDeToken.java, JwtVerificadorDeToken.java
    │   ├── JwtAuthFilter.java, SecurityConfig.java
    ├── persistencia/
    │   ├── SesionEntity.java, SesionJpaRepository.java
    │   └── AlmacenDeTokensDeRefrescoJpa.java
    ├── web/
    │   ├── AuthController.java
    │   ├── LoginRequest.java, LoginResponse.java
    │   ├── RefreshRequest.java, RefreshResponse.java
    │   ├── ErrorResponse.java, ManejadorErroresAuth.java
    └── ConfiguracionAutenticacion.java
```

## 3. Dominio (`autenticacion-domain`)

- **`TokenDeRefrescoInvalidoException`**: refresh token inexistente, ya usado (rotación) o expirado.

## 4. Aplicación (`autenticacion-application`)

### `AutenticarUsuarioService` (login)
Busca el `Usuario`, verifica credenciales, lee `ContextoEmpresaActual` (**ya establecido por `AuthController` antes de llamar acá** — no lo recibe como parámetro, para no ensuciar el puerto `AutenticarUsuario` con un detalle de multi-tenancy) para saber qué empresa embeber en el token, genera access + refresh token.

### `RenovarTokenService` (refresh)
Valida el refresh token (hash + expiración + no usado antes), lo invalida (rotación), genera un par nuevo — leyendo la empresa del mismo `ContextoEmpresaActual`.

### `GeneradorTokenDeRefresco` (utilidad interna)
Genera el valor aleatorio (256 bits) y lo hashea con **SHA-256, no BCrypt** — BCrypt protege contraseñas humanas adivinables; un valor de alta entropía no lo necesita.

### Puertos de salida
- `GeneradorDeToken.generarPara(Usuario, String identificadorEmpresa): String` — el access token, ahora recibe la empresa para embeberla como claim.
- `VerificadorDeToken.verificar(String): Optional<UsuarioAutenticado>` — antes devolvía solo el UUID de usuario; ahora trae usuario **y** empresa juntos (ver ADR 0005).
- `AlmacenDeTokensDeRefresco`: `guardar(...)`, `buscarUsuarioPorHashActivo(...)`, `eliminarPorHash(...)`, `eliminarTodosDeUsuario(...)`.

### `UsuarioAutenticado`
`record(UUID usuarioId, String identificadorEmpresa)` — lo que sale de verificar un JWT.

### `ResultadoAutenticacion`
`record(UUID usuarioId, String token, String refreshToken)`.

## 5. Infraestructura (`autenticacion-infrastructure`)

### `seguridad/`
- **`JwtGeneradorDeToken`**: agrega el claim `empresa` al token, además de `sub` (usuario) y `correo`. Access token expira en **15 minutos**.
- **`JwtVerificadorDeToken`**: extrae usuario **y** empresa del token, devuelve `UsuarioAutenticado`.
- **`JwtAuthFilter`**: en cada request con JWT válido, marca al usuario autenticado, **establece `ContextoEmpresaActual`** (limpiándolo en `finally`) y deja la empresa como atributo del request (`identificadorEmpresa`) — así cualquier controller protegido por JWT puede leerla sin volver a decodificar el token (lo usa, por ejemplo, `identidad-visual`).
- **`SecurityConfig`**: `/api/v1/auth/**` y `/api/v1/admin/**` públicos (se protegen distinto), todo lo demás exige JWT válido.

### ⚠️ Bug crítico encontrado y corregido (2026-09-04)
`ContextoEmpresaActual` nunca se establecía en ningún lado del código — se había construido la pieza que lo *lee* (`EnrutadorDataSourcePorEmpresa`) pero nunca la que lo *escribe*. **Cualquier login real fallaba** con `IllegalStateException: No hay empresa activa en el contexto de la peticion`. No se detectó antes porque las pruebas solo confirmaban que la app arrancaba, nunca que un login real completara. Arreglado en `AuthController`: establece el contexto desde el body del request (`identificadorEmpresa`) antes de cada operación de login/refresh, lo limpia después (`finally`).

### `persistencia/`
- **`SesionEntity`**: mapeo de `seguridad.tbl_sesiones`. `usuario_id` como `Long` plano (sin `@ManyToOne`) para no depender de `UsuarioEntity`.
- **`AlmacenDeTokensDeRefrescoJpa`**: rotación real — `eliminarPorHash` antes de emitir el token nuevo.
- Duración del refresh token: **7 días, fija por ahora**.

### `web/`
- **`AuthController`**: `POST /login`, `POST /refresh`. Establece/limpia `ContextoEmpresaActual` alrededor de cada operación (ver bug de arriba).
- **`ConfiguracionAutenticacion`**: conecta los servicios de aplicación como beans.

## 6. Contrato REST

| Método | Ruta | Body | Respuesta OK | Errores |
|---|---|---|---|---|
| POST | `/api/v1/auth/login` | `{correo, contrasena, identificadorEmpresa}` | 200 + `{usuarioId, token, refreshToken}` | 401 · 403 |
| POST | `/api/v1/auth/refresh` | `{refreshToken, identificadorEmpresa}` | 200 + `{usuarioId, token, refreshToken}` | 401 (`TokenDeRefrescoInvalidoException`) |

`identificadorEmpresa` en `/refresh` se agregó el 2026-09-04 — antes el refresh tampoco funcionaba, por el mismo motivo que el login.

## 7. Decisiones de diseño relacionadas

- [`decisiones/2026-09-03-0003-refresh-token-revocable.md`](decisiones/2026-09-03-0003-refresh-token-revocable.md) — propuesta original (Redis). Superada por la 0004.
- [`decisiones/2026-09-03-0004-refresh-token-postgres-tbl-sesiones.md`](decisiones/2026-09-03-0004-refresh-token-postgres-tbl-sesiones.md) — decisión vigente (Postgres, `tbl_sesiones`).
- [`decisiones/2026-09-04-0005-jwt-lleva-empresa-como-claim.md`](decisiones/2026-09-04-0005-jwt-lleva-empresa-como-claim.md) — por qué el JWT ahora lleva la empresa, y el bug crítico que esto resolvió de paso.

## 8. Pendiente / próximos pasos

- **Detección de reutilización con revocación en cascada** — anotado, no implementado.
- **Duración diferenciada web/móvil** — hoy 7 días fijo para todos.
- **Comentario en el código sobre el hotspot de CSRF** — pendiente decidir.
- **Protección contra fuerza bruta en `/login`** — en construcción por otro miembro del equipo, en `usuarios`.
- **Modelo de un usuario = una sola empresa** — si algún día un usuario necesita pertenecer a varias empresas, el claim `empresa` (hoy un solo valor) requiere rediseño (ver ADR 0005).

## 9. Cómo compilar/probar localmente

```bash
mvn -f backend/pom.xml install -DskipTests -pl autenticacion/autenticacion-domain,autenticacion/autenticacion-application,autenticacion/autenticacion-infrastructure -am
```

---

## Historial de cambios

- **2026-09-04** — Luis — Documentación inicial, tras la separación de `usuarios` y la implementación completa del refresh token.
- **2026-09-04** — Luis — JWT lleva la empresa como claim; corregido el bug crítico de `ContextoEmpresaActual` nunca establecido (login/refresh no funcionaban de verdad); `RefreshRequest` ahora exige `identificadorEmpresa`.
