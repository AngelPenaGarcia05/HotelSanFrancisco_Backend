-- =============================================================================
-- V37__permiso_mis_reservas_update.sql
-- Añade el permiso 'mis-reservas:update' (editar acompañantes de una reserva
-- propia desde el panel del cliente) y lo asigna al rol CLIENTE. El rol ADMIN
-- lo recibe por el INSERT genérico de todos los permisos.
-- =============================================================================

-- 1. PERMISO
INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
  ('Editar mis reservas', 'mis-reservas:update', NOW())
ON CONFLICT (codigo) DO NOTHING;

-- 2. ADMIN — recibe todos los permisos (incluyendo el nuevo)
INSERT INTO detalles_rol (permiso_id, rol_id, fecha_creacion)
SELECT p.permiso_id, r.rol_id, NOW()
FROM permisos p
CROSS JOIN roles r
WHERE r.nombre = 'ADMIN'
ON CONFLICT (permiso_id, rol_id) DO NOTHING;

-- 3. CLIENTE — recibe el permiso de edición
INSERT INTO detalles_rol (permiso_id, rol_id, fecha_creacion)
SELECT p.permiso_id, r.rol_id, NOW()
FROM permisos p
CROSS JOIN roles r
WHERE r.nombre = 'CLIENTE'
  AND p.codigo = 'mis-reservas:update'
ON CONFLICT (permiso_id, rol_id) DO NOTHING;
