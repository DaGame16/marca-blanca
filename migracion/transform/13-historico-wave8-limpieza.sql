-- Limpieza post-validacion de la ola 8. Solo tras validar 12-historico-wave8.sql.
ALTER TABLE seguridad.tbl_usuarios DROP COLUMN id_origen;
