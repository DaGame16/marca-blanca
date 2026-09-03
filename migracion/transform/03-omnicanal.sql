-- ============================================================
-- Transformacion  staging.*  ->  omnicanal.*   (modulo omnicanal)
-- Se ejecuta en: db_cliente_guajiranet  (como guajiranet_owner)
-- ============================================================

BEGIN;

-- Columnas temporales id viejo -> id nuevo (se eliminan en 04-omnicanal-limpieza.sql).
ALTER TABLE omnicanal.tbl_canales_de_chat      ADD COLUMN id_origen text;
ALTER TABLE omnicanal.tbl_conversaciones_liwa  ADD COLUMN id_origen text;
ALTER TABLE omnicanal.tbl_casos_liwa           ADD COLUMN id_origen text;

-- id_origen temporal en seguridad.tbl_usuarios (se re-deriva por correo; se borra al final).
ALTER TABLE seguridad.tbl_usuarios ADD COLUMN id_origen text;
UPDATE seguridad.tbl_usuarios u
SET id_origen = s.id
FROM staging.usuarios s
WHERE lower(s.correo) = lower(u.correo);

-- ---------- canales_de_chat ----------
INSERT INTO omnicanal.tbl_canales_de_chat (tipo, nombre, filtro_de_rol, creado_en, id_origen)
SELECT tipo, nombre, filtroderol, creadoen, id
FROM staging.canales_de_chat;

-- ---------- mensajes_de_chat (0 filas en el origen; defensivo) ----------
INSERT INTO omnicanal.tbl_mensajes_de_chat (canal_id, remitente_id, contenido, editado_en, eliminado_en, creado_en)
SELECT c.id, u.id, s.contenido, s.editadoen, s.eliminadoen, s.creadoen
FROM staging.mensajes_de_chat s
JOIN omnicanal.tbl_canales_de_chat c ON c.id_origen = s.canalid
JOIN seguridad.tbl_usuarios       u ON u.id_origen = s.remitenteid;

-- ---------- participantes_de_chat (0 filas en el origen; defensivo) ----------
INSERT INTO omnicanal.tbl_participantes_de_chat (canal_id, usuario_id, ultima_lectura_en, oculto_en, limpiado_en, unido_en)
SELECT c.id, u.id, s.ultimalecturaen, s.ocultoen, s.limpiadoen, s.unidoen
FROM staging.participantes_de_chat s
JOIN omnicanal.tbl_canales_de_chat c ON c.id_origen = s.canalid
JOIN seguridad.tbl_usuarios       u ON u.id_origen = s.usuarioid;

-- ---------- conversaciones_liwa ----------
INSERT INTO omnicanal.tbl_conversaciones_liwa (
    id_contacto, nombre_contacto, historial_chat_completo, datos_crudos, es_de_ads, creado_en, archivada_en, id_origen)
SELECT
    idcontacto, nombrecontacto, historialchatcompleto, datoscrudos::jsonb,
    COALESCE(vienedeads, false), creadoen, archivadaen, id
FROM staging.conversaciones_liwa;

-- ---------- turnos_conversacion_liwa ----------
INSERT INTO omnicanal.tbl_turnos_conversacion_liwa (
    conversacion_id, orden, autor, nombre_autor, mensaje, ocurrido_en)
SELECT cl.id, s.orden, s.autor::text, s.nombreautor, s.mensaje, s.fecha
FROM staging.turnos_conversacion_liwa s
JOIN omnicanal.tbl_conversaciones_liwa cl ON cl.id_origen = s.conversacionid;

-- ---------- casos_liwa ----------
INSERT INTO omnicanal.tbl_casos_liwa (
    conversacion_id, turno_orden_inicio, turno_orden_fin, es_procesada, es_de_ads, archivada_en, id_origen)
SELECT cl.id, s.turnoordeninicio, s.turnoordenfin, s.procesada, COALESCE(s.vienedeads, false), s.archivadaen, s.id
FROM staging.casos_liwa s
JOIN omnicanal.tbl_conversaciones_liwa cl ON cl.id_origen = s.conversacionid;

-- ---------- conversaciones_analizadas ----------
INSERT INTO omnicanal.tbl_conversaciones_analizadas (
    caso_id, id_contacto, area_destino, municipio, barrio, categoria_oficina,
    motivo_contacto, submotivo, resumen_motivo, resumen_desenlace,
    sentimiento_inicial, sentimiento_final, resultado, fcr, esfuerzo_cliente,
    temas, banderas_calidad, oportunidad_venta, venta_confirmada_en_texto,
    revisar_limite, abandono, abandonado_por, es_de_ads, modelo_ia_usado, razonamiento,
    cerrado_en, primer_mensaje_en, primera_respuesta_en, procesado_en)
SELECT
    ca.id, s.idcontacto, s.areadestino, s.municipio, s.barrio, s.categoriaoficina,
    s.motivocontacto, s.submotivo, s.resumenmotivo, s.resumendesenlace,
    s.sentimientoinicial, s.sentimientofinal, s.resultado, s.fcr, s.esfuerzocliente,
    NULLIF(s.temas, '')::jsonb, NULLIF(s.banderascalidad, '')::jsonb,
    s.oportunidadventa, s.ventaconfirmadaentexto, s.revisarlimite, s.abandono,
    s.abandonadopor, COALESCE(s.vienedeads, false), s.modeloiausado, s.razonamiento,
    s.tscierre, s.tsprimermensaje, s.tsprimerarespuesta, s.procesadoen
FROM staging.conversaciones_analizadas s
JOIN omnicanal.tbl_casos_liwa ca ON ca.id_origen = s.casoid;

-- ---------- seguimientos_ia ----------
-- tarea_id queda NULL: el cuid de tareaid no mapea hasta migrar el modulo producto.
INSERT INTO omnicanal.tbl_seguimientos_ia (
    cedula, celular, descripcion, conversacion, importancia, estado, area,
    es_convertido_en_ticket, tarea_id, id_solicitud, creado_en, actualizado_en)
SELECT
    cedula, celular, descripcion, conversacion, importancia, estado, area,
    convertidoenticket, NULL, idsolicitud::integer, creadoen, actualizadoen
FROM staging.seguimientos_ia;

COMMIT;

-- ============================================================
-- Validacion de conteos
-- ============================================================
SELECT 'canales_de_chat' t,
       (SELECT count(*) FROM staging.canales_de_chat), (SELECT count(*) FROM omnicanal.tbl_canales_de_chat)
UNION ALL SELECT 'mensajes_de_chat',
       (SELECT count(*) FROM staging.mensajes_de_chat), (SELECT count(*) FROM omnicanal.tbl_mensajes_de_chat)
UNION ALL SELECT 'participantes_de_chat',
       (SELECT count(*) FROM staging.participantes_de_chat), (SELECT count(*) FROM omnicanal.tbl_participantes_de_chat)
UNION ALL SELECT 'conversaciones_liwa',
       (SELECT count(*) FROM staging.conversaciones_liwa), (SELECT count(*) FROM omnicanal.tbl_conversaciones_liwa)
UNION ALL SELECT 'turnos_conversacion_liwa',
       (SELECT count(*) FROM staging.turnos_conversacion_liwa), (SELECT count(*) FROM omnicanal.tbl_turnos_conversacion_liwa)
UNION ALL SELECT 'casos_liwa',
       (SELECT count(*) FROM staging.casos_liwa), (SELECT count(*) FROM omnicanal.tbl_casos_liwa)
UNION ALL SELECT 'conversaciones_analizadas',
       (SELECT count(*) FROM staging.conversaciones_analizadas), (SELECT count(*) FROM omnicanal.tbl_conversaciones_analizadas)
UNION ALL SELECT 'seguimientos_ia',
       (SELECT count(*) FROM staging.seguimientos_ia), (SELECT count(*) FROM omnicanal.tbl_seguimientos_ia);
