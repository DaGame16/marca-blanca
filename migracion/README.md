# Workspace de migración (efímero, solo DEV)

Copia el sistema viejo (MariaDB) a un schema `staging` en Postgres, para luego
transformar los datos hacia el modelo nuevo, módulo por módulo.

## 1. Colocar el dump
Pon el/los archivo(s) `.sql` del sistema actual (CON DATOS) en `dump/`.
Puede ser un único `dump.sql` o varios por tabla — se cargan todos al arrancar.

## 2. Levantar MariaDB + pgloader
    docker compose -f migracion/compose.yaml up -d

Esperar a que MariaDB termine de importar:
    docker logs -f migracion-mariadb-origen
(hasta ver "ready for connections" y que pare de crear tablas)

Verificar:
    docker exec -it migracion-mariadb-origen mysql -uroot -pmigracion erp_saas_test -e "SHOW TABLES;"

## 3. Crear la base de cliente de prueba (clonada de la plantilla)
    docker exec -it marca-blanca-postgres psql -U marca_blanca -d db_plantilla_maestra \
      -c "CREATE DATABASE db_cliente_demo TEMPLATE db_plantilla_maestra"

(db_cliente_demo nace con la estructura del módulo seguridad ya aplicada.)

## 4. Cargar staging
    docker exec migracion-pgloader pgloader /work/staging.load

## 5. Verificar la carga
    docker exec -it marca-blanca-postgres psql -U marca_blanca -d db_cliente_demo -c "\dt staging.*"
    docker exec -it marca-blanca-postgres psql -U marca_blanca -d db_cliente_demo -c "SELECT count(*) FROM staging.usuarios;"

## Siguiente
Escribir el SQL de transformación staging.* -> seguridad.* para una empresa,
validar conteos y FKs, y repetir por módulo.

## Apagar / limpiar
    docker compose -f migracion/compose.yaml down -v     # borra MariaDB y su volumen
    docker exec -it marca-blanca-postgres psql -U marca_blanca -d db_plantilla_maestra \
      -c "DROP DATABASE db_cliente_demo"l