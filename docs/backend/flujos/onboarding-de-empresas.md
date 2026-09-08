# Flujo — Onboarding de empresas (autoservicio)

> Cómo un prospecto entra a la plataforma marca blanca, se registra solo, elige
> módulos y personalización, y termina con su plataforma aprovisionada y un
> correo de bienvenida. **Sin pago** (ver [ADR 0005](../modulos/aprovisionamiento/decisiones/2026-09-08-0005-onboarding-publico-sin-pasarela-de-pago.md)).

## 1. Panorama

```
Paso 1  Registro          POST   /api/v1/registro/empresas                         -> 201  { empresaId, identificador, dominio, estado:"borrador" }
Paso 2  Módulos           POST   /api/v1/registro/empresas/{id}/modulos/{cod}/activar    -> 204
                          POST   /api/v1/registro/empresas/{id}/modulos/{cod}/desactivar -> 204
Paso 3-5 Personalización  PUT    /api/v1/registro/empresas/{id}/personalizacion    -> 204   (colores, logo, tipoLogin, tipoPantallaPrincipal)
Paso 6  Finalizar         POST   /api/v1/registro/empresas/{id}/finalizar          -> 202  { empresaId, estado:"pendiente_aprovisionamiento", url }
        │
        ├─ escribe evento en el outbox (plataforma.tbl_eventos_salientes)
        ▼
Paso 7  Pipeline (async)  SondeadorDeEventos (@Scheduled) -> EjecutarAprovisionamientoService
        │  clona la BD, siembra, crea roles, registra conexión/versión, activa módulos,
        │  activa la empresa, y envía el correo de bienvenida (con la contraseña temporal)
        ▼
Paso 8  Primer login      POST   /api/v1/auth/login            -> 200  { ..., debeCambiarContrasena:true }
        (con pwd temporal) POST   /api/v1/auth/cambiar-contrasena -> 204
                          POST   /api/v1/auth/login            -> 200  { ..., debeCambiarContrasena:false }
```

Todos los endpoints de `/api/v1/registro/**` son **públicos** (sin JWT, sin
`X-Admin-Key`) — están en `permitAll` de `SecurityConfig`.

## 2. Estados de la empresa (`plataforma.tbl_empresas.estado`)

```
borrador  ──(Finalizar)──►  pendiente_aprovisionamiento  ──(pipeline)──►  activa
```

`suspendida` / `inactiva` existen en el CHECK pero no los usa este flujo.
Mientras está en `borrador` se puede modificar (módulos, personalización); en
cualquier otro estado, esos endpoints devuelven `409`
(`EmpresaNoModificableException`).

## 3. Paso 1 — Registro

`POST /api/v1/registro/empresas`

```json
{ "nombreEmpresa": "...", "representanteLegal": "...", "correo": "...",
  "telefono": "...", "sitioWeb": "acme.com" }
```

- Deriva el **identificador** (slug) del `sitioWeb` (`Identificador.desde(...)`:
  quita protocolo/www, toma lo anterior al primer `.` o `/`, todo lo no
  `[a-z0-9]` → `_`). El constructor valida formato/largo/reservados.
- El **dominio** = `<identificador>.<sufijo>` (sufijo configurable
  `app.aprovisionamiento.sufijo-dominio`, default `mb`).
- Valida unicidad de identificador y dominio (`409` si ya existen).
- Inserta en `tbl_empresas` con `estado = 'borrador'`, `correo_contacto`,
  `representante_legal`, `telefono`, `sitio_web`. **Sin contraseña** (se genera
  en el Paso 7) y **sin módulos**.
- Respuesta `201` con `{ empresaId, identificador, dominio, estado }`.

`correo` sirve de **contacto** (notificaciones) **y de usuario de login**.

## 4. Paso 2 — Elección de módulos

`POST /api/v1/registro/empresas/{empresaId}/modulos/{codigo}/activar` (y
`/desactivar`). Verifica que la empresa esté en `borrador` y delega en
`modulos-empresa` a través del puente ACL `PuenteModulosEmpresa` →
`ActivarModuloDeEmpresa` / `DesactivarModuloDeEmpresa`. Escribe
`plataforma.tbl_empresa_modulos` (`es_activo`). El catálogo con precio vive en
`plataforma.tbl_modulos` (`precio`, `moneda`).

## 5. Pasos 3-5 — Personalización

`PUT /api/v1/registro/empresas/{empresaId}/personalizacion` — **reemplazo total**
(el wizard manda todo lo acumulado):

```json
{ "colorPrimario": "#1E3A5F", "colorSecundario": "#2563EB",
  "urlLogo": "https://...", "tipoLogin": 2, "tipoPantallaPrincipal": 3 }
```

- Colores: hexadecimal `#RRGGBB` (`ColorHex`). Logo: URL. Ambos opcionales.
- `tipoLogin` / `tipoPantallaPrincipal`: 1..3 (si no vienen, 1).
- `aprovisionamiento` **escribe `plataforma.tbl_empresas_marca` con su propio
  mapeo** (`MarcaDeAprovisionamientoEntity`, `@Entity(name="MarcaDeAprovisionamiento")`),
  no vía `identidad-visual` — ver [ADR 0006](../modulos/aprovisionamiento/decisiones/2026-09-08-0006-wizard-escribe-config-directo.md).
- La variante de UI es **autoridad del backend**: se guarda acá y el frontend
  solo pinta la variante que le dicen en el bootstrap de marca.

## 6. Paso 6 — Finalizar

`POST /api/v1/registro/empresas/{empresaId}/finalizar`

- Exige que la empresa esté en `borrador` y que tenga **≥ 1 módulo activo**
  (`400` si no).
- `Empresa.finalizarRegistro(modulos)` → `estado = pendiente_aprovisionamiento`
  y levanta el evento de dominio `EmpresaRegistrada`.
- El decorador `FinalizarRegistroTransaccional` (`@Transactional`) hace **atómico**
  guardar la empresa + escribir el evento en `plataforma.tbl_eventos_salientes`
  (patrón outbox, [ADR 0003](../modulos/aprovisionamiento/decisiones/2026-09-07-0003-outbox-transaccional.md)).
- Respuesta `202` con `{ empresaId, estado, url: "https://<dominio>" }`.

## 7. Paso 7 — Pipeline de aprovisionamiento (asíncrono)

`SondeadorDeEventos` (`@Scheduled`, cada `app.aprovisionamiento.sondeo-ms` ms)
reclama filas del outbox con `FOR UPDATE SKIP LOCKED`, deserializa el payload a
`EmpresaRegistrada` y llama a `EjecutarAprovisionamiento`.

`EjecutarAprovisionamientoService` corre una **saga con checkpoints**
(`plataforma.tbl_aprovisionamiento_tareas.paso_actual` = último paso completado;
reanuda desde el siguiente; reintentos con backoff; tras N intentos →
`estado = error`). Cada paso es **idempotente**.

| # | Paso (`paso_actual`) | Qué hace | Conexión |
|---|---|---|---|
| 1 | `base_creada` | `CREATE DATABASE db_cliente_<slug> TEMPLATE db_plantilla_maestra` | mantenimiento (owner @ `postgres`) |
| 2 | `semilla_aplicada` | Crea el rol `ADMIN` (idempotente). El usuario admin se crea en el paso 8. | `db_cliente_<slug>` |
| 3 | `roles_creados` | `CREATE ROLE cli_<slug>_app` / `cli_<slug>_lectura` + `GRANT` a los grupos motor | mantenimiento |
| 4 | `conexion_registrada` | `INSERT` en `tbl_empresa_conexiones` (host, puerto, `nombre_bd`, `secreto_ref='dev-local'`) | control |
| 5 | `version_registrada` | `INSERT` en `tbl_empresa_esquema_version` con el último id de `databasechangelog` de la base clonada | control + cliente |
| 6 | `modulos_poblados` | Activa los módulos elegidos vía el puente ACL a `modulos-empresa` (tolerante) | `modulos-empresa` |
| 7 | `empresa_activada` | `Empresa.activar()` → `estado = activa` | control |
| 8 | `bienvenida_enviada` | Genera contraseña temporal, siembra el usuario admin, envía el correo | cliente + SMTP |

**Paso 8 del pipeline (`enviarBienvenida`)** en detalle:
- Idempotencia: si `tbl_empresas.bienvenida_enviada_en` ya está seteada, no hace nada.
- Genera una contraseña temporal de 12 caracteres (alfabeto sin ambiguos).
- En `db_cliente_<slug>.seguridad`: crea/actualiza el usuario admin con
  `correo = correo_contacto`, hash BCrypt de la temporal, `es_contrasena_temporal = true`,
  y le asigna el rol `ADMIN`.
- Lee la config SMTP de `plataforma.tbl_config_correo` (`es_activa`) y envía el
  correo. **Sin SMTP configurado** (ni `app.aprovisionamiento.smtp-password`) →
  **loguea** el contenido del correo (incluida la contraseña) en nivel `WARN` —
  fallback para DEV.
- Marca `tbl_empresas.bienvenida_enviada_en = now()`.

Contenido del correo: bienvenida + `url` (`https://<dominio>`) + usuario (el
correo) + contraseña temporal + aviso de que se pedirá cambiarla.

> **Pendiente:** el envío de correo dentro de `EjecutorDdlPostgres` usa un
> `JavaMailSenderImpl` armado desde `tbl_config_correo` + `app.aprovisionamiento.smtp-password`.
> `application.yml` ya trae `spring.mail.*` + `app.correo.habilitado` / `app.correo.remitente`
> como config estándar — falta reconciliar ambos enfoques.

## 8. Paso 8 — Primer login con contraseña temporal

- `POST /api/v1/auth/login` con la contraseña temporal → `200` con
  `debeCambiarContrasena: true`. El JWT lleva el claim `pwd_temp: true`.
- `JwtAuthFilter`: si el token tiene `pwd_temp` y la ruta **no** es
  `/api/v1/auth/**` → responde `403`. O sea, ese token **solo** sirve para
  cambiar la contraseña.
- `POST /api/v1/auth/cambiar-contrasena` (con el Bearer token) — body
  `{ contrasenaActual, contrasenaNueva }`. Verifica la actual, guarda la nueva y
  limpia `es_contrasena_temporal`. Responde `204`.
- `POST /api/v1/auth/login` con la nueva contraseña → `200` con
  `debeCambiarContrasena: false`; el token ya no tiene `pwd_temp`.

Detalle: [ADR 0007](../modulos/aprovisionamiento/decisiones/2026-09-08-0007-contrasena-temporal-y-primer-login.md).

## 9. Módulos backend involucrados

| Módulo | Rol en el flujo |
|---|---|
| **`aprovisionamiento`** | Dueño del wizard (pasos 1-6), la saga (paso 7) y el disparo del correo. Endpoints `/api/v1/registro/**`. |
| **`modulos-empresa`** | Catálogo de módulos (`tbl_modulos` + `precio`) y activación por empresa. `aprovisionamiento` lo invoca vía puente ACL. |
| **`identidad-visual`** | Lee `tbl_empresas_marca` para servir la marca a una empresa **ya activa**. `aprovisionamiento` escribe esa tabla durante el wizard con su propio mapeo. |
| **`autenticacion`** | Login/JWT + el nuevo endpoint `cambiar-contrasena` y la enforcement de contraseña temporal (`JwtAuthFilter`). |
| **`usuarios`** | Dueño de `seguridad.tbl_usuarios`. `Usuario` gana el flag `esContrasenaTemporal`. `autenticacion` lo toca solo a través de `AdaptadorVerificadorDeUsuarios`. |
| **`empresas`** | Enrutamiento multi-tenant en runtime (no participa del wizard). |

## 10. Datos (changesets de control)

| Changeset | Qué agrega |
|---|---|
| `0012-agregar-datos-de-registro-empresas` | `tbl_empresas`: `representante_legal`, `correo_contacto`, `telefono`, `sitio_web` + índice |
| `0013-ampliar-estados-empresa` | CHECK de `estado` con `borrador` |
| `0014-agregar-precio-modulos` | `tbl_modulos`: `precio`, `moneda` |
| `0015-agregar-variantes-ui-marca` | `tbl_empresas_marca`: `tipo_login`, `tipo_pantalla_principal` (CHECK 1..3) |
| `0016-crear-tabla-config-correo` | `tbl_config_correo` (config SMTP de la plataforma) |
| `0017-preparar-correo-de-bienvenida` | CHECK de `paso_actual` con `bienvenida_enviada` + `tbl_empresas.bienvenida_enviada_en` |
| `cliente/seguridad/0014-agregar-contrasena-temporal` | `seguridad.tbl_usuarios.es_contrasena_temporal` |

## 11. Configuración (`application.yml`)

```yaml
app:
  aprovisionamiento:
    sondeo-ms:        ${APROV_SONDEO_MS:5000}
    plantilla:        ${APROV_PLANTILLA:db_plantilla_maestra}
    sufijo-dominio:   ${APROV_SUFIJO_DOMINIO:mb}
    cliente-host:     ${APROV_CLIENTE_HOST:localhost}
    cliente-puerto:   ${APROV_CLIENTE_PUERTO:5432}
    mantenimiento: { url, username, password }   # owner @ base 'postgres', para DDL
    smtp-password:   ${APROV_SMTP_PASSWORD:}      # secreto SMTP; vacío -> correo se loguea, no se envía
```

## 12. Pendientes / DEV-only

- **Contraseña de roles `cli_<slug>_*`** = el nombre del rol (convención DEV).
  El enrutador multi-tenant sigue usando el rol compartido `guajiranet_app`
  ([ADR 0004](../modulos/aprovisionamiento/decisiones/2026-09-07-0004-credenciales-por-tenant.md)).
- **`secreto_ref = 'dev-local'`** en `tbl_empresa_conexiones` — falta vault real (QA/PROD).
- **Envío de correo**: reconciliar `EjecutorDdlPostgres` con `spring.mail.*` /
  `app.correo.*` de `application.yml`.
- **Bases de cliente pre-existentes** sin la columna `es_contrasena_temporal`
  (`db_cliente_*` creadas antes de aplicar `cliente/seguridad/0014` a la plantilla)
  — recrear o migrar.
- **`db.changelog-master.yaml` de control**: el changeset `0016` tiene `id`
  `0018-crear-tabla-config-correo` (no coincide con el nombre de archivo) —
  quedó así por haberse renombrado después de correrse; alinear en un entorno limpio.
- **Barrido de eventos colgados** del outbox (fila en `procesando` con worker caído).
