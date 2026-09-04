# ADR 0004 — Refresh token en Postgres (tbl_sesiones), reemplaza la ADR 0003

**Fecha:** 2026-09-04
**Estado:** Aceptada

## Resumen

El refresh token se guarda en `seguridad.tbl_sesiones` (Postgres, en la base de cada empresa), no en Redis como proponía la ADR 0003.

## Contexto

La ADR 0003 proponía Redis para el refresh token, antes de que se revisara el modelo de base de datos completo. Al diseñar el adaptador real, se confirmó con Leidi que `tbl_sesiones` ya estaba modelada — con prácticamente los mismos campos que se habían pensado para Redis (`hash_token_refresco`, `expira_en`, `usuario_id`, `info_dispositivo`, `direccion_ip`), heredada del sistema anterior.

## Opciones evaluadas

| Opción | Consideración |
|---|---|
| Redis (ADR 0003 original) | Requiere una dependencia de infraestructura nueva que el proyecto no tenía; duplicaría un modelo que Leidi ya había armado en Postgres |
| **`tbl_sesiones` en Postgres** (elegida) | Reutiliza el trabajo ya hecho; queda bajo el mismo control de versiones (Liquibase) que el resto del esquema; hereda auditoría automática vía los triggers ya existentes (`plataforma.tbl_auditoria`), sin trabajo extra |

## Decisión

Refresh token guardado en `tbl_sesiones`, con rotación en cada renovación (sin cambios respecto al mecanismo ya descrito en la 0003). El valor se hashea con SHA-256, no con BCrypt — a diferencia de las contraseñas, un refresh token es un valor aleatorio de alta entropía, y BCrypt solo agregaría latencia sin sumar seguridad real.

## Consecuencias

| Capa | Impacto |
|---|---|
| Dominio | Ninguno — sin cambios respecto a la ADR 0003. |
| Aplicación | Puerto `AlmacenDeTokensDeRefresco` (mismo nombre planeado en la 0003, implementación distinta). |
| Infraestructura | Adaptador JPA contra `tbl_sesiones`, usando la misma unidad de persistencia "cliente" ya armada para `usuarios` — no un cliente de Redis aparte. |
| Pendiente | Duración exacta diferenciada web/móvil, y detección de reutilización con revocación en cascada — siguen abiertos, igual que en la 0003. |

## Cómo se podría revertir o evolucionar

Si el volumen de sesiones activas se vuelve un problema de rendimiento para Postgres, se puede migrar a Redis sin tocar el puerto `AlmacenDeTokensDeRefresco` — solo cambia el adaptador de infraestructura, mismo argumento que ya usaba la propia ADR 0003.