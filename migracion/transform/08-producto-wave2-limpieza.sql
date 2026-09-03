-- Limpieza post-validacion de la ola 2 de producto (modulo tareas).
-- Se ejecuta en db_cliente_guajiranet como guajiranet_owner, SOLO tras validar 07-producto-wave2.sql.

ALTER TABLE producto.tbl_tareas     DROP COLUMN id_origen;
ALTER TABLE producto.tbl_clientes   DROP COLUMN id_origen;
ALTER TABLE producto.tbl_cuadrillas DROP COLUMN id_origen;
ALTER TABLE producto.tbl_nodos      DROP COLUMN id_origen;
ALTER TABLE seguridad.tbl_usuarios  DROP COLUMN id_origen;
