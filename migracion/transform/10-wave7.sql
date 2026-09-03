-- ============================================================
-- Transformacion  staging.*  ->  producto/cliente/seguridad   (ola 7: config del tenant)
-- Se ejecuta en: db_cliente_guajiranet  (como guajiranet_owner)
-- Con datos en el dump: notificaciones (22). avisos_globales y consentimientos_de_datos vacios.
-- ============================================================

BEGIN;
SET LOCAL plataforma.auditoria_activa = 'off';

ALTER TABLE seguridad.tbl_usuarios ADD COLUMN id_origen text;
ALTER TABLE producto.tbl_tareas    ADD COLUMN id_origen text;
UPDATE seguridad.tbl_usuarios u SET id_origen = s.id FROM staging.usuarios s WHERE lower(s.correo) = lower(u.correo);
UPDATE producto.tbl_tareas t     SET id_origen = s.id FROM staging.tareas s   WHERE s.idvisible IS NOT NULL AND s.idvisible = t.id_visible;

-- ---------- notificaciones ----------
INSERT INTO producto.tbl_notificaciones (
    usuario_id, tarea_id, titulo, mensaje, id_canal, tipo_canal, es_leida, creado_en)
SELECT u.id, t.id, s.titulo, s.mensaje, s.idcanal, s.tipocanal, s.leida, s.creadoen
FROM staging.notificaciones s
JOIN seguridad.tbl_usuarios u ON u.id_origen = s.usuarioid
LEFT JOIN producto.tbl_tareas t ON t.id_origen = s.tareaid;

-- ---------- avisos_globales: vacio en el origen ----------
INSERT INTO cliente.tbl_avisos_globales (creado_por_id, titulo, mensaje, es_activo, plataforma, creado_en, actualizado_en)
SELECT u.id, s.titulo, s.mensaje, s.activo, s.plataforma, s.creadoen, s.actualizadoen
FROM staging.avisos_globales s
JOIN seguridad.tbl_usuarios u ON u.id_origen = s.creadoporid;

-- ---------- consentimientos_de_datos: vacio en el origen ----------
INSERT INTO seguridad.tbl_consentimientos_de_datos (usuario_id, version_politica, direccion_ip, agente_usuario, aceptado_en)
SELECT u.id, s.versionpolitica,
       CASE WHEN s.ip ~ '^[0-9a-fA-F:.]+$' THEN s.ip::inet END,
       s.agenteusuario, s.aceptadoen
FROM staging.consentimientos_de_datos s
JOIN seguridad.tbl_usuarios u ON u.id_origen = s.usuarioid;

COMMIT;

SELECT 'notificaciones' t, (SELECT count(*) FROM staging.notificaciones), (SELECT count(*) FROM producto.tbl_notificaciones)
UNION ALL SELECT 'avisos_globales', (SELECT count(*) FROM staging.avisos_globales), (SELECT count(*) FROM cliente.tbl_avisos_globales)
UNION ALL SELECT 'consentimientos_de_datos', (SELECT count(*) FROM staging.consentimientos_de_datos), (SELECT count(*) FROM seguridad.tbl_consentimientos_de_datos)
UNION ALL SELECT 'notificaciones con tarea', (SELECT count(*) FROM staging.notificaciones WHERE tareaid IS NOT NULL), (SELECT count(*) FROM producto.tbl_notificaciones WHERE tarea_id IS NOT NULL);
