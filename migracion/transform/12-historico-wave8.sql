-- ============================================================
-- Transformacion  staging.*  ->  historico.*   (ola 8: auditoria heredada)
-- Se ejecuta en: db_cliente_guajiranet  (como guajiranet_owner)
-- logs_de_auditoria (217), logs_de_auditoria_de_solicitudes (4450).
-- Datos de solo lectura, formato heredado.
-- ============================================================

BEGIN;
SET LOCAL plataforma.auditoria_activa = 'off';

ALTER TABLE seguridad.tbl_usuarios ADD COLUMN id_origen text;
UPDATE seguridad.tbl_usuarios u SET id_origen = s.id FROM staging.usuarios s WHERE lower(s.correo) = lower(u.correo);

-- ---------- logs_de_auditoria ----------
INSERT INTO historico.tbl_logs_de_auditoria (
    usuario_id, accion, entidad, id_entidad, datos_anteriores, datos_nuevos, direccion_ip, hash, creado_en)
SELECT
    u.id, s.accion, s.entidad, s.identidad,
    NULLIF(s.datosanteriores,'')::jsonb, NULLIF(s.datosnuevos,'')::jsonb,
    CASE WHEN s.direccionip ~ '^[0-9a-fA-F:.]+$' THEN s.direccionip::inet END,
    s.hash, s.creadoen
FROM staging.logs_de_auditoria s
LEFT JOIN seguridad.tbl_usuarios u ON u.id_origen = s.usuarioid;

-- ---------- logs_de_auditoria_de_solicitudes ----------
INSERT INTO historico.tbl_logs_de_auditoria_de_solicitudes (
    usuario_id, correo_usuario, rol_usuario, metodo, ruta, codigo_estado, direccion_ip,
    agente_usuario, plataforma, duracion_ms, creado_en)
SELECT
    u.id, s.correousuario, s.rolusuario, s.metodo, s.ruta, s.codigoestado::integer,
    CASE WHEN s.direccionip ~ '^[0-9a-fA-F:.]+$' THEN s.direccionip::inet END,
    s.agenteusuario, s.plataforma, s.duracionms::integer, s.creadoen
FROM staging.logs_de_auditoria_de_solicitudes s
LEFT JOIN seguridad.tbl_usuarios u ON u.id_origen = s.usuarioid;

COMMIT;

SELECT 'logs_de_auditoria' t, (SELECT count(*) FROM staging.logs_de_auditoria), (SELECT count(*) FROM historico.tbl_logs_de_auditoria)
UNION ALL SELECT 'logs_de_auditoria_de_solicitudes', (SELECT count(*) FROM staging.logs_de_auditoria_de_solicitudes), (SELECT count(*) FROM historico.tbl_logs_de_auditoria_de_solicitudes)
UNION ALL SELECT 'auditoria con usuario resuelto', (SELECT count(*) FROM staging.logs_de_auditoria WHERE usuarioid IS NOT NULL), (SELECT count(*) FROM historico.tbl_logs_de_auditoria WHERE usuario_id IS NOT NULL);
