-- ============================================================
-- Transformacion  staging.*  ->  producto.*   (ola 2: modulo tareas)
-- Se ejecuta en: db_cliente_guajiranet  (como guajiranet_owner)
-- fotos_de_acta, evidencias_de_tarea, fotos_de_evidencia, mensajes_de_tarea:
--   vacias en el origen, solo estructura.
-- ============================================================

BEGIN;

ALTER TABLE producto.tbl_tareas    ADD COLUMN id_origen text;
ALTER TABLE producto.tbl_clientes  ADD COLUMN id_origen text;
ALTER TABLE producto.tbl_cuadrillas ADD COLUMN id_origen text;
ALTER TABLE producto.tbl_nodos      ADD COLUMN id_origen text;  -- vacio en el origen; join siempre da NULL
ALTER TABLE seguridad.tbl_usuarios ADD COLUMN id_origen text;
UPDATE producto.tbl_clientes c   SET id_origen = s.id FROM staging.clientes s   WHERE lower(coalesce(s.numerodocumento,'')) <> '' AND s.numerodocumento = c.numero_documento;
UPDATE producto.tbl_cuadrillas q SET id_origen = s.id FROM staging.cuadrillas s WHERE s.nombre = q.nombre;
UPDATE seguridad.tbl_usuarios u  SET id_origen = s.id FROM staging.usuarios s   WHERE lower(s.correo) = lower(u.correo);

-- ---------- tareas ----------
INSERT INTO producto.tbl_tareas (
    id_visible, titulo, descripcion, estado, prioridad,
    creado_por_id, asignado_a_id, cliente_id, cuadrilla_id, nodo_id, proceso_venta_id,
    etapa_asignacion, rol_asignado, reasignado_a, tipo_reasignacion,
    tipo_tarea, tipo_problema, incumplimiento_motivo,
    nombre_cliente, telefono_cliente, cedula_cliente, id_sucursal_sigma, id_contrato_sigma,
    barrio, municipio, departamento, plan, sucursal_texto, minutos_tiempo_reaccion,
    total_ms_pausado, ubicacion, lat_inicio, lng_inicio,
    asignado_en, pausado_en, iniciado_en, finalizado_en, programado_en, archivado_en,
    creado_en, actualizado_en, id_origen)
SELECT
    s.idvisible::integer, s.titulo, s.descripcion, s.estado::text, s.prioridad::text,
    uc.id, ua.id, cl.id, cu.id, no.id, NULL,
    s.etapaasignacion, s.rolasignado, s.reasignadoa, s.tiporeasignacion,
    s.tipotarea, s.tipoproblema, s.incumplimientomotivo,
    s.nombrecliente, s.telefonocliente, s.cedulacliente, s.idsucursal::integer, s.idcontrato::integer,
    s.barrio, s.municipio, s.departamento, s.plan, s.sucursaltexto, s.minutostiemporeaccion,
    s.totalmspausado, NULLIF(s.ubicacion,'')::jsonb, s.latinicio, s.lnginicio,
    s.asignadoen, s.pausadoen, s.horainicio, s.horafin, s.programadoen, s.archivadoen,
    s.creadoen, s.actualizadoen, s.id
FROM staging.tareas s
JOIN seguridad.tbl_usuarios uc ON uc.id_origen = s.creadoporid
LEFT JOIN seguridad.tbl_usuarios ua ON ua.id_origen = s.asignadoaid
LEFT JOIN producto.tbl_clientes  cl ON cl.id_origen = s.clienteid
LEFT JOIN producto.tbl_cuadrillas cu ON cu.id_origen = s.cuadrillaid
LEFT JOIN producto.tbl_nodos      no ON no.id_origen = s.nodoid;

-- ---------- historial_de_tareas ----------
INSERT INTO producto.tbl_historial_de_tareas (
    tarea_id, accion, tipo_objetivo, de_usuario_id, a_usuario_id, de_cuadrilla_id, a_cuadrilla_id,
    estado_anterior, estado_nuevo, notas, realizado_por_id, creado_en)
SELECT
    t.id, s.accion, s.tipoobjetivo, du.id, au.id, dc.id, ac.id,
    s.estadoanterior::text, s.estadonuevo::text, s.notas, rp.id, s.creadoen
FROM staging.historial_de_tareas s
JOIN producto.tbl_tareas t ON t.id_origen = s.tareaid
JOIN seguridad.tbl_usuarios rp ON rp.id_origen = s.realizadoporid
LEFT JOIN seguridad.tbl_usuarios du ON du.id_origen = s.deusuarioid
LEFT JOIN seguridad.tbl_usuarios au ON au.id_origen = s.ausuarioid
LEFT JOIN producto.tbl_cuadrillas dc ON dc.id_origen = s.decuadrillaid
LEFT JOIN producto.tbl_cuadrillas ac ON ac.id_origen = s.acuadrillaid;

-- ---------- actas_de_tarea ----------
INSERT INTO producto.tbl_actas_de_tarea (
    tarea_id, id_visible_tarea, tecnico_nombre, cuadrilla_nombre, asistentes, identificacion,
    id_sucursal_sigma, id_contrato_sigma, clasificacion, nombre_completo, movil, direccion, plan,
    barrio, municipio, descripcion_tarea, observaciones, soluciones, diagnosticos, firma_cliente,
    tipo_conector, latitud, longitud, iniciada_en, finalizada_en, creado_en, actualizado_en)
SELECT
    t.id, s.idvisibletarea::integer, s.tecniconombre, s.cuadrillanombre, NULLIF(s.asistentes,'')::jsonb, s.identificacion,
    s.idsucursal::integer, s.idcontratosigma::integer, s.clasificacion, s.nombrecompleto, s.movil, s.direccion, s.plan,
    s.barrio, s.municipio, s.descripciontarea, s.observaciones, NULLIF(s.soluciones,'')::jsonb, NULLIF(s.diagnosticos,'')::jsonb, s.firmacliente,
    s.tipoconector, s.latitud, s.longitud, s.fechainicio, s.fechafin, s.creadoen, s.actualizadoen
FROM staging.actas_de_tarea s
LEFT JOIN producto.tbl_tareas t ON t.id_origen = s.tareaid;

-- ---------- puntos_de_seguimiento ----------
INSERT INTO producto.tbl_puntos_de_seguimiento (
    tarea_id, usuario_id, latitud, longitud, precision_gps, velocidad, registrado_en)
SELECT t.id, u.id, s.latitud, s.longitud, s.precision, s.velocidad, s.marcadetiempo
FROM staging.puntos_de_seguimiento s
JOIN seguridad.tbl_usuarios u ON u.id_origen = s.usuarioid
LEFT JOIN producto.tbl_tareas t ON t.id_origen = s.tareaid;

-- ---------- ordenes_instalacion ----------
INSERT INTO producto.tbl_ordenes_instalacion (
    identificacion, id_proceso_venta_externo, ticket_instalacion, nombre_cliente, direccion, movil,
    email, plan, departamento, ciudad, barrio, dpto_codigo, mun_codigo, link_pago,
    url_imagenes_identificacion, url_imagenes_recibo_publico, estado, creado_en, actualizado_en)
SELECT
    identificacion::integer, idprocesoventa::integer, ticketinstalacion, nombrecliente, direccion, movil,
    email, plan, departamento, ciudad, barrio, dptocodigo, muncodigo, linkpago,
    NULLIF(urlimagenesidentificacion,'')::jsonb, NULLIF(urlimagenesrecibopublico,'')::jsonb, estado, creadoen, actualizadoen
FROM staging.ordenes_instalacion;

COMMIT;

-- Validacion
SELECT 'tareas' t, (SELECT count(*) FROM staging.tareas), (SELECT count(*) FROM producto.tbl_tareas)
UNION ALL SELECT 'historial_de_tareas', (SELECT count(*) FROM staging.historial_de_tareas), (SELECT count(*) FROM producto.tbl_historial_de_tareas)
UNION ALL SELECT 'actas_de_tarea', (SELECT count(*) FROM staging.actas_de_tarea), (SELECT count(*) FROM producto.tbl_actas_de_tarea)
UNION ALL SELECT 'puntos_de_seguimiento', (SELECT count(*) FROM staging.puntos_de_seguimiento), (SELECT count(*) FROM producto.tbl_puntos_de_seguimiento)
UNION ALL SELECT 'ordenes_instalacion', (SELECT count(*) FROM staging.ordenes_instalacion), (SELECT count(*) FROM producto.tbl_ordenes_instalacion)
UNION ALL SELECT 'tareas con cliente', (SELECT count(*) FROM staging.tareas WHERE clienteid IS NOT NULL), (SELECT count(*) FROM producto.tbl_tareas WHERE cliente_id IS NOT NULL)
UNION ALL SELECT 'tareas con cuadrilla', (SELECT count(*) FROM staging.tareas WHERE cuadrillaid IS NOT NULL), (SELECT count(*) FROM producto.tbl_tareas WHERE cuadrilla_id IS NOT NULL);
