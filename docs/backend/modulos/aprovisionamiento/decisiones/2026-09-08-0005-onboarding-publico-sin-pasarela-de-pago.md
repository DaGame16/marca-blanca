# ADR 0005 — Onboarding público, sin pasarela de pago

**Fecha:** 2026-09-08
**Estado:** Aceptada
**Reemplaza a:** parcialmente al ADR 0002 (el "alta" deja de ser admin-only)

## Resumen

El alta de empresa pasa de ser un endpoint de administrador
(`POST /api/v1/admin/empresas`, protegido con `X-Admin-Key`) a un **wizard
público de autoservicio** bajo `/api/v1/registro/**`. No hay cobro: al finalizar
el wizard se aprovisiona directo.

## Contexto

El diseño inicial contemplaba un paso de pago (resumen de costos por módulo,
botón pagar, webhook de la pasarela que disparaba el aprovisionamiento). Se
descartó: "ya no va a haber gestión de pagos, ya solo registro directo".

## Decisión

- Nuevo estado `borrador` en `tbl_empresas`: el wizard crea la empresa en
  `borrador` y la va completando (módulos, personalización) mientras siga en ese
  estado. Cualquier modificación sobre una empresa que ya no está en `borrador`
  devuelve `409`.
- El wizard son 6 pasos (registro → módulos → colores/logo → tipo de login →
  tipo de pantalla → finalizar). Ver el flujo completo en
  [`docs/backend/flujos/onboarding-de-empresas.md`](../../../flujos/onboarding-de-empresas.md).
- El disparador del pipeline pasa de "webhook de pago confirmado" a **el botón
  Finalizar** (`POST .../finalizar`), que mueve la empresa a
  `pendiente_aprovisionamiento` y escribe el evento de outbox.
- Se eliminó del diseño la tabla `tbl_ordenes_pago` y la config de pasarela.
  `tbl_modulos.precio` se mantiene: sirve para **mostrar** el costo en el wizard
  aunque no se cobre.
- Todo `/api/v1/registro/**` está en `permitAll` de `SecurityConfig`.

## Consecuencias

- El wizard es multi-request y sin sesión: hace falta persistir el `borrador` tras
  el paso 1 para poder colgarle módulos/personalización con su `empresa_id`.
- No hay barrera anti-abuso (rate limit / captcha) en el registro público —
  pendiente para QA/PROD.
- Si en el futuro vuelve el cobro, se reintroduce entre "finalizar" y el
  pipeline, con `borrador → pendiente_pago → pendiente_aprovisionamiento`.
