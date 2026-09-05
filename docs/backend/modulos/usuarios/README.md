# Módulo Usuarios

> Resumen vivo. Se actualiza *in place* cada vez que el módulo cambia.

## 1. Qué es y qué NO es

**Usuario = la cuenta de un empleado de la empresa cliente (tenant).** Identidad y credenciales, nada más. Desde el 2026-09-03, este módulo **NO contiene la lógica de sesión** (JWT, refresh token) — eso vive en el módulo `autenticacion`, separado a propósito para que `usuarios` no sepa nada de tokens ni de mecanismos de login.

Le corresponde: correo, contraseña (cifrada), estado de la cuenta. Fuera de alcance todavía: datos adicionales de la persona (cédula, nombre completo), Rol/Permiso, y el caso de uso de registro de cuenta — nada de esto está implementado.

## 2. Estructura de paquetes

```
usuarios/
├── usuarios-domain/.../usuarios/domain/
│   ├── Usuario.java, Correo.java, Contrasena.java, HashContrasena.java, EstadoUsuario.java
│   ├── CredencialesInvalidasException.java, UsuarioNoDisponibleException.java
│   └── port/out/
│       ├── RepositorioUsuarios.java
│       └── CifradorDeContrasenas.java
├── usuarios-application/.../usuarios/application/
│   └── package-info.java   <- VACÍO por ahora, ver sección 4
└── usuarios-infrastructure/.../usuarios/infrastructure/
    ├── seguridad/
    │   └── BCryptCifradorDeContrasenas.java
    └── persistencia/
        ├── UsuarioEntity.java
        ├── UsuarioJpaRepository.java
        └── RepositorioUsuariosJpa.java
```

**Qué se mudó a `autenticacion` (2026-09-03):** `AutenticarUsuario`, `AutenticarUsuarioService`, `ResultadoAutenticacion`, `GeneradorDeToken`, `VerificadorDeToken`, todo `seguridad/Jwt*`+`SecurityConfig`+`JwtAuthFilter`, y todo `web/`. Ver el README de `autenticacion` para el detalle.

**Sobre `port/out` en dominio vs. aplicación:** el criterio es quién usa el puerto directamente. `RepositorioUsuarios` y `CifradorDeContrasenas` viven en `domain/port/out/` porque `Usuario.verificarCredenciales()` los recibe como parámetro directo.

## 3. Dominio (`usuarios-domain`)

### Value objects
- **`Correo`**: valida formato con regex, normaliza (trim + minúsculas) al construirse.
- **`Contrasena`**: texto plano, solo en el instante del login. Valida longitud mínima (8 caracteres). Nunca se persiste así.
- **`HashContrasena`**: el valor ya cifrado. El dominio no sabe con qué algoritmo se generó.
- **`EstadoUsuario`**: `ACTIVO`, `INACTIVO`, `BLOQUEADO`.

### Entidad: `Usuario`
Campos: `id` (UUID — identificador externo, expuesto en el JWT), `correo`, `hashContrasena`, `estado`.

```java
public void verificarCredenciales(Contrasena contrasenaCandidata, CifradorDeContrasenas cifrador) {
    if (estado != EstadoUsuario.ACTIVO) throw new UsuarioNoDisponibleException(estado);
    if (!cifrador.verificar(contrasenaCandidata, hashContrasena)) throw new CredencialesInvalidasException();
}
```
Vive dentro de `Usuario`, no en la capa de aplicación — regla de negocio real, no detalle de orquestación.

### Excepciones
- `CredencialesInvalidasException`: correo inexistente O contraseña incorrecta — mismo mensaje para los dos, a propósito.
- `UsuarioNoDisponibleException`: cuenta encontrada pero no `ACTIVO`.

### Puertos (`domain/port/out/`)
- `RepositorioUsuarios.buscarPorCorreo(Correo): Optional<Usuario>`
- `RepositorioUsuarios.buscarPorId(UUID): Optional<Usuario>` — agregado para el flujo de renovación de token en `autenticacion` (necesita reconstruir el `Usuario` a partir del id que vino en el refresh token, no tiene el correo a mano).
- `CifradorDeContrasenas.cifrar(Contrasena): HashContrasena` / `.verificar(Contrasena, HashContrasena): boolean`

## 4. Aplicación (`usuarios-application`) — vacía a propósito

No hay ningún caso de uso propio de `usuarios` todavía — todo lo que existía acá (el login) se mudó a `autenticacion`. Cuando se implemente el CRUD real de usuarios (crear cuenta, editar, desactivar), va a vivir en este paquete.

## 5. Infraestructura (`usuarios-infrastructure`)

### `seguridad/`
- **`BCryptCifradorDeContrasenas`**: implementa `CifradorDeContrasenas` con `BCryptPasswordEncoder` de Spring Security. Es lo único que quedó de la carpeta `seguridad/` original — todo lo de JWT se fue con la mudanza.

### `persistencia/` (nuevo, 2026-09-04)
- **`UsuarioEntity`**: mapeo JPA de `seguridad.tbl_usuarios` (vive en la base de **cada empresa**, no en la de control). Dos identificadores por fila, como el resto del esquema: `id` (BIGINT interno, nunca sale de esta clase) y `uuid` (el que sí se expone — es el mismo que usa `Usuario.id` en el dominio).
- **`UsuarioJpaRepository`**: consultas Spring Data derivadas (`findByCorreo`, `findByUuid`).
- **`RepositorioUsuariosJpa`**: implementa `RepositorioUsuarios`. Traduce `es_activo` + `bloqueado_hasta` (dos columnas separadas en la tabla) al `EstadoUsuario` único que espera el dominio: bloqueado si `bloqueado_hasta` es futuro, si no inactivo si `es_activo=false`, si no activo.
- Usa la **segunda unidad de persistencia** ("cliente"), configurada en `bootstrap` (`ConfiguracionPersistenciaCliente`) — no la misma conexión que usa la base de control. Ver README de `empresas` para el enrutamiento multi-tenant.

## 6. Contrato REST

**Ninguno.** Este módulo no expone ningún endpoint propio — el login vive en `autenticacion`.

## 7. Decisiones de diseño relacionadas

- [`decisiones/2026-09-03-0001-identificador-empresa-login.md`](decisiones/2026-09-03-0001-identificador-empresa-login.md) — se queda acá aunque el código descrito (`LoginRequest`) ya vive en `autenticacion`: describe una decisión tomada cuando el login todavía era parte de este módulo, es historia, no se mueve.
- [`decisiones/2026-09-03-0002-cifrado-bcrypt-y-jwt.md`](decisiones/2026-09-03-0002-cifrado-bcrypt-y-jwt.md) — mismo caso; la parte de BCrypt sigue siendo válida hoy, la de JWT describe la decisión original antes de la separación.
- Las ADR 0003 y 0004 (refresh token) se mudaron a [`autenticacion/decisiones/`](../autenticacion/decisiones/) — describen código que nunca llegó a vivir en `usuarios`.

## 8. Pendiente / próximos pasos

- ✅ ~~Persistencia real vía JPA~~ — resuelto 2026-09-04.
- ✅ ~~Enrutamiento multi-tenant~~ — resuelto (módulo `empresas`).
- **CRUD de usuarios** (crear, editar, desactivar cuenta) — sin empezar, `usuarios-application` sigue vacía.
- **Rol y Permiso** — sin empezar.
- **Fuerza bruta / bloqueo de cuenta** — en construcción por otro miembro del equipo (usa `intentos_fallidos`/`bloqueado_hasta`, ya en la tabla). Hay una versión de `Usuario.java` con esta lógica en revisión — todavía no aplicada, tiene una incompatibilidad conocida con el tipo de `getId()` que hay que resolver antes de fusionarla.

## 9. Cómo compilar/probar localmente

```bash
mvn -f backend/pom.xml install -DskipTests -pl usuarios/usuarios-domain,usuarios/usuarios-application,usuarios/usuarios-infrastructure -am
```

---

## Historial de cambios

- **2026-09-03** — Luis — Documentación inicial: login completo (dominio, aplicación, infraestructura).
- **2026-09-04** — Luis — Reescrito tras la separación de `autenticacion`: módulo reducido a identidad + persistencia real vía JPA. `buscarPorId` agregado al puerto.
