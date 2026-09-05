# Módulo Autenticación

> Resumen vivo. Se actualiza *in place* cada vez que el módulo cambia.

## 1. Qué es y qué NO es

**Autenticación = todo lo relacionado a la sesión:** login, access token (JWT), refresh token, su rotación. Separado de `usuarios` (2026-09-03) porque "quién sos" (identidad) y "cómo probás que seguís siendo vos entre requests" (sesión) son responsabilidades distintas — mismo criterio que usa Odoo separando `base` (usuarios) de `auth_oauth`/`auth_ldap` (mecanismos de sesión).

Depende de `usuarios-domain` (necesita `Usuario`, `Correo`, `Contrasena`, `RepositorioUsuarios`, `CifradorDeContrasenas` para poder loguear) — nunca al revés.

**NO le corresponde:** verificar si una contraseña es correcta (esa regla vive en `Usuario.verificarCredenciales()`), ni saber a qué base de datos de empresa conectarse (eso es el módulo `empresas`).

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
│       └── out/GeneradorDeToken.java, VerificadorDeToken.java, AlmacenDeTokensDeRefresco.java
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

**`autenticacion-domain` casi vacío, a propósito:** la única regla de negocio real (verificar credenciales) sigue viviendo en `Usuario`, en `usuarios-domain` — no se duplicó acá. `TokenDeRefrescoInvalidoException` es la única regla propia de este módulo hasta ahora ("¿este refresh token sigue siendo válido?").

## 3. Dominio (`autenticacion-domain`)

- **`TokenDeRefrescoInvalidoException`**: se lanza cuando un refresh token no existe, ya fue usado (rotación) o expiró.

## 4. Aplicación (`autenticacion-application`)

### `AutenticarUsuarioService` (login)
Orquesta: busca el `Usuario` por correo, llama `verificarCredenciales()`, genera el access token, genera y guarda un refresh token nuevo. Devuelve los dos en `ResultadoAutenticacion`.

### `RenovarTokenService` (refresh)
Recibe el refresh token actual, lo hashea, busca a qué usuario pertenece (si sigue activo — no expirado, no ya usado). Si es válido: **lo invalida** (rotación), genera un access token nuevo y un refresh token nuevo, y guarda este último. Si el token no se encuentra activo, lanza `TokenDeRefrescoInvalidoException` — puede ser porque expiró, o porque alguien ya lo usó antes (señal de robo).

### `GeneradorTokenDeRefresco` (utilidad interna)
Genera el valor aleatorio del refresh token (256 bits, `SecureRandom`) y lo hashea con **SHA-256, no BCrypt**: BCrypt está pensado para ir lento a propósito, protegiendo contraseñas humanas fáciles de adivinar. Un refresh token ya es un valor aleatorio de alta entropía — BCrypt solo agregaría latencia sin sumar seguridad real.

### Puertos de salida
- `GeneradorDeToken.generarPara(Usuario): String` — el access token (JWT).
- `VerificadorDeToken.verificar(String): Optional<UUID>`
- `AlmacenDeTokensDeRefresco`: `guardar(...)`, `buscarUsuarioPorHashActivo(...)`, `eliminarPorHash(...)`, `eliminarTodosDeUsuario(...)`.

### `ResultadoAutenticacion`
`record(UUID usuarioId, String token, String refreshToken)`.

## 5. Infraestructura (`autenticacion-infrastructure`)

### `seguridad/`
- **`JwtGeneradorDeToken`** / **`JwtVerificadorDeToken`**: JJWT 0.12.6. El access token expira en **15 minutos** (`app.jwt.expiracion-minutos`, bajado de 60 el 2026-09-04).
- **`JwtAuthFilter`** / **`SecurityConfig`**: `/api/v1/auth/**` público (login y refresh no necesitan JWT — todavía no hay sesión), `/api/v1/admin/**` público también (se protege distinto, ver README de `empresas`), todo lo demás exige JWT válido.

### `persistencia/`
- **`SesionEntity`**: mapeo de `seguridad.tbl_sesiones` (base de cada empresa). El campo `usuario_id` se guarda como `Long` plano, **sin relación `@ManyToOne`** — evita que este módulo dependa en código Java de `UsuarioEntity` (que vive en `usuarios-infrastructure`); el cruce se hace con JPQL por nombre de entidad, no por import.
- **`AlmacenDeTokensDeRefrescoJpa`**: implementa el puerto. La rotación es real — `eliminarPorHash` se llama antes de emitir el token nuevo.
- Usa la segunda unidad de persistencia ("cliente"), igual que `usuarios` — ver `bootstrap/ConfiguracionPersistenciaCliente`.
- **Duración del refresh token: 7 días, fija por ahora** (constante en el código, no configurable por variable de entorno todavía).

### `web/`
- **`AuthController`**: `POST /login`, `POST /refresh`.
- **`ConfiguracionAutenticacion`**: conecta `AutenticarUsuarioService`/`RenovarTokenService` como beans — los servicios de aplicación no tienen anotaciones de Spring a propósito.

## 6. Contrato REST

| Método | Ruta | Body | Respuesta OK | Errores |
|---|---|---|---|---|
| POST | `/api/v1/auth/login` | `{correo, contrasena, identificadorEmpresa}` | 200 + `{usuarioId, token, refreshToken}` | 401 · 403 |
| POST | `/api/v1/auth/refresh` | `{refreshToken}` | 200 + `{usuarioId, token, refreshToken}` | 401 (`TokenDeRefrescoInvalidoException`) |

## 7. Decisiones de diseño relacionadas

- [`decisiones/2026-09-03-0003-refresh-token-revocable.md`](decisiones/2026-09-03-0003-refresh-token-revocable.md) — propuesta original (Redis). Superada por la 0004.
- [`decisiones/2026-09-03-0004-refresh-token-postgres-tbl-sesiones.md`](decisiones/2026-09-03-0004-refresh-token-postgres-tbl-sesiones.md) — decisión vigente (Postgres, `tbl_sesiones`).
- Contexto histórico (código descrito ya no vive acá, pero explica el porqué del contrato de login): [`usuarios/decisiones/2026-09-03-0001`](../usuarios/decisiones/2026-09-03-0001-identificador-empresa-login.md), [`0002`](../usuarios/decisiones/2026-09-03-0002-cifrado-bcrypt-y-jwt.md).

## 8. Pendiente / próximos pasos

- **Detección de reutilización con revocación en cascada** — si un refresh token ya usado vuelve a aparecer, hoy solo se rechaza esa petición puntual; la mejora (cerrar sesión en todos los dispositivos) quedó anotada, no implementada.
- **Duración diferenciada web/móvil** — hoy 7 días fijo para todos los clientes; pendiente validar con Carlos (Angular) y el equipo de Flutter.
- **Comentario en el código sobre el hotspot de CSRF** — `SecurityConfig` tiene `csrf().disable()` marcado "Safe" en SonarQube, pendiente decidir si además se documenta con un comentario en el archivo.
- **Protección contra fuerza bruta en `/login`** — la lógica de dominio (`intentos_fallidos`/`bloqueado_hasta`) está siendo construida por otro miembro del equipo en `usuarios`, no en este módulo.

## 9. Cómo compilar/probar localmente

```bash
mvn -f backend/pom.xml install -DskipTests -pl autenticacion/autenticacion-domain,autenticacion/autenticacion-application,autenticacion/autenticacion-infrastructure -am
```

Para probar de punta a punta hace falta al menos una empresa cargada en la base de control (ver README de `empresas`, sección de datos de prueba — pendiente).

---

## Historial de cambios

- **2026-09-04** — Luis — Documentación inicial, tras la separación de `usuarios` y la implementación completa del refresh token (rotación, access token a 15 min, endpoint `/refresh`).
