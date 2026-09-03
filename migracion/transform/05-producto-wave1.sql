-- ============================================================
-- Transformacion  staging.*  ->  producto.*   (ola 1: clientes, sucursales, cuadrillas)
-- Se ejecuta en: db_cliente_guajiranet  (como guajiranet_owner)
-- Catalogos base (unidades, categorias, bodegas, productos, proveedores,
-- codigos_equivalentes, nodos, festivos) estan vacios en el origen: solo estructura.
-- ============================================================

BEGIN;
SET LOCAL plataforma.auditoria_activa = 'off';

ALTER TABLE producto.tbl_clientes  ADD COLUMN id_origen text;
ALTER TABLE seguridad.tbl_usuarios ADD COLUMN id_origen text;
UPDATE seguridad.tbl_usuarios u
SET id_origen = s.id
FROM staging.usuarios s
WHERE lower(s.correo) = lower(u.correo);

-- ---------- clientes ----------
INSERT INTO producto.tbl_clientes (
    nombre_completo, tipo_documento, numero_documento, telefono, telefono_fijo, correo,
    direccion, barrio, municipio, departamento, plan,
    tiene_tv, tiene_sim, tiene_marketing, saldo_cartera, dias_cartera, clasificacion, url_pago,
    id_contrato_sigma, estado_contrato, id_sucursal_sigma, serial_ont, puerto_ont,
    latitud, longitud, ubicacion_actualizada_en, sincronizado_en, sigma_actualizado_en,
    creado_en, actualizado_en, id_origen)
SELECT
    nombrecompleto, tipodocumento, numerodocumento, telefono, telefonofijo, correo,
    direccion, barrio, municipio, departamento, plan,
    tienetv, tienesim, tienemarketing, saldocartera, diascartera, clasificacion, urlpago,
    idcontratosigma::integer, estadocontrato, idsucursal::integer, serialont, puertoont,
    latitud, longitud, ubicacionactualizadaen, fechasincronizacion, ultimaactualizacionsigma,
    creadoen, actualizadoen, id
FROM staging.clientes;

-- ---------- sucursales_de_cliente ----------
INSERT INTO producto.tbl_sucursales_de_cliente (
    cliente_id, id_sucursal_sigma, id_contrato_sigma, direccion, barrio, municipio, departamento,
    plan, movil, serial_ont, puerto_ont, estatus, creado_en, actualizado_en)
SELECT
    c.id, s.idsucursal::integer, s.idcontrato::integer, s.direccion, s.barrio, s.municipio, s.departamento,
    s.plan, s.movil, s.serialont, s.puertoont, s.estatus, s.creadoen, s.actualizadoen
FROM staging.sucursales_de_cliente s
JOIN producto.tbl_clientes c ON c.id_origen = s.clienteid;

-- ---------- cuadrillas ----------
INSERT INTO producto.tbl_cuadrillas (nombre, zona, coordinador_id, lider_id, creado_en, actualizado_en)
SELECT s.nombre, s.zona, uc.id, ul.id, s.creadoen, s.actualizadoen
FROM staging.cuadrillas s
LEFT JOIN seguridad.tbl_usuarios uc ON uc.id_origen = s.coordinadorid
LEFT JOIN seguridad.tbl_usuarios ul ON ul.id_origen = s.liderid;

COMMIT;

-- Validacion
SELECT 'clientes' t, (SELECT count(*) FROM staging.clientes), (SELECT count(*) FROM producto.tbl_clientes)
UNION ALL SELECT 'sucursales_de_cliente', (SELECT count(*) FROM staging.sucursales_de_cliente), (SELECT count(*) FROM producto.tbl_sucursales_de_cliente)
UNION ALL SELECT 'cuadrillas', (SELECT count(*) FROM staging.cuadrillas), (SELECT count(*) FROM producto.tbl_cuadrillas)
UNION ALL SELECT 'cuadrillas con coordinador', (SELECT count(*) FROM staging.cuadrillas WHERE coordinadorid IS NOT NULL), (SELECT count(*) FROM producto.tbl_cuadrillas WHERE coordinador_id IS NOT NULL)
UNION ALL SELECT 'cuadrillas con lider', (SELECT count(*) FROM staging.cuadrillas WHERE liderid IS NOT NULL), (SELECT count(*) FROM producto.tbl_cuadrillas WHERE lider_id IS NOT NULL);
