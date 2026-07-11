-- =============================================================================
-- V39__permisos_mi_asistencia.sql
-- Permisos para el marcado self-service de asistencia (clock-in / clock-out).
-- El empleado marca su propia entrada/salida; el usuario se infiere del JWT.
-- Se asignan explícitamente a los roles de staff (no a CLIENTE).
-- =============================================================================

-- 1. PERMISOS
INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
  ('Marcar mi asistencia', 'mi-asistencia:marcar', NOW()),
  ('Ver mi asistencia',    'mi-asistencia:read',   NOW())
ON CONFLICT (codigo) DO NOTHING;

-- 2. ASIGNACIÓN EXPLÍCITA A ROLES DE STAFF (ADMIN, RECEPCION, CAJA, RRHH, INVENTARIO)
INSERT INTO detalles_rol (permiso_id, rol_id, fecha_creacion)
SELECT p.permiso_id, r.rol_id, NOW()
FROM permisos p
CROSS JOIN roles r
WHERE p.codigo IN ('mi-asistencia:marcar', 'mi-asistencia:read')
  AND r.nombre IN ('ADMIN', 'RECEPCION', 'CAJA', 'RRHH', 'INVENTARIO')
ON CONFLICT (permiso_id, rol_id) DO NOTHING;
