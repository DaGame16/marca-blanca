-- Limpieza post-validacion de la ola 7. Solo tras validar 10-wave7.sql.
ALTER TABLE seguridad.tbl_usuarios DROP COLUMN id_origen;
ALTER TABLE producto.tbl_tareas    DROP COLUMN id_origen;
