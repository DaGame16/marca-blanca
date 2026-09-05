# Módulo Usuarios

> Resumen vivo. Se actualiza *in place* cada vez que el módulo cambia.

## 1. Qué es y qué NO es

**Usuario = la cuenta de un empleado de la empresa cliente (tenant).** Identidad, credenciales y datos básicos de la cuenta. Desde el 2026-09-03, este módulo **NO contiene la lógica de sesión** (JWT, refresh token) — eso vive en el módulo `autenticacion`, separado a propósito para que `usuarios` no sepa nada de tokens ni de mecanismos de login.

Le corresponde: correo, contraseña (cifrada), nombre completo, estado de la cuenta (activo/inactivo, bloqueo por intentos fallidos), y la ficha de la persona (`UsuarioPerfil`: cédula, teléfono, dirección, zona, cuadrilla). Fuera de alcance todavía: Rol/Permiso, y dos campos de salud del perfil (`tipoSangre`, `notasMedicas`) — existen en la tabla real pero están excluidos del dominio hasta que exista cifrado por columna (Ley 1581, ver changeset `seguridad-0004`).

## 2. Estructura de paquetes

```
usuarios/
├── usuarios-domain/.../usuarios/domain/
│   ├── Usuario.java, UsuarioPerfil.java, Correo.java, Contrasena.java, HashContrasena.java, EstadoUsuario.java
│   ├── CredencialesInvalidasException.java, UsuarioNoDisponibleException.java
│   ├── CorreoYaRegistradoException.java, UsuarioNoEncontradoException.java
│   └── port/out/
│       ├── RepositorioUsuarios.java
│       ├── RepositorioUsuarioPerfiles.java
│       └── CifradorDeContrasenas.java
├── usuarios-application/.../usuarios/application/
│   ├── GestionarUsuarioService.java
│   └── port/in/GestionarUsuario.java
└── usuarios-infrastructure/.../usuarios/infrastructure/
    ├── seguridad/
    │   └── BCryptCifradorDeContrasenas.java
    ├── persistencia/
    │   ├── UsuarioJpaEntity.java, UsuarioPerfilJpaEntity.java
    │   ├── UsuarioMapper.java
    │   ├── SpringDataUsuarioRepository.java, SpringDataUsuarioPerfilRepository.java
    │   ├── RepositorioUsuariosJpaAdapter.java, RepositorioUsuarioPerfilesJpaAdapter.java
    └── web/
        ├── UsuarioController.java
        ├── CrearUsuarioRequest.java, ActualizarUsuarioRequest.java, ActualizarPerfilRequest.java
        └── UsuarioResponse.java
```

**Qué se mudó a `autenticacion` (2026-09-03):** `AutenticarUsuario`, `AutenticarUsuarioService`, `ResultadoAutenticacion`, `GeneradorDeToken`, `VerificadorDeToken`, todo `seguridad/Jwt*`+`SecurityConfig`+`JwtAuthFilter`, y todo `web/` de login. Ver el README de `autenticacion` para el detalle.

**Sobre `port/out` en dominio vs. aplicación:** el criterio es quién usa el puerto directamente. `RepositorioUsuarios`, `RepositorioUsuarioPerfiles` y `CifradorDeContrasenas` viven en `domain/port/out/` porque el dominio los recibe como parámetro directo en sus métodos.

## 3. Dominio (`usuarios-domain`)

### Value objects
- **`Correo`**: valida formato con regex, normaliza (trim + minúsculas) al construirse.
- **`Contrasena`**: texto plano, solo en el instante del login/creación. Valida longitud mínima (8 caracteres). Nunca se persiste así.
- **`HashContrasena`**: el valor ya cifrado. El dominio no sabe con qué algoritmo se generó.
- **`EstadoUsuario`**: ya no es un campo persistido — se usa solo como parámetro de `UsuarioNoDisponibleException` (`INACTIVO` / `BLOQUEADO`), para distinguir el motivo en el mensaje de error.

### Entidad: `Usuario`
Campos: **`id`** (`Long`, BIGINT interno, nunca sale del backend) y **`uuid`** (`UUID`, identificador externo — el que se expone en el JWT y en la API), `correo`, `hashContrasena`, `nombreCompleto`, `activo` (boolean), `intentosFallidos`, `bloqueadoHasta`.

```java
public void verificarCredenciales(Contrasena contrasenaCandidata, CifradorDeContrasenas cifrador) {
    if (!activo) throw new UsuarioNoDisponibleException(EstadoUsuario.INACTIVO);
    if (estaBloqueado()) throw new UsuarioNoDisponibleException(EstadoUsuario.BLOQUEADO);
    if (!cifrador.verificar(contrasenaCandidata, hashContrasena)) {
        registrarIntentoFallido();
        throw new CredencialesInvalidasException();
    }
    reiniciarIntentosFallidos();
}
```
Vive dentro de `Usuario`, no en la capa de aplicación — regla de negocio real, no detalle de orquestación. Bloqueo automático tras 5 intentos fallidos (15 minutos), vía `intentosFallidos`/`bloqueadoHasta`.

**Pendiente conocido:** `verificarCredenciales()` muta este estado en memoria, pero `AutenticarUsuarioService` (módulo `autenticacion`) no vuelve a guardar el `Usuario` después de llamarlo — el bloqueo por intentos fallidos no persiste todavía entre peticiones. Falta un `guardar()` posterior en ese servicio.

### Entidad: `UsuarioPerfil` (nueva, 2026-09-04)
1:1 con `Usuario`. Campos: `idEmpleado`, `urlFoto`, `cedula`, `tipoDocumento`, `fechaNacimiento`, `telefono`, `direccion`, `contactoEmergencia`, `telefonoEmergencia`, `zona`, `cuadrillaId`, `estadoLaboral`. **No incluye** `tipoSangre` ni `notasMedicas` (ver sección 1).

### Excepciones
- `CredencialesInvalidasException`: correo inexistente O contraseña incorrecta — mismo mensaje para los dos, a propósito.
- `UsuarioNoDisponibleException`: cuenta encontrada pero no disponible (inactiva o bloqueada).
- `CorreoYaRegistradoException` (nueva): al crear un usuario con un correo ya existente.
- `UsuarioNoEncontradoException` (nueva): al buscar por `uuid` sin resultado.

### Puertos (`domain/port/out/`)
- `RepositorioUsuarios`: `buscarPorCorreo`, `buscarPorUuid`, `listarTodos`, `guardar`.
- `RepositorioUsuarioPerfiles` (nuevo): `buscarPorUsuarioId`, `guardar`.
- `CifradorDeContrasenas.cifrar(Contrasena): HashContrasena` / `.verificar(Contrasena, HashContrasena): boolean`

## 4. Aplicación (`usuarios-application`)

**`GestionarUsuario`** (puerto de entrada) + **`GestionarUsuarioService`** — el CRUD completo: `crear`, `actualizar`, `activar`, `desactivar`, `consultarPorUuid`, `listar`, `actualizarPerfil`.

## 5. Infraestructura (`usuarios-infrastructure`)

### `seguridad/`
- **`BCryptCifradorDeContrasenas`**: implementa `CifradorDeContrasenas` con `BCryptPasswordEncoder` de Spring Security.

### `persistencia/`
- **`UsuarioJpaEntity`** / **`UsuarioPerfilJpaEntity`**: mapeo JPA de `seguridad.tbl_usuarios` y `seguridad.tbl_usuario_perfiles` (viven en la base de cada empresa). Dos identificadores por fila: `id` (BIGINT interno) y `uuid` (externo).
- **`UsuarioMapper`**: traduce entre dominio y entidades JPA en ambas direcciones.
- **`SpringDataUsuarioRepository`** / **`SpringDataUsuarioPerfilRepository`**: consultas Spring Data derivadas.
- **`RepositorioUsuariosJpaAdapter`** / **`RepositorioUsuarioPerfilesJpaAdapter`**: implementan los puertos del dominio.
- Usa la segunda unidad de persistencia ("cliente"), configurada en `bootstrap` (`ConfiguracionPersistenciaCliente`). Ver README de `empresas` para el enrutamiento multi-tenant.

### `web/`
- **`UsuarioController`**: expone el CRUD vía REST (ver sección 6).
- DTOs: `CrearUsuarioRequest`, `ActualizarUsuarioRequest`, `ActualizarPerfilRequest`, `UsuarioResponse`.

**Pendiente conocido:** no existe todavía un `@ExceptionHandler` para `CorreoYaRegistradoException` / `UsuarioNoEncontradoException` — hoy no se traducen a códigos HTTP (409 / 404).

## 6. Contrato REST

| Método | Ruta | Body | Respuesta |
|---|---|---|---|
| POST | `/api/v1/usuarios` | `CrearUsuarioRequest` | 201 + `UsuarioResponse` |
| GET | `/api/v1/usuarios` | — | `UsuarioResponse[]` |
| GET | `/api/v1/usuarios/{uuid}` | — | `UsuarioResponse` |
| PUT | `/api/v1/usuarios/{uuid}` | `ActualizarUsuarioRequest` | `UsuarioResponse` |
| PUT | `/api/v1/usuarios/{uuid}/activar` | — | 204 |
| PUT | `/api/v1/usuarios/{uuid}/desactivar` | — | 204 |
| PUT | `/api/v1/usuarios/{uuid}/perfil` | `ActualizarPerfilRequest` | — |

## 7. Decisiones de diseño relacionadas

- [`decisiones/2026-09-03-0001-identificador-empresa-login.md`](decisiones/2026-09-03-0001-identificador-empresa-login.md) — se queda acá aunque el código descrito (`LoginRequest`) ya vive en `autenticacion`: describe una decisión tomada cuando el login todavía era parte de este módulo, es historia, no se mueve.
- [`decisiones/2026-09-03-0002-cifrado-bcrypt-y-jwt.md`](decisiones/2026-09-03-0002-cifrado-bcrypt-y-jwt.md) — mismo caso; la parte de BCrypt sigue siendo válida hoy.
- Las ADR 0003 y 0004 (refresh token) viven en [`autenticacion/decisiones/`](../autenticacion/decisiones/).

## 8. Pendiente / próximos pasos

- ✅ ~~Persistencia real vía JPA~~ — resuelto 2026-09-04.
- ✅ ~~Enrutamiento multi-tenant~~ — resuelto (módulo `empresas`).
- ✅ ~~CRUD de usuarios~~ — resuelto 2026-09-05 (`GestionarUsuarioService` + `UsuarioController`).
- **Persistir el bloqueo por intentos fallidos** después de `verificarCredenciales()` — pendiente en `autenticacion` (ver sección 3).
- **Manejador de errores HTTP** para `CorreoYaRegistradoException` / `UsuarioNoEncontradoException` — pendiente.
- **Campos de salud del perfil** (`tipoSangre`, `notasMedicas`) — excluidos hasta que exista cifrado por columna.
- **Formato de cédula** — sin definir (¿solo cédula de ciudadanía, o también extranjería/pasaporte?).
- **Rol y Permiso** — sin empezar.

## 9. Cómo compilar/probar localmente

```bash
mvn -f backend/pom.xml -pl usuarios/usuarios-domain,usuarios/usuarios-application,usuarios/usuarios-infrastructure -am install -DskipTests
```

Para verificar arquitectura y compilación completa del backend:
```bash
mvn -f backend/pom.xml -pl bootstrap -am clean test
```

---

## Historial de cambios

- **2026-09-03** — Luis — Documentación inicial: login completo (dominio, aplicación, infraestructura).
- **2026-09-04** — Luis — Reescrito tras la separación de `autenticacion`: módulo reducido a identidad + persistencia real vía JPA.
- **2026-09-05** — Carlos — CRUD completo de usuarios (`GestionarUsuarioService`, `UsuarioController`, `UsuarioPerfil`). Reconciliado con el trabajo paralelo de Luis en `autenticacion`: se resolvió una duplicación de adaptadores de persistencia (`RepositorioUsuariosJpa` de Luis vs. `RepositorioUsuariosJpaAdapter`, se conservó este último) y se actualizaron 3 archivos de `autenticacion` para usar `uuid` en vez del `id` interno, coordinado directamente con Luis.
