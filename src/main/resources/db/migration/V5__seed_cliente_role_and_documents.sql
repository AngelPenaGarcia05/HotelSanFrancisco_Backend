-- =============================================================================
-- V5__seed_cliente_role_and_documents.sql
-- Habilita registro público (huéspedes/clientes):
--   1. Tipos de documento adicionales (CE, PASAPORTE)
--   2. Rol CLIENTE — usuarios externos que se registran desde la web
-- =============================================================================

-- 1. Tipos de documento adicionales
INSERT INTO tipos_documento (acronimo, nombre, estado, fecha_creacion) VALUES
('CE',   'Carné de Extranjería', 'ACTIVO', NOW()),
('PASS', 'Pasaporte',            'ACTIVO', NOW())
ON CONFLICT DO NOTHING;

-- 2. Rol CLIENTE (huésped registrado vía web)
INSERT INTO roles (nombre, descripcion, estado, fecha_creacion) VALUES
('CLIENTE', 'Cliente / Huésped registrado vía portal web', 'ACTIVO', NOW())
ON CONFLICT DO NOTHING;

SELECT setval('roles_rol_id_seq', COALESCE((SELECT MAX(rol_id) FROM roles), 1));

-- 3. Permisos mínimos del rol CLIENTE
--    Por ahora puede consultar habitaciones (lectura) y crear sus propias reservas.
INSERT INTO detalles_rol (permiso_id, rol_id, fecha_creacion)
SELECT p.permiso_id, r.rol_id, NOW()
FROM permisos p
CROSS JOIN roles r
WHERE r.nombre = 'CLIENTE'
  AND p.codigo IN (
    'tipo-habitacion:read',
    'reserva:read',
    'reserva:create'
  )
ON CONFLICT (permiso_id, rol_id) DO NOTHING;
