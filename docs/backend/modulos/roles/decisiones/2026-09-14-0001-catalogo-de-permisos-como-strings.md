# ADR 0001 (roles) — El catálogo de permisos es texto, sin fuente única de verdad automática

**Fecha:** 2026-09-14
**Estado:** Aceptada

## Resumen

El nombre de un permiso (`"usuarios:crear"`, `"marca:editar"`, etc.) es un string literal que aparece en **tres lugares independientes**, y tiene que coincidir exactamente en los tres para que la protección funcione de punta a punta:

1. **Catálogo en base de datos** (`seguridad.tbl_permisos`) — sembrado por un changeset de Liquibase.
2. **Backend** — `@PreAuthorize("hasAuthority('usuarios:crear')")` en el controller.
3. **Frontend** — `authService.tienePermiso('usuarios:crear')` en un guard de ruta o un `@if` de un botón.

No hay ningún mecanismo (compilador, test, chequeo al arrancar) que garantice que las tres copias coincidan.

## Contexto

Se evaluaron dos formas de modelar los permisos:

| Opción | Consideración |
|---|---|
| **Strings en tabla, chequeados en runtime** (elegida) | Es el patrón estándar de la industria (Spring Security `hasAuthority`, scopes de OAuth/GitHub, acciones de AWS IAM) — y es el único que permite que un admin cree roles y asigne permisos **sin desplegar código nuevo**, que es el objetivo completo de tener la pantalla "Roles y permisos". |
| Enum compilado en Java, generado hacia la base y hacia el frontend | Ganaría chequeo en tiempo de compilación en el backend, pero rompe la premisa central: los permisos dejarían de ser datos administrables, un catálogo nuevo exigiría un release. Se descarta. |

La opción elegida es la correcta arquitectónicamente, pero tiene un costo real: **el riesgo de que las tres copias se desincronicen queda 100% en manos de la disciplina de quien programe**. Si alguien agrega `@PreAuthorize("hasAuthority('omnicanal:exportar')")` en un controller nuevo y se olvida de sembrar `omnicanal:exportar` en el catálogo, ese endpoint queda inaccesible (403 silencioso, nadie lo nota hasta que alguien lo prueba).

## Decisión

1. **Se mantiene el patrón de strings en tabla** — es correcto, no se cambia.
2. **Se documenta un runbook obligatorio** ("Cómo agregar un permiso nuevo", ver `README.md` del módulo) que cualquiera que agregue un endpoint protegido debe seguir — sembrar el permiso en el mismo PR que agrega el `@PreAuthorize`, nunca por separado.
3. **El otorgamiento al rol `ADMIN` se automatiza dentro del propio changeset de Liquibase que siembra el permiso** (no solo en el momento de aprovisionar una empresa nueva, que ya lo hacía `EjecutorDdlPostgres.aplicarSemilla`). Así, correr `mvn liquibase:update@aplicar-cliente` contra CUALQUIER base de cliente (nueva o ya existente) deja el permiso otorgado al ADMIN sin un paso manual aparte. Ver la plantilla de changeset en el README.

## Consecuencias

| Capa | Impacto |
|---|---|
| Proceso | Agregar un permiso pasa a ser un checklist de 4 pasos documentado (catálogo + grant automático + `@PreAuthorize` + gating de frontend), no una decisión libre de cada desarrollador. |
| Riesgo residual | Sigue sin haber un chequeo automático que *fuerce* el checklist — es documentación, no un guardrail de CI. Un descuido sigue siendo posible. |
| Deploy | Cada vez que se agrega un permiso a un módulo ya en producción, hay que correr `mvn liquibase:update@aplicar-cliente` contra cada base de cliente existente (rutina de deploy, no un paso especial) para que el ADMIN de esos tenants ya aprovisionados lo reciba. |

## Cómo se podría evolucionar

Si el equipo crece y los descuidos empiezan a pasar de verdad, el siguiente paso natural es un `ApplicationRunner` que al arrancar la app escanee (por reflexión) todos los `@PreAuthorize("hasAuthority('...')")` del classpath y compare esa lista contra `seguridad.tbl_permisos` de la base de control o de una base de cliente de referencia, logueando un `WARN` (o fallando el arranque en un perfil de CI) por cada nombre que aparezca en código pero no en el catálogo. No se construyó ahora porque el volumen de permisos actual (15) no lo justifica todavía — es la mitigación natural cuando ese volumen crezca.
