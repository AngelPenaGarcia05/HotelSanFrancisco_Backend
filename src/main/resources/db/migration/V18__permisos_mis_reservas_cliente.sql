-- =============================================================================
-- V18__permisos_mis_reservas_cliente.sql
-- Crea los permisos del módulo "Mis Reservas" (panel del cliente) y los
-- asigna al rol CLIENTE. El rol ADMIN los recibe automáticamente por el
-- INSERT genérico de todos los permisos.
-- =============================================================================

-- 1. PERMISOS — módulo mis-reservas
INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
  ('Ver mis reservas',      'mis-reservas:read',   NOW()),
  ('Crear mis reservas',    'mis-reservas:create', NOW()),
  ('Cancelar mis reservas', 'mis-reservas:delete', NOW())
ON CONFLICT (codigo) DO NOTHING;

-- 2. ADMIN — recibe todos los permisos (incluyendo los nuevos)
INSERT INTO detalles_rol (permiso_id, rol_id, fecha_creacion)
SELECT p.permiso_id, r.rol_id, NOW()
FROM permisos p
CROSS JOIN roles r
WHERE r.nombre = 'ADMIN'
ON CONFLICT (permiso_id, rol_id) DO NOTHING;

-- 3. CLIENTE — recibe los tres permisos de mis-reservas
INSERT INTO detalles_rol (permiso_id, rol_id, fecha_creacion)
SELECT p.permiso_id, r.rol_id, NOW()
FROM permisos p
CROSS JOIN roles r
WHERE r.nombre = 'CLIENTE'
  AND p.codigo IN ('mis-reservas:read', 'mis-reservas:create', 'mis-reservas:delete')
ON CONFLICT (permiso_id, rol_id) DO NOTHING;
