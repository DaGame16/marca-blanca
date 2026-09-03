-- ============================================================
-- Registro de la empresa en la base de control (transversal).
-- Se ejecuta en: db_portal_guajiranet_control  (como guajiranet_owner)
-- Doc: seccion 2 / 2.1 / 2.3 (directorio de enrutamiento + version de esquema)
-- ============================================================

BEGIN;

-- ---------- Catalogo de modulos de la plataforma ----------
-- 3 modulos: usuarios y omnicanal (con tablas y logica), 3cx (aun sin tablas).
INSERT INTO plataforma.tbl_modulos (codigo, nombre, descripcion, creado_en, actualizado_en) VALUES
  ('usuarios',  'Usuarios y seguridad', 'Identidad, roles, permisos, sesiones',            now(), now()),
  ('omnicanal', 'Omnicanal (Liwa)',     'Canales de chat, conversaciones, seguimiento IA', now(), now()),
  ('3cx',       'Telefonia 3CX',        'Integracion telefonica (pendiente)',              now(), now())
ON CONFLICT (codigo) DO NOTHING;

-- ---------- Empresa: GuajiraNet (unica en el dump) ----------
INSERT INTO plataforma.tbl_empresas
  (identificador, nombre_legal, nombre_comercial, dominio, hash_contrasena_maestra, estado, creado_en, actualizado_en)
VALUES
  ('guajiranet', 'GuajiraNet', 'GuajiraNet', 'soft.test.guajiranet.com', NULL, 'activa',
   TIMESTAMPTZ '2026-08-12 14:18:42.170', now())
ON CONFLICT (identificador) DO NOTHING;

-- ---------- Marca blanca ----------
INSERT INTO plataforma.tbl_empresas_marca
  (empresa_id, url_logo, color_primario, color_secundario, dominio_propio, creado_en, actualizado_en)
SELECT e.id, NULL, '#4F46E5', NULL, 'soft.test.guajiranet.com', now(), now()
FROM plataforma.tbl_empresas e
WHERE e.identificador = 'guajiranet'
  AND NOT EXISTS (SELECT 1 FROM plataforma.tbl_empresas_marca m WHERE m.empresa_id = e.id);

-- ---------- Conexion fisica (directorio de enrutamiento, seccion 2.2) ----------
INSERT INTO plataforma.tbl_empresa_conexiones
  (empresa_id, host, puerto, nombre_bd, secreto_ref, es_activa, creado_en, actualizado_en)
SELECT e.id, 'localhost', 5432, 'db_cliente_guajiranet', 'dev/guajiranet', true, now(), now()
FROM plataforma.tbl_empresas e
WHERE e.identificador = 'guajiranet'
  AND NOT EXISTS (SELECT 1 FROM plataforma.tbl_empresa_conexiones c WHERE c.empresa_id = e.id);

-- ---------- Version de esquema aplicada (seccion 2.1) ----------
INSERT INTO plataforma.tbl_empresa_esquema_version
  (empresa_id, ultima_migracion_aplicada, aplicada_en)
SELECT e.id, '0012-otorgar-privilegios-roles', now()
FROM plataforma.tbl_empresas e
WHERE e.identificador = 'guajiranet'
  AND NOT EXISTS (SELECT 1 FROM plataforma.tbl_empresa_esquema_version v WHERE v.empresa_id = e.id);

-- ---------- Modulos activos para GuajiraNet ----------
-- usuarios y omnicanal activos; 3cx inactivo.
INSERT INTO plataforma.tbl_empresa_modulos
  (empresa_id, modulo_id, es_activo, activado_en, creado_en, actualizado_en)
SELECT e.id, m.id,
       (m.codigo IN ('usuarios', 'omnicanal')),
       CASE WHEN m.codigo IN ('usuarios', 'omnicanal') THEN now() END,
       now(), now()
FROM plataforma.tbl_empresas e
CROSS JOIN plataforma.tbl_modulos m
WHERE e.identificador = 'guajiranet'
  AND NOT EXISTS (
    SELECT 1 FROM plataforma.tbl_empresa_modulos em
    WHERE em.empresa_id = e.id AND em.modulo_id = m.id
  );

COMMIT;

-- ---------- Verificacion ----------
SELECT e.identificador, e.estado, c.nombre_bd, v.ultima_migracion_aplicada,
       (SELECT count(*) FROM plataforma.tbl_empresa_modulos em WHERE em.empresa_id = e.id AND em.es_activo) AS modulos_activos
FROM plataforma.tbl_empresas e
JOIN plataforma.tbl_empresa_conexiones c ON c.empresa_id = e.id
JOIN plataforma.tbl_empresa_esquema_version v ON v.empresa_id = e.id
WHERE e.identificador = 'guajiranet';
