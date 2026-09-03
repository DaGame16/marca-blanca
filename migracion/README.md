# Workspace de migración (efímero, solo DEV)

Copia el sistema viejo (MariaDB) a un schema `staging` en Postgres y luego
transforma los datos hacia el modelo nuevo, módulo por módulo.

`staging` es un schema **solo de migración**, excepción a los schemas del
documento (`plataforma` / `producto` / `cliente`). Se elimina tras el corte.

Sistema viejo: MariaDB 10.4, base `erp_saas_test`, hecho con Prisma.
Una sola empresa en el dump: **GuajiraNet** → slug `guajiranet` →
base destino `db_cliente_guajiranet`.

## 1. Colocar el dump
Los `.sql` del sistema actual (CON DATOS) van en `dump/` (gitignored).
Ya están los 76 archivos por tabla (sin `_prisma_migrations`).

## 2. Levantar MariaDB + pgloader
    docker compose -f migracion/compose.yaml up -d

Esperar a que MariaDB termine de importar:
    docker logs -f migracion-mariadb-origen
(hasta ver "ready for connections" y que pare de crear tablas)

Verificar:
    docker exec -it migracion-mariadb-origen mysql -uroot -pmigracion erp_saas_test -e "SHOW TABLES;"

## 3. Crear la base de cliente (clonada de la plantilla, seccion 2.3)
    docker exec -it marca-blanca-postgres psql -U guajiranet_owner -d db_plantilla_maestra ^
      -c "CREATE DATABASE db_cliente_guajiranet TEMPLATE db_plantilla_maestra"

Nace con la estructura del módulo `seguridad` + los grants de rol ya aplicados.

## 4. Cargar staging (copia cruda MySQL -> Postgres)
    docker exec migracion-pgloader pgloader /work/staging.load

## 5. Verificar la carga
    docker exec -it marca-blanca-postgres psql -U guajiranet_owner -d db_cliente_guajiranet -c "\dt staging.*"
    docker exec -it marca-blanca-postgres psql -U guajiranet_owner -d db_cliente_guajiranet -c "\d staging.usuarios"

## 6. Transformar el módulo usuarios
    # registrar la empresa en la base de control
    docker exec -i marca-blanca-postgres psql -U guajiranet_owner -d db_portal_guajiranet_control < migracion/transform/00-registrar-empresa-en-control.sql
    # cargar staging -> seguridad
    docker exec -i marca-blanca-postgres psql -U guajiranet_owner -d db_cliente_guajiranet < migracion/transform/01-seguridad.sql
    # tras validar los conteos: quitar columnas temporales
    docker exec -i marca-blanca-postgres psql -U guajiranet_owner -d db_cliente_guajiranet < migracion/transform/02-seguridad-limpieza.sql

## Siguiente
Repetir el patrón (staging -> schema destino) para los demás módulos:
omnicanal (schema `omnicanal`), núcleo ERP (schema `producto`),
config de tenant (schema `cliente`), auditoría heredada (schema `historico`).

## Apagar / limpiar
    docker compose -f migracion/compose.yaml down -v     # borra MariaDB y su volumen
    docker exec -it marca-blanca-postgres psql -U guajiranet_owner -d db_plantilla_maestra ^
      -c "DROP DATABASE db_cliente_guajiranet"
