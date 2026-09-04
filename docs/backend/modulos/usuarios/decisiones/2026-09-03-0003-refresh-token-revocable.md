# ADR 0003 — Renovación de sesión con refresh token revocable (Redis)

**Fecha:** 2026-09-03
**Estado:** Aceptada

## Resumen

Se agrega un refresh token revocable, almacenado en Redis, junto al access token JWT existente (ADR 0002) — para poder cortar el acceso de un usuario o empresa antes de que el token expire por sí solo.

## Contexto

La ADR 0002 dejó señalado como pendiente que no había invalidación de sesión del lado del servidor. Con el modelo de marketplace ya definido (`empresa_suscripciones`, `empresa_modulos`), ese pendiente pasa a ser un problema real: si una empresa cancela su plan o se bloquea un usuario, con JWT puro esa persona sigue entrando con su token vigente hasta que expire naturalmente — no hay forma de cortarlo antes.

## Opciones evaluadas

| Opción | Descripción | Consideración |
|---|---|---|
| Cookies de sesión de servidor | Sesión con estado en el servidor, cookie en el cliente | Descartada — complica el cliente Flutter (no es su patrón natural) y exige sesión compartida entre las réplicas de PROD |
| JWT puro, sin cambios (statu quo) | Seguir solo con el access token de la ADR 0002 | Descartada — no permite revocar acceso antes de la expiración natural del token |
| **Access token corto + refresh token revocable en Redis** (elegida) | El access token (JWT) sigue igual; se agrega un refresh token opaco guardado en Redis, con rotación en cada uso | Mantiene la mayoría de requests stateless (rápidos, sin tocar Redis) y agrega un punto real de revocación para el resto |

## Decisión

Access token JWT sin cambios respecto a la ADR 0002 (vida corta). Refresh token nuevo, guardado en Redis por usuario, con rotación: cada vez que se usa para pedir un access token nuevo, se invalida y se entrega uno nuevo — si un token viejo ya invalidado se vuelve a usar, es señal de robo.

## Consecuencias

| Capa | Impacto |
|---|---|
| Dominio | Nuevo concepto `TokenDeRefresco`, con su propio ciclo de vida. |
| Aplicación | Nuevo puerto de salida (guardar / validar / revocar contra Redis); nuevo caso de uso `RenovarToken`. El login pasa a devolver el par access + refresh, en vez de un solo token. |
| Infraestructura | Implementación del puerto contra Redis (ya en uso en el ERP actual). |
| Seguridad | Revocación real posible por empresa (cancelación de plan) o por usuario (bloqueo), sin esperar a que expire el token. |
| Pendiente | Duración exacta del refresh token (web vs. Flutter), y cómo Angular maneja la renovación automática — a definir con Carlos y Leidi. |

## Cómo se podría revertir o evolucionar

Si Redis se vuelve un cuello de botella para este propósito, el almacén de refresh tokens se puede mover a otro motor sin tocar dominio ni aplicación — el puerto ya aísla esa decisión, igual que pasó con `CifradorDeContrasenas` en la ADR 0002.