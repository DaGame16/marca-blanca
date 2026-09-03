# ADR 0005 — Mapeo de tipos MySQL → PostgreSQL

**Fecha:** 2026-09-03
**Estado:** Aceptada

## Resumen

Al pasar del sistema viejo (MariaDB/Prisma) al modelo nuevo, los tipos no se
copian tal cual: los `enum` de MySQL se vuelven `VARCHAR` + `CHECK`, el JSON se
vuelve `JSONB`, y se usan los tipos nativos de PostgreSQL donde aporta (`NUMERIC`,
`DOUBLE PRECISION`, `INET`, `DATE`).

## Contexto

El modelo destino es un rediseño, no un *lift-and-shift*. pgloader podría copiar
tipo por tipo, pero eso arrastra decisiones de MySQL que en PostgreSQL se hacen
mejor de otra forma.

## Decisión

| Origen (MySQL / Prisma) | Destino (PostgreSQL) | Motivo |
|---|---|---|
| `enum('A','B',...)` | `VARCHAR(n)` + constraint `ck_<tabla>_<col>` | Cambiar los valores permitidos no requiere `ALTER TYPE`; consistente con `estado` de `tbl_empresas` |
| `longtext` con `CHECK (json_valid(...))` | `JSONB` | Tipo nativo, indexable, validado por el motor |
| `decimal(18,4)` / `decimal(18,2)` | `NUMERIC(18,4)` / `NUMERIC(18,2)` | Equivalente exacto |
| `double` | `DOUBLE PRECISION` | Equivalente |
| `varchar` con IPs (`direccionIp`, `ip`) | `INET` | Validación y comparación de red nativas; en la migración se convierte con guardia (valores no-IP → NULL) |
| `datetime(3)` | `TIMESTAMP WITH TIME ZONE` (`timestamptz`) | pgloader convierte; se prefiere siempre con zona |
| `datetime` que es un evento | columna con sufijo `_en` | Convención del documento §5 (`creado_en`, `iniciado_en`, `movido_en`...) |
| `date` (solo fecha) | `DATE` | Ej. `fecha_nacimiento`, `festivos.fecha` |
| `tinyint(1)` | `BOOLEAN` | pgloader lo convierte; columna con prefijo `es_` / `tiene_` |
| `varchar(191)` (límite de índice utf8mb4 de MySQL) | ancho real según el dato (`VARCHAR(254)` para correo, `VARCHAR(100)` para teléfono, etc.) | El `191` es un artefacto de MySQL sin sentido en PostgreSQL |

Además, el token de refresco de las sesiones viejas (texto plano) se guarda
**hasheado** (`sha256`) en el modelo nuevo.

## Consecuencias

| Aspecto | Impacto |
|---|---|
| Anchos de columna | Hubo un ajuste durante la migración (`cedula`/`telefono`/`telefono_emergencia` de `VARCHAR(30)` a `VARCHAR(100)`) porque los datos de prueba superaban el ancho supuesto. Cuando el ancho real se desconoce, mejor pecar de amplio. |
| Enums | Cada valor nuevo permitido es un `ALTER TABLE ... DROP CONSTRAINT / ADD CONSTRAINT` en un changeset — más verboso que un `enum` pero sin bloqueos. |
| JSON | Las consultas sobre `datos_crudos`, `temas`, etc. usan operadores `JSONB` (`->`, `->>`, `@>`). |

## Cómo se podría revertir o evolucionar

Si un `CHECK` de enum crece mucho (muchos valores, cambios frecuentes), se puede
evaluar un `ENUM` nativo de PostgreSQL o una tabla catálogo con FK. Por ahora
`VARCHAR` + `CHECK` es suficiente y uniforme.
