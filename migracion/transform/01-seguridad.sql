-- ============================================================
-- Transformacion  staging.*  ->  seguridad.*   (modulo usuarios)
-- Se ejecuta en: db_cliente_guajiranet  (como guajiranet_owner)
-- Empresa unica en el dump: GuajiraNet.
--
-- Nota: los nombres de columna de staging vienen en minuscula
-- (pgloader baja a minuscula: hashContrasena -> hashcontrasena).
-- ============================================================

BEGIN;

-- Columnas temporales para mapear id viejo (cuid texto) -> id nuevo (bigint).
-- Se eliminan en 02-seguridad-limpieza.sql tras validar (no id_legado en tablas vivas).
ALTER TABLE seguridad.tbl_roles     ADD COLUMN id_origen text;
ALTER TABLE seguridad.tbl_permisos  ADD COLUMN id_origen text;
ALTER TABLE seguridad.tbl_usuarios  ADD COLUMN id_origen text;

-- ---------- roles ----------
INSERT INTO seguridad.tbl_roles (nombre, descripcion, es_del_sistema, creado_en, actualizado_en, id_origen)
SELECT nombre, descripcion, esdelsistema, creadoen, actualizadoen, id
FROM staging.roles;

-- ---------- permisos ----------
INSERT INTO seguridad.tbl_permisos (nombre, descripcion, creado_en, id_origen)
SELECT nombre, descripcion, creadoen, id
FROM staging.permisos;

-- ---------- usuarios (identidad + acceso) ----------
INSERT INTO seguridad.tbl_usuarios (
    correo, hash_contrasena, nombre_completo, es_activo,
    ultimo_inicio_en, intentos_fallidos, bloqueado_hasta,
    secreto_totp, es_totp_habilitado, es_doble_factor_omitido,
    id_dispositivo_vinculado, es_powerbi_habilitado, hash_secreto_powerbi,
    creado_en, actualizado_en, id_origen)
SELECT
    correo, hashcontrasena, nombrecompleto, activo,
    ultimoiniciosesion, intentosfallidos, bloqueadohasta,
    secretototp, totphabilitado, omitirdoblefactor,
    iddispositivovinculado, powerbihabilitado, hashsecretopowerbi,
    creadoen, actualizadoen, id
FROM staging.usuarios;

-- ---------- perfiles (datos personales/laborales, 1:1 con usuarios) ----------
INSERT INTO seguridad.tbl_usuario_perfiles (
    usuario_id, id_empleado, url_foto, cedula, tipo_documento, fecha_nacimiento,
    tipo_sangre, notas_medicas, telefono, direccion,
    contacto_emergencia, telefono_emergencia, zona, cuadrilla_id, estado_laboral,
    creado_en, actualizado_en)
SELECT
    u.id, s.idempleado, s.urlfoto, s.cedula, s.tipodocumento, s.fechanacimiento::date,
    s.tiposangre, s.notasmedicas, s.telefono, s.direccion,
    s.contactoemergencia, s.telefonoemergencia, s.zona, NULL, s.estadolaboral,
    s.creadoen, s.actualizadoen
FROM staging.usuarios s
JOIN seguridad.tbl_usuarios u ON u.id_origen = s.id;
-- cuadrilla_id = NULL: la relacion con producto.tbl_cuadrillas se resuelve cuando exista ese modulo.

-- ---------- usuarios_roles ----------
-- El rol de cada usuario viene de staging.usuarios.rolid (la pivote vieja usuarios_roles esta vacia).
INSERT INTO seguridad.tbl_usuarios_roles (usuario_id, rol_id, asignado_en)
SELECT u.id, r.id, s.creadoen
FROM staging.usuarios s
JOIN seguridad.tbl_usuarios u ON u.id_origen = s.id
JOIN seguridad.tbl_roles    r ON r.id_origen = s.rolid
WHERE s.rolid IS NOT NULL;

-- ---------- permisos_de_rol ----------
INSERT INTO seguridad.tbl_permisos_de_rol (rol_id, permiso_id, asignado_en)
SELECT r.id, p.id, now()
FROM staging.permisos_de_rol pr
JOIN seguridad.tbl_roles    r ON r.id_origen = pr.rolid
JOIN seguridad.tbl_permisos p ON p.id_origen = pr.permisoid;

-- ---------- permisos_de_usuario: vacia en el origen (nada que migrar) ----------

-- ---------- tokens_fcm ----------
INSERT INTO seguridad.tbl_tokens_fcm (usuario_id, token, dispositivo, creado_en)
SELECT u.id, s.token, s.dispositivo, s.creadoen
FROM staging.tokens_fcm s
JOIN seguridad.tbl_usuarios u ON u.id_origen = s.usuarioid;

-- ---------- sesiones ----------
-- hash_token_refresco = SHA-256 del token viejo (el modelo nuevo no guarda el token en claro).
INSERT INTO seguridad.tbl_sesiones (
    usuario_id, info_dispositivo, direccion_ip, hash_token_refresco, expira_en, creado_en)
SELECT
    u.id, s.infodispositivo,
    CASE WHEN s.direccionip ~ '^[0-9a-fA-F:.]+$' THEN s.direccionip::inet END,
    encode(sha256(s.tokenrefresco::bytea), 'hex'),
    s.expiraen, s.creadoen
FROM staging.sesiones s
JOIN seguridad.tbl_usuarios u ON u.id_origen = s.usuarioid;

-- ---------- dispositivos_confiables ----------
INSERT INTO seguridad.tbl_dispositivos_confiables (
    usuario_id, id_dispositivo, nombre_dispositivo, plataforma, ultimo_uso_en, expira_en, creado_en)
SELECT
    u.id, s.iddispositivo, s.nombredispositivo, s.plataforma, s.ultimousoen, s.expiraen, s.creadoen
FROM staging.dispositivos_confiables s
JOIN seguridad.tbl_usuarios u ON u.id_origen = s.usuarioid;

COMMIT;

-- ============================================================
-- Validacion de conteos  (origen  vs  destino)
-- ============================================================
SELECT 'roles'                  AS tabla,
       (SELECT count(*) FROM staging.roles)                                   AS origen,
       (SELECT count(*) FROM seguridad.tbl_roles)                             AS destino
UNION ALL SELECT 'permisos',
       (SELECT count(*) FROM staging.permisos),
       (SELECT count(*) FROM seguridad.tbl_permisos)
UNION ALL SELECT 'usuarios',
       (SELECT count(*) FROM staging.usuarios),
       (SELECT count(*) FROM seguridad.tbl_usuarios)
UNION ALL SELECT 'usuario_perfiles',
       (SELECT count(*) FROM staging.usuarios),
       (SELECT count(*) FROM seguridad.tbl_usuario_perfiles)
UNION ALL SELECT 'usuarios_roles',
       (SELECT count(*) FROM staging.usuarios WHERE rolid IS NOT NULL),
       (SELECT count(*) FROM seguridad.tbl_usuarios_roles)
UNION ALL SELECT 'permisos_de_rol',
       (SELECT count(*) FROM staging.permisos_de_rol),
       (SELECT count(*) FROM seguridad.tbl_permisos_de_rol)
UNION ALL SELECT 'sesiones',
       (SELECT count(*) FROM staging.sesiones),
       (SELECT count(*) FROM seguridad.tbl_sesiones)
UNION ALL SELECT 'dispositivos_confiables',
       (SELECT count(*) FROM staging.dispositivos_confiables),
       (SELECT count(*) FROM seguridad.tbl_dispositivos_confiables)
UNION ALL SELECT 'tokens_fcm',
       (SELECT count(*) FROM staging.tokens_fcm),
       (SELECT count(*) FROM seguridad.tbl_tokens_fcm);
