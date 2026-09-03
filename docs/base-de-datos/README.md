# Base de datos

> Resumen vivo. Describe **qué hay y cómo funciona hoy**. Se actualiza *in place*
> cada vez que cambia el esquema o el proceso. El **por qué** de cada decisión
> vive en [`decisiones/`](decisiones/).
>
> Área a cargo de: **Leidi**.

Cubre la capa de datos del backend: motor, cómo se accede en cada ambiente,
modelo multi-tenant, estructura de los changelogs de Liquibase, roles, auditoría
y el workspace de migración desde el sistema anterior.

La fuente de verdad del **diseño** es el documento *"Portal GuajiraNet —
Arquitectura de Base de Datos"* (v5). Este README describe cómo está
**implementado** en el repo.

## 1. Motor y stack

- **PostgreSQL 17**. En DEV corre en Docker (`backend/bootstrap/compose.yaml`);
  `spring-boot-docker-compose` lo levanta y lo apaga con la app.
- **Liquibase** (YAML) para toda la estructura. El esquema nunca se toca a mano.
- Dependencias (`backend/bootstrap/pom.xml`): `spring-boot-starter-data-jpa`,
  `liquibase-core`, `spring-boot-liquibase`, `postgresql`, y el
  `liquibase-maven-plugin` para aplicar el changelog de cliente a demanda.

## 2. Cómo se accede a la base

### DEV — cada quien la suya, no hay base compartida

En desarrollo **no existe una base central a la que el equipo se conecte**. Cada
persona corre su **propia copia local** en Docker. Nadie se conecta a la base de
otra persona. Es a propósito (documento §8.1: "DEV = Docker, 1 instancia") y es lo
que dan Docker + Liquibase: todos levantan un Postgres idéntico con un comando, y
si algo se rompe se recrea desde cero.

**Puesta en marcha de un compañero, desde cero:**

1. Instalar **Docker Desktop** (con backend WSL2 en Windows). Una sola vez.
2. `git pull` del repo.
3. Levantar la app:
   ```bash
   mvn -f backend/pom.xml -pl bootstrap -am install -DskipTests
   mvn -f backend/pom.xml -pl bootstrap spring-boot:run
   ```
   `spring-boot-docker-compose` levanta **su** contenedor `marca-blanca-postgres`
   y Liquibase aplica el changelog de **control**. `Ctrl+C` al terminar.
4. Crear la plantilla y aplicarle el changelog de **cliente** completo:
   ```bash
   docker exec -it marca-blanca-postgres psql -U guajiranet_owner -d db_portal_guajiranet_control \
     -c "CREATE DATABASE db_plantilla_maestra"
   mvn -f backend/pom.xml -pl bootstrap liquibase:update
   ```
5. (Opcional) Una base de cliente con datos para trabajar:
   ```bash
   docker exec -it marca-blanca-postgres psql -U guajiranet_owner -d db_portal_guajiranet_control \
     -c "CREATE DATABASE db_cliente_demo TEMPLATE db_plantilla_maestra"
   ```

**Credenciales locales** (`guajiranet_owner` / `guajiranet_app` /
`guajiranet_lectura`, contraseña = el mismo nombre): están en `compose.yaml` y
`application.yml`, y son **solo para local**. Nunca se usan en otro ambiente.

**Datos entre compañeros:** si dos personas necesitan el mismo set de datos, se
comparte un dump o un script de *seed* — nunca la conexión a una base ajena.

### QA / PROD — sí hay una instancia por ambiente

La base compartida vive en **AWS RDS PostgreSQL** (Multi-AZ en PROD), detrás de
RDS Proxy, con las credenciales en AWS Secrets Manager. Ahí se corren **los mismos
changelogs** de este repo. Ese montaje es de **infraestructura (Neider)** —
ver `docs/infraestructura/`.

## 3. Modelo multi-tenant: una base por cliente

No hay una sola base con columna `empresa_id`. Cada empresa cliente tiene su
**propia base PostgreSQL completa**, más una base transversal que coordina todo.

| Base | Nombre | Contenido |
|---|---|---|
| **Control** (transversal) | `db_portal_guajiranet_control` | Catálogo de empresas, marca blanca, el **directorio de enrutamiento** (a qué base física va cada empresa), catálogo de módulos y módulos activos por empresa. |
| **Plantilla** | `db_plantilla_maestra` | Estructura completa de una base de cliente, sin datos. Liquibase la mantiene al día. |
| **Cliente** | `db_cliente_<slug>` | Una por empresa. Nace clonada de la plantilla con `CREATE DATABASE ... TEMPLATE`. Ahí viven los datos de negocio de esa empresa. |

El backend nunca "adivina" a qué base ir: resuelve la empresa desde la petición,
consulta `plataforma.tbl_empresa_conexiones` en la base de control, y se conecta a
la `db_cliente_*` correspondiente. *(Ese ruteo es código Java y todavía no está
hecho — ver [§10](#10-estado-y-pendientes).)*

## 4. La base de control (`db_portal_guajiranet_control`)

Un solo schema: **`plataforma`**.

| Tabla | Para qué |
|---|---|
| `tbl_empresas` | Catálogo maestro de clientes + estado (`pendiente_aprovisionamiento` → `activa`). `identificador` = slug que nombra la `db_cliente_<slug>`. |
| `tbl_empresas_marca` | Logo, colores, dominio propio (marca blanca). 1:1 con empresa. |
| `tbl_empresa_conexiones` | **Directorio de enrutamiento**: host, puerto, `nombre_bd`, `secreto_ref` (referencia al vault, nunca la contraseña en claro). |
| `tbl_empresa_esquema_version` | Audita que ninguna base de cliente quede desincronizada del changelog. |
| `tbl_modulos` | Catálogo de módulos del producto: `usuarios`, `omnicanal`, `3cx`. |
| `tbl_empresa_modulos` | Qué módulos tiene activo cada empresa. Es un checklist — el gating real lo hace el backend, la BD no bloquea nada. |

El changelog de control lo aplica **la app al arrancar** (`spring.liquibase`
apunta a `db/changelog/control/db.changelog-master.yaml`).

## 5. La base de cliente (`db_cliente_<slug>` / `db_plantilla_maestra`)

Toda base de cliente tiene la **misma** estructura, tenga o no activo cada módulo.
**76 tablas** en 6 schemas:

| Schema | Contenido | Tablas |
|---|---|---|
| `seguridad` | Usuarios, perfiles, roles, permisos, sesiones, dispositivos confiables, tokens FCM, consentimientos de datos | 11 |
| `omnicanal` | Chat interno del equipo + Liwa (conversaciones con clientes, turnos, casos, análisis IA, seguimientos) | 8 |
| `producto` | Núcleo operativo del ERP: catálogos, clientes finales, tareas, inventario/bodega, compras, ventas | 53 |
| `cliente` | Configuración/personalización del tenant (hoy: avisos globales) | 1 |
| `historico` | Logs de auditoría **heredados** del sistema viejo (solo lectura) | 2 |
| `plataforma` | Tabla y funciones de **auditoría** de esta base (ver §8) | 1 |

> No se usa el schema `public` — decisión de seguridad del documento (§4).

### Nomenclatura (documento §5)

- Español, `snake_case`, identificadores **en minúscula** (ver
  [ADR 0002](decisiones/2026-09-03-0002-identificadores-en-minuscula.md)).
- Prefijos: `tbl_` tablas, `pk_`/`uq_`/`fk_`/`ck_` constraints, `idx_` índices,
  `fn_` funciones, `trg_` triggers.
- Tablas en plural, columnas en singular. FK `<tabla_singular>_id`. Fechas con
  sufijo `_en`. Booleanos con prefijo `es_` / `tiene_`.
- **Cada columna FK lleva su `idx_` explícito** en el mismo changeset (Postgres no
  indexa FKs solo).
- Toda tabla tiene **dos identificadores**: `id` `BIGINT` autoincremental
  (eficiencia interna) + `uuid` (exposición externa, integraciones).
- Relaciones N:N siempre con tabla pivote.
- Tipos: `enum` de MySQL → `VARCHAR` + `CHECK`; JSON → `JSONB`; `decimal` →
  `NUMERIC`; `double` → `DOUBLE PRECISION`; IPs → `INET`. Detalle en
  [ADR 0005](decisiones/2026-09-03-0005-mapeo-de-tipos-mysql-a-postgres.md).

## 6. Roles del motor (documento §6.1)

Tres roles a nivel de cluster, creados idempotentemente por Liquibase:

| Rol | Puede | No puede |
|---|---|---|
| `guajiranet_owner` | DDL, correr migraciones | — |
| `guajiranet_app` | `INSERT`/`UPDATE`/`DELETE` (DML) | crear/alterar estructura |
| `guajiranet_lectura` | `SELECT` | escribir |

Cada módulo termina con un changeset `otorgar-privilegios` que hace `GRANT` sobre
su schema y `ALTER DEFAULT PRIVILEGES` (para cubrir tablas futuras). En DEV la app
se conecta como `guajiranet_owner`; `guajiranet_app` se usará en el DataSource de
enrutamiento por tenant y en QA/PROD.

## 7. Estructura de los changelogs

```
backend/bootstrap/src/main/resources/db/changelog/
├── control/                    → db_portal_guajiranet_control (corre al arrancar la app)
│   ├── db.changelog-master.yaml
│   └── changes/                0000..0008
└── cliente/                    → db_plantilla_maestra (mvn -pl bootstrap liquibase:update)
    ├── db.changelog-master.yaml   incluye los módulos EN ORDEN de dependencia
    ├── base/                    schemas
    ├── seguridad/              0001..0013
    ├── omnicanal/              0001..0009
    ├── producto/              sub-dividido en:
    │   ├── grants/ catalogos/ clientes/ tareas/ inventario/ compras/ ventas/
    │   └── producto.changelog.yaml
    ├── cliente/               0001
    ├── historico/             0001..0002
    └── auditoria/             0001..0005
```

- Cada módulo tiene su `<modulo>.changelog.yaml` y numeración propia que reinicia
  en `0001`.
- El `id` de cada changeset es `<modulo>-NNNN-descripcion` (o
  `producto-<area>-NNNN-descripcion`) y **coincide con el nombre del archivo**.
- Orden en `cliente/db.changelog-master.yaml`: `base → seguridad → omnicanal →
  producto → cliente → historico → auditoria` (hay FKs cross-schema a
  `seguridad.tbl_usuarios`; la auditoría cuelga triggers de tablas ya creadas).
- Las rutas de los dos `db.changelog-master.yaml` **no cambian nunca** — agregar
  un módulo no toca `application.yml` ni el `pom.xml`.

Los comandos para aplicarlos están en [§2](#dev--cada-quien-la-suya-no-hay-base-compartida).
Por qué esta estructura: [ADR 0001](decisiones/2026-09-03-0001-estructura-de-los-changelogs.md).

## 8. Auditoría (documento §7)

Cada base de cliente audita sus escrituras con **triggers de fila**. Módulo
`cliente/auditoria/`.

- **`plataforma.tbl_auditoria`** — una fila por cada INSERT/UPDATE/DELETE en tabla
  crítica: `esquema`, `tabla`, `operacion`, `registro_id`/`registro_uuid`,
  `datos_anteriores`/`datos_nuevos` (JSONB), `usuario_bd`, `usuario_app_id`,
  `txid`, `ejecutado_en`.
- **`fn_auditar()`** — función de trigger (`SECURITY DEFINER`).
- **`fn_activar_auditoria(esquema, tabla)`** — helper para colgar el trigger.
  **Sumar una tabla crítica = un changeset nuevo que la llame.** La lista actual
  (20 tablas) está en `auditoria/0005`.
- **Inmutabilidad**: triggers `BEFORE UPDATE/DELETE/TRUNCATE` que lanzan
  excepción, más `REVOKE` de escritura a `app`/`lectura`/`PUBLIC`. **Ni
  `guajiranet_owner` puede borrar el rastro.** La única vía es
  `fn_purgar_auditoria(interval)` (retención, arranca en 2 años — Ley 1581).
- **Silenciado**: en una transacción con `SET LOCAL plataforma.auditoria_activa =
  'off'` los triggers no escriben. Lo usan los scripts de migración para no
  ensuciar el rastro con la carga inicial.
- **Usuario de app**: el backend debe hacer `SET plataforma.usuario_app_id =
  '<id>'` al inicio de cada request. *(Pendiente — es código Java.)*

Por qué así: [ADR 0004](decisiones/2026-09-03-0004-auditoria-por-triggers-inmutable.md).

## 9. Migración desde el sistema anterior

El sistema viejo es MariaDB 10.4 (`erp_saas_test`, hecho con Prisma, PKs de texto
tipo cuid, multi-tenant por columna `empresaId`). Migración **big-bang** con una
zona de staging.

```
dump MySQL  →  MariaDB (Docker)  →  pgloader  →  schema "staging" en la base de cliente
                                                       │
                                    SQL de transformación por módulo (migracion/transform/)
                                                       ▼
                                              schemas nuevos (seguridad, producto, ...)
```

- Todo en `migracion/` (raíz del repo). El dump **no** se commitea (gitignored,
  puede tener datos personales — Ley 1581). Ver `migracion/README.md`.
- Los `id` cuid del sistema viejo se usan solo como columna temporal `id_origen`
  durante cada transform y se eliminan al validar (`*-limpieza.sql`). **No quedan
  en las tablas vivas.** Detalle: [ADR 0003](decisiones/2026-09-03-0003-manejo-de-ids-en-la-migracion.md).
- La transformación se hizo por olas (usuarios → omnicanal → ERP por sub-área →
  config del tenant → auditoría heredada). Cada `migracion/transform/NN-*.sql`
  tiene su bloque de validación de conteos (origen = destino).
- Resultado sobre la empresa del dump (GuajiraNet, ~37 000 filas): todos los
  conteos origen = destino, 0 huérfanos en todas las FK.

## 10. Estado y pendientes

**Hecho:**

- Motor + Liquibase + roles.
- Changelog de control aplicado; changelog de cliente completo (82 changesets
  desde cero sin error).
- Migración de datos validada.
- Auditoría por triggers funcionando (probado: un cambio real se audita; el
  borrado del rastro se bloquea incluso para `owner`).

**Pendiente (ya no es base de datos — es código o infra):**

| Tarea | Tipo |
|---|---|
| `AbstractRoutingDataSource`: rutear cada request al `db_cliente_*` correcto leyendo `tbl_empresa_conexiones` | Java/Spring |
| `SET plataforma.usuario_app_id` por request | Java/Spring |
| Testcontainers en CI (un Postgres efímero para los `@SpringBootTest`) | Config |
| Infra QA/PROD: RDS Multi-AZ, RDS Proxy, Secrets Manager, correr los changelogs allá | Infra (Neider) |
| Definir con negocio/legal la lista definitiva de tablas críticas de auditoría y el plazo de retención | Decisión |

## Decisiones de diseño relacionadas

- [`decisiones/2026-09-03-0001-estructura-de-los-changelogs.md`](decisiones/2026-09-03-0001-estructura-de-los-changelogs.md)
- [`decisiones/2026-09-03-0002-identificadores-en-minuscula.md`](decisiones/2026-09-03-0002-identificadores-en-minuscula.md)
- [`decisiones/2026-09-03-0003-manejo-de-ids-en-la-migracion.md`](decisiones/2026-09-03-0003-manejo-de-ids-en-la-migracion.md)
- [`decisiones/2026-09-03-0004-auditoria-por-triggers-inmutable.md`](decisiones/2026-09-03-0004-auditoria-por-triggers-inmutable.md)
- [`decisiones/2026-09-03-0005-mapeo-de-tipos-mysql-a-postgres.md`](decisiones/2026-09-03-0005-mapeo-de-tipos-mysql-a-postgres.md)

---

## Historial de cambios

- **2026-09-03** — Leidi — Documentación inicial: integración PostgreSQL +
  Liquibase, base de control, esquema completo de una base de cliente (6 schemas,
  76 tablas), roles del motor, auditoría por triggers, y el workspace + los
  scripts de migración desde el sistema anterior.
