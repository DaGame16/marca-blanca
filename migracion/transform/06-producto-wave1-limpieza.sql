-- Limpieza post-validacion de la ola 1 de producto.
-- Se ejecuta en db_cliente_guajiranet como guajiranet_owner, SOLO tras validar 05-producto-wave1.sql.

ALTER TABLE producto.tbl_clientes  DROP COLUMN id_origen;
ALTER TABLE seguridad.tbl_usuarios DROP COLUMN id_origen;
