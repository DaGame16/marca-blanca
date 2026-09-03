-- ============================================================
-- Transformacion  staging.*  ->  producto.*   (ola 6: ventas)
-- Se ejecuta en: db_cliente_guajiranet  (como guajiranet_owner)
-- Con datos en el dump: procesos_de_venta (2), solicitudes_cobertura (2).
-- El resto de ventas (cotizaciones, pedidos, facturas, devoluciones...) esta vacio.
-- ============================================================

BEGIN;
SET LOCAL plataforma.auditoria_activa = 'off';

-- ---------- procesos_de_venta ----------
INSERT INTO producto.tbl_procesos_de_venta (
    id_proceso_venta_externo, id_sucursal_sigma, nit, nombre_cliente, direccion, movil, correo,
    plan, departamento, ciudad, nombre_barrio, estado, proceso, latitud, longitud, creado_en, actualizado_en)
SELECT
    idprocesoventa::integer, idsucursal::integer, nit, nombrecliente, direccion, movil, correo,
    plan, departamento, ciudad, nombrebarrio, estado, proceso, latitud, longitud, creadoen, actualizadoen
FROM staging.procesos_de_venta;

-- ---------- solicitudes_cobertura ----------
INSERT INTO producto.tbl_solicitudes_cobertura (
    nombre, telefono, direccion, barrio, municipio, estado, ticket_validacion, creado_en, actualizado_en)
SELECT
    nombre, telefono, direccion, barrio, municipio, estado, ticketvalidacion, creadoen, actualizadoen
FROM staging.solicitudes_cobertura;

COMMIT;

SELECT 'procesos_de_venta' t, (SELECT count(*) FROM staging.procesos_de_venta), (SELECT count(*) FROM producto.tbl_procesos_de_venta)
UNION ALL SELECT 'solicitudes_cobertura', (SELECT count(*) FROM staging.solicitudes_cobertura), (SELECT count(*) FROM producto.tbl_solicitudes_cobertura);
