-- ============================================================
-- Limpieza post-validacion del modulo omnicanal.
-- Se ejecuta en: db_cliente_guajiranet  (como guajiranet_owner)
-- SOLO despues de validar los conteos de 03-omnicanal.sql.
-- ============================================================

ALTER TABLE omnicanal.tbl_canales_de_chat     DROP COLUMN id_origen;
ALTER TABLE omnicanal.tbl_conversaciones_liwa DROP COLUMN id_origen;
ALTER TABLE omnicanal.tbl_casos_liwa          DROP COLUMN id_origen;
ALTER TABLE seguridad.tbl_usuarios            DROP COLUMN id_origen;
