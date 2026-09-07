# Módulo Aprovisionamiento

> Resumen vivo. Se actualiza *in place* cada vez que el módulo cambia.

## 1. Qué es y qué NO es

Cubre el **ciclo de alta de una empresa cliente**: registrarla en la base de
control y **aprovisionar su base de datos física** (`db_cliente_<slug>`),
sembrarla, registrar su conexión y dejarla `activa`.

**No le corresponde**:
- Resolver a qué base va cada petición en runtime — eso es [`empresas`](../empresas/README.md).
- Ser dueño del catálogo de módulos — eso es [`modulos-empresa`](../modulos-empresa/README.md); este módulo solo lo **invoca** vía un puente ACL.
- La marca visual — eso es [`identidad-visual`](../identidad-visual/README.md).

## 2. Las dos capas

| Capa | Rol motor | Responsabilidad |
|---|---|---|
| **1 — Alta** | `guajiranet_app` (sin DDL) | `POST /api/v1/admin/empresas` → inserta en `plataforma.tbl_empresas` (`estado = pendiente_aprovisionamiento`) y escribe el outbox, en **una** transacción. Responde `202`. |
| **2 — Pipeline** | `guajiranet_owner` (DDL) | `@Scheduled` que sondea el outbox y ejecuta la saga hasta `estado = activa`. |

Detalle y motivación: [ADR 0002](decisiones/2026-09-07-0002-dos-capas-alta-y-pipeline.md).

## 3. La saga (Capa 2) — pasos

`tbl_aprovisionamiento_tareas.paso_actual` guarda el último paso **completado**;
un reintento reanuda desde el siguiente. Cada paso es idempotente.

| Paso | Qué hace | Dónde |
|---|---|---|
| `base_creada` | `CREATE DATABASE db_cliente_<slug> TEMPLATE db_plantilla_maestra` | conexión de mantenimiento (owner @ `postgres`) |
| `semilla_aplicada` | Rol `ADMIN` + usuario maestro (`hash_contrasena_maestra`, correo `admin@<dominio\|slug.local>`) + asignación | conexión a `db_cliente_<slug>` |
| `roles_creados` | `CREATE ROLE cli_<slug>_app / _lectura` + `GRANT` a los grupos | conexión de mantenimiento |
| `conexion_registrada` | `INSERT` en `plataforma.tbl_empresa_conexiones` (`secreto_ref = 'dev-local'`) | base de control |
| `version_registrada` | `INSERT` en `plataforma.tbl_empresa_esquema_version` con el último id de `databasechangelog` de la base clonada | control + base cliente |
| `modulos_poblados` | Activa los módulos del plan vía el puente ACL a `modulos-empresa` (tolerante: un código inválido no bloquea) | `modulos-empresa` |
| `empresa_activada` | `Empresa.activar()` → `estado = activa` | base de control |

## 4. Estructura de paquetes

```
aprovisionamiento/
├── aprovisionamiento-domain/.../domain/
│   Empresa, Identificador, EstadoEmpresa, PasoDeAprovisionamiento,
│   EstadoTarea, TareaDeAprovisionamiento, EmpresaRegistrada,
│   EventoDeDominio, HashContrasenaMaestra, *Exception
├── aprovisionamiento-application/.../application/
│   RegistrarEmpresaService (Capa 1), EjecutarAprovisionamientoService (Capa 2),
│   ComandoRegistrarEmpresa
│   └── port/{in,out}/
└── aprovisionamiento-infrastructure/.../infrastructure/
    ├── ConfiguracionAprovisionamiento, RegistrarEmpresaTransaccional
    ├── web/            → AltaEmpresaController + DTOs + ManejadorErrores
    ├── pipeline/       → SondeadorDeEventos, EjecutorDdlPostgres,
    │                     PuenteModulosEmpresa (ACL), ConfiguracionPipeline
    └── persistencia/   → entidades JPA + adaptadores (base de control)
```

## 5. Contrato REST

`POST /api/v1/admin/empresas` — cuerpo: `identificador`, `nombreLegal`,
`nombreComercial?`, `dominio?`, `contrasenaMaestra`, `modulosSolicitados[]`.
Respuesta `202` con `{ empresaId, estado }` y header `Location`.

Protección: header `X-Admin-Key` (interceptor de `/api/v1/admin/**`, propiedad
`app.admin.clave`). Spring Security tiene `/api/v1/admin/**` en `permitAll`.

## 6. Configuración (`application.yml`, bloque `app.aprovisionamiento`)

| Propiedad | Default DEV | Variable de entorno |
|---|---|---|
| `sondeo-ms` | 5000 | `APROV_SONDEO_MS` |
| `plantilla` | `db_plantilla_maestra` | `APROV_PLANTILLA` |
| `cliente-host` / `cliente-puerto` | `localhost` / `5432` | `APROV_CLIENTE_HOST` / `_PUERTO` |
| `mantenimiento.url` | `jdbc:postgresql://localhost:5432/postgres` | `APROV_MANT_URL` |
| `mantenimiento.username` / `password` | `guajiranet_owner` / `guajiranet_owner` | `APROV_MANT_USER` / `_PASSWORD` |

## 7. Decisiones de diseño

- [0001](decisiones/2026-09-07-0001-modulo-separado-para-aprovisionamiento.md) — módulo separado.
- [0002](decisiones/2026-09-07-0002-dos-capas-alta-y-pipeline.md) — dos capas, rol app vs owner.
- [0003](decisiones/2026-09-07-0003-outbox-transaccional.md) — outbox transaccional.
- [0004](decisiones/2026-09-07-0004-credenciales-por-tenant.md) — credenciales por tenant y `secreto_ref`.

## 8. Pendiente

- **Barrido de eventos colgados**: una fila del outbox en `procesando` con
  `bloqueado_en` viejo (proceso muerto a mitad) hoy no se recupera sola.
- **Vault real** en QA/PROD para `cli_<slug>_*` y `secreto_ref`, y que el
  enrutador (`EnrutadorDataSourcePorEmpresa`) use el rol por-tenant en vez del
  `guajiranet_app` compartido (ADR 0004).
- **Capa 2 como deployable aparte** con su propio IAM/secreto (ADR 0002).
- **Semilla de marca**: hoy la semilla crea el usuario admin; falta la fila de
  `identidad-visual` (logo, colores) a partir de los datos del alta.

---

## Historial de cambios

- **2026-09-07** — Leidi — Creación del módulo: Capa 1 (alta + outbox), Capa 2
  (sondeador + pipeline de 7 pasos), puente ACL a `modulos-empresa`, semilla del
  usuario maestro, roles por-tenant en DEV. ADRs 0001–0004.
