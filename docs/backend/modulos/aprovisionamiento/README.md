# Módulo Aprovisionamiento

> Resumen vivo. Se actualiza *in place* cada vez que el módulo cambia.

## 1. Qué es y qué NO es

Este módulo cubre el **ciclo de alta de una empresa cliente**: registrarla en la
base de control y **aprovisionar su base de datos física** (`db_cliente_<slug>`),
sembrarla, registrar su conexión y dejarla `activa`.

**No le corresponde**:
- Resolver a qué base va cada petición en runtime — eso es [`empresas`](../empresas/README.md).
- Activar/desactivar módulos de negocio — eso es [`modulos-empresa`](../modulos-empresa/README.md).
- La marca visual — eso es [`identidad-visual`](../identidad-visual/README.md).

## 2. Las dos capas (documento de BD §2.3)

| Capa | Rol motor | Responsabilidad |
|---|---|---|
| **1 — Alta** | `guajiranet_app` (sin DDL) | Registra la empresa en `plataforma.tbl_empresas` (`estado = pendiente_aprovisionamiento`) y escribe un evento en el outbox transaccional. Nada más. |
| **2 — Pipeline** | `guajiranet_owner` (DDL) | Consume el evento y ejecuta el pipeline: crear la base (`CREATE DATABASE ... TEMPLATE db_plantilla_maestra`), sembrar datos de la empresa, registrar `tbl_empresa_conexiones` y `tbl_empresa_esquema_version`, poblar `tbl_empresa_modulos`, pasar `estado` a `activa`. |

Por qué se separan: una vulnerabilidad en el backend público no debe traducirse
en poder crear/borrar bases. Hoy ambas capas viven en el mismo proceso; la
separación en un deployable aparte para la Capa 2 es un paso de infraestructura
posterior (ver `decisiones/`).

## 3. Estructura de paquetes

```
aprovisionamiento/
├── aprovisionamiento-domain/.../aprovisionamiento/domain/
├── aprovisionamiento-application/.../aprovisionamiento/application/
│   └── port/{in,out}/
└── aprovisionamiento-infrastructure/.../aprovisionamiento/infrastructure/
    ├── web/            → Capa 1: controlador REST + DTOs + manejador de errores
    ├── pipeline/       → Capa 2: sondeador del outbox + ejecutor DDL + saga
    └── persistencia/   → JPA contra la base de control (tbl_empresas, outbox, tareas)
```

## 4. Contrato REST

`POST /api/v1/admin/empresas` — registra una empresa (Capa 1). Responde `202
Accepted` con el estado `pendiente_aprovisionamiento`. *(Pendiente de implementar.)*

## 5. Decisiones de diseño relacionadas

Ver `decisiones/`.

## 6. Estado

En construcción — módulo recién creado, sin código funcional todavía.

---

## Historial de cambios

- **2026-09-07** — Leidi — Creación del módulo (esqueleto Maven + carpetas).
