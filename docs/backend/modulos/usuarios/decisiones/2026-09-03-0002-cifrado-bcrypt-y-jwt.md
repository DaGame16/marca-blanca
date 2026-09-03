# ADR 0002 — Cifrado de contraseñas con BCrypt y sesión con JWT

**Fecha:** 2026-09-03
**Estado:** Aceptada

## Resumen

Contraseñas cifradas con BCrypt. Sesión sin estado (stateless) con JWT firmado con clave simétrica.

## Contexto

La plataforma expone una API REST consumida por un frontend Angular separado (SPA), sin sesiones de servidor tradicionales. Se necesita: (1) cifrar contraseñas de forma irreversible, y (2) un mecanismo de sesión que no dependa de guardar estado en el servidor.

## Opciones evaluadas — cifrado de contraseñas

| Opción | Descripción | Consideración |
|---|---|---|
| **BCrypt** (elegida) | Estándar de facto en el ecosistema Spring Security, soporte nativo vía `BCryptPasswordEncoder` | No requiere librerías adicionales, ampliamente probado en producción |
| Argon2 | Algoritmo más moderno (ganador de la Password Hashing Competition) | Spring Security lo soporta (`Argon2PasswordEncoder`), pero no se evaluó a fondo para este proyecto — queda como opción futura si el equipo de seguridad lo pide explícitamente |

## Opciones evaluadas — mecanismo de sesión

| Opción | Descripción | Consideración |
|---|---|---|
| **JWT** (elegida) | Token autocontenido y firmado, sin estado en el servidor | Encaja naturalmente con API REST + SPA separada |
| Sesión de servidor (cookie + estado en memoria o Redis) | Requiere almacenamiento compartido si hay más de una instancia del backend corriendo | Se descartó por simplicidad en esta etapa del proyecto |

## Decisión

BCrypt (`BCryptPasswordEncoder`) + JWT (librería JJWT 0.12.6, firma HMAC).

## Ejemplo

Contenido del token (claims):
```json
{
  "sub": "3f2504e0-4f89-11d3-9a0c-0305e82c3301",
  "correo": "ana@empresa.com",
  "iat": 1735689600,
  "exp": 1735693200
}
```

Configuración (`application.yml`):
```yaml
app:
  jwt:
    secret: ${JWT_SECRET:solo-para-desarrollo-local-cambiar-siempre}
    expiracion-minutos: 60
```

## Consecuencias

| Capa | Impacto |
|---|---|
| Dominio | Ninguno — `CifradorDeContrasenas` es una interfaz; el dominio no conoce el algoritmo real. |
| Aplicación | Ninguno — solo conoce los puertos `GeneradorDeToken` / `VerificadorDeToken`. |
| Infraestructura | `BCryptCifradorDeContrasenas`, `JwtGeneradorDeToken`, `JwtVerificadorDeToken`, `JwtAuthFilter`. |
| Seguridad | El secreto real **siempre** debe venir de la variable de entorno `JWT_SECRET` en DEV compartido, QA y PROD — el valor en el repo es únicamente un placeholder de desarrollo local. |
| Pendiente | No hay invalidación de sesión del lado del servidor (logout real / revocación de tokens antes de su expiración natural). |

## Cómo se podría revertir o evolucionar

Si se necesita revocar tokens antes de que expiren (por ejemplo, al desactivar una cuenta), se puede agregar una lista de revocación consultada por `JwtVerificadorDeToken` — sin cambiar el contrato de los puertos ni tocar dominio o aplicación.
