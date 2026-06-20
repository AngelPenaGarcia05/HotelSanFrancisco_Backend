-- =============================================================================
-- V17__seed_cliente_permissions.sql
-- Los permisos del módulo "clientes" nunca fueron insertados en la tabla
-- permisos, por lo que ni el rol ADMIN podía ejecutar POST /api/v1/clientes.
-- Este script:
--   1. Inserta los 5 permisos del módulo clientes.
--   2. Asigna todos al rol ADMIN.
--   3. Asigna cliente:read y cliente:create al rol RECEPCION
--      (necesarios para buscar y registrar clientes durante el flujo de reservas).
-- =============================================================================

-- 1. PERMISOS — módulo clientes
INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
  ('Leer clientes',               'cliente:read',          NOW()),
  ('Crear clientes',              'cliente:create',        NOW()),
  ('Actualizar clientes',         'cliente:update',        NOW()),
  ('Eliminar clientes',           'cliente:delete',        NOW()),
  ('Cambiar estado de cliente',   'cliente:change-status', NOW())
ON CONFLICT (codigo) DO NOTHING;

SELECT setval('permisos_permiso_id_seq', COALESCE((SELECT MAX(permiso_id) FROM permisos), 1));

-- 2. ADMIN — recibe todos los permisos existentes (incluyendo los nuevos)
INSERT INTO detalles_rol (permiso_id, rol_id, fecha_creacion)
SELECT p.permiso_id, r.rol_id, NOW()
FROM permisos p
CROSS JOIN roles r
WHERE r.nombre = 'ADMIN'
ON CONFLICT (permiso_id, rol_id) DO NOTHING;

-- 3. RECEPCION — necesita leer y crear clientes para el flujo de reservas
INSERT INTO detalles_rol (permiso_id, rol_id, fecha_creacion)
SELECT p.permiso_id, r.rol_id, NOW()
FROM permisos p
CROSS JOIN roles r
WHERE r.nombre = 'RECEPCION'
  AND p.codigo IN ('cliente:read', 'cliente:create')
ON CONFLICT (permiso_id, rol_id) DO NOTHING;
