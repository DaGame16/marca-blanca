-- ============================================================
-- Limpieza post-validacion del modulo usuarios.
-- Se ejecuta en: db_cliente_guajiranet  (como guajiranet_owner)
-- SOLO despues de validar los conteos de 01-seguridad.sql.
--
-- Quita las columnas temporales id_origen: el modelo nuevo no guarda
-- el id del sistema viejo en las tablas vivas (el mapeo se archiva aparte).
-- ============================================================

ALTER TABLE seguridad.tbl_roles     DROP COLUMN id_origen;
ALTER TABLE seguridad.tbl_permisos  DROP COLUMN id_origen;
ALTER TABLE seguridad.tbl_usuarios  DROP COLUMN id_origen;
