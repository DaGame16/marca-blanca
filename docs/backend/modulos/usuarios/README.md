# Módulo Usuarios

## 1. Qué es y qué NO es

**Usuario = la cuenta de un empleado de la empresa cliente (tenant).** El módulo crea y autentica cuentas para el personal interno de la empresa que compró la plataforma — no para sus clientes finales, y no para los administradores de GuajiraNet (eso vive en otro lugar, la base transversal de control).

Le corresponden: correo, contraseña, estado de la cuenta. Datos adicionales de la persona (cédula, nombre completo, fecha de nacimiento) y el modelo de Rol/Permiso están **fuera de alcance de este documento** — se agregan cuando se implemente el caso de uso de registro de cuenta, todavía sin construir.

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
│   ├── AutenticarUsuarioService.java, ResultadoAutenticacion.java
│   └── port/
│       ├── in/AutenticarUsuario.java
│       └── out/GeneradorDeToken.java, VerificadorDeToken.java
└── usuarios-infrastructure/.../usuarios/infrastructure/
    ├── seguridad/
    │   ├── BCryptCifradorDeContrasenas.java, JwtGeneradorDeToken.java
    │   ├── JwtVerificadorDeToken.java, JwtAuthFilter.java, SecurityConfig.java
    └── web/
        ├── AuthController.java, LoginRequest.java, LoginResponse.java
        └── ErrorResponse.java, ManejadorErroresAuth.java
```

**Sobre `port/out` en dominio vs. `port/out` en aplicación:** no es una regla ciega de "todos los puertos van a application". El criterio es quién usa el puerto directamente. `RepositorioUsuarios` y `CifradorDeContrasenas` viven en `domain/port/out/` porque `Usuario.verificarCredenciales()` los recibe como parámetro directo — si estuvieran en `application`, el dominio tendría que importar algo de una capa que le está prohibido conocer. `GeneradorDeToken` y `VerificadorDeToken` sí están en `application/port/out/`, porque solo los usa la infraestructura a través de la aplicación, nunca el dominio.

## 3. Dominio (`usuarios-domain`)

### Value objects
- **`Correo`**: valida formato con regex, normaliza (trim + minúsculas) al construirse.
- **`Contrasena`**: representa el valor en texto plano, solo en el instante en que se recibe (login). Valida longitud mínima (8 caracteres). Nunca se persiste así.
- **`HashContrasena`**: el valor ya cifrado. El dominio no sabe con qué algoritmo se generó — eso es decisión de infraestructura.
- **`EstadoUsuario`**: `ACTIVO`, `INACTIVO`, `BLOQUEADO`.

### Entidad: `Usuario`
Campos: `id` (UUID), `correo`, `hashContrasena`, `estado`.

Regla de negocio central:
```java
public void verificarCredenciales(Contrasena contrasenaCandidata, CifradorDeContrasenas cifrador) {
    if (estado != EstadoUsuario.ACTIVO) throw new UsuarioNoDisponibleException(estado);
    if (!cifrador.verificar(contrasenaCandidata, hashContrasena)) throw new CredencialesInvalidasException();
}
```
Vive **dentro** de `Usuario`, no en la capa de aplicación — es una regla de negocio real ("quién puede autenticarse y bajo qué condiciones"), no un detalle de orquestación. Evita el patrón de dominio anémico.

### Excepciones
- `CredencialesInvalidasException`: correo inexistente O contraseña incorrecta — **mismo mensaje para los dos casos**, a propósito, para no revelar si un correo existe en el sistema.
- `UsuarioNoDisponibleException`: cuenta encontrada pero no `ACTIVO`. Nota de diseño: esto sí revela que la cuenta existe (mensaje distinto al de arriba) — decisión consciente por ahora, más útil para soporte interno; revisable si el equipo de seguridad prefiere unificarlo en un solo mensaje genérico.

### Puertos (`domain/port/out/`)
- `RepositorioUsuarios.buscarPorCorreo(Correo): Optional<Usuario>`
- `CifradorDeContrasenas.cifrar(Contrasena): HashContrasena` / `.verificar(Contrasena, HashContrasena): boolean`

## 4. Aplicación (`usuarios-application`)

### Puerto de entrada
`AutenticarUsuario.ejecutar(String correo, String contrasenaPlano): ResultadoAutenticacion` — es lo único que conoce la infraestructura (el controller).

### `AutenticarUsuarioService`
Orquestación pura, sin lógica de negocio propia: construye los value objects, busca el usuario (o lanza `CredencialesInvalidasException` si no existe), llama a `usuario.verificarCredenciales(...)`, pide el token, arma el resultado.

### Puertos de salida propios de esta capa
- `GeneradorDeToken.generarPara(Usuario): String`
- `VerificadorDeToken.verificar(String token): Optional<UUID>`

### `ResultadoAutenticacion`
`record(UUID usuarioId, String token)` — lo que el caso de uso devuelve hacia afuera.

## 5. Infraestructura (`usuarios-infrastructure`)

### `seguridad/`
- **`BCryptCifradorDeContrasenas`**: implementa `CifradorDeContrasenas` con `BCryptPasswordEncoder` de Spring Security.
- **`JwtGeneradorDeToken`**: implementa `GeneradorDeToken` con JJWT 0.12.6. El token lleva el id del usuario como `subject`, el correo como claim, y expira según `app.jwt.expiracion-minutos` (default 60).
- **`JwtVerificadorDeToken`**: implementa `VerificadorDeToken`. Verifica firma y expiración; cualquier problema (firma inválida, token vencido, formato incorrecto) se trata igual: devuelve vacío, nunca explota.
- **`JwtAuthFilter`**: corre en cada request. Lee el header `Authorization: Bearer <token>`; si es válido, marca la petición como autenticada con el id del usuario.
- **`SecurityConfig`**: `/api/v1/auth/**` público, todo lo demás exige autenticación (`.anyRequest().authenticated()`). El secreto JWT se lee de la variable de entorno `JWT_SECRET` — el valor por defecto en `application.yml` es explícitamente solo para desarrollo local, nunca usar en DEV compartido, QA o PROD.

### `web/`
- **`AuthController`**: expone `POST /api/v1/auth/login`.
- **`LoginRequest`**: `correo`, `contrasena`, `identificadorEmpresa`. Este último campo **se recibe pero todavía no se usa** — el enrutamiento real hacia la base de datos de la empresa correspondiente está pendiente (ver sección 8).
- **`LoginResponse`**: `usuarioId`, `token`.
- **`ManejadorErroresAuth`**: traduce excepciones de dominio a HTTP.

## 6. Contrato REST

| Método | Ruta | Body | Respuesta OK | Errores |
|---|---|---|---|---|
| POST | `/api/v1/auth/login` | `LoginRequest` | 200 + `LoginResponse` | 401 (`CredencialesInvalidasException`) · 403 (`UsuarioNoDisponibleException`) |

Formato de error (`ErrorResponse`): `{ codigo, mensaje, marcaDeTiempo, ruta }`.

## 7. Decisiones de diseño relacionadas

- [`decisiones/2026-09-03-0001-identificador-empresa-login.md`](decisiones/2026-09-03-0001-identificador-empresa-login.md)
- [`decisiones/2026-09-03-0002-cifrado-bcrypt-y-jwt.md`](decisiones/2026-09-03-0002-cifrado-bcrypt-y-jwt.md)

## 8. Pendiente / próximos pasos

- **Persistencia real** (`RepositorioUsuarios` vía JPA) — depende de la definición de tabla de Leidi. No se avanza sin eso.
- **Enrutamiento multi-tenant real** — cómo se usa `identificadorEmpresa` para conectar a la base de datos correcta. Mecanismo decidido (identificador explícito, no subdominio), implementación pendiente.
- **Formato de cédula** para el registro de cuenta — sin definir todavía (¿solo cédula de ciudadanía, o también extranjería/pasaporte?).
- **Rol y Permiso** — dominio sin empezar. Son conceptos separados de `Usuario`, no fusionados en la misma entidad.
- **Registro de cuenta** (creación de usuario nuevo) — caso de uso no implementado, solo existe login sobre usuarios ya existentes.

## 9. Cómo compilar/probar localmente

```bash
cd backend
mvn clean install -pl usuarios/usuarios-domain,usuarios/usuarios-application,usuarios/usuarios-infrastructure -am
```

No hay forma de probar el endpoint de punta a punta todavía — falta la persistencia real conectada a una base de datos.

---

## Historial de cambios

- **2026-09-03** — Luis — Documentación inicial: login completo (dominio, aplicación, infraestructura), puertos separados en `port/in`/`port/out`, filtro JWT cerrando la seguridad.
