-- =============================================================================
-- V41__permisos_turnos.sql
-- Permisos del módulo de turnos (planificación de personal). Asignados
-- explícitamente a los roles que gestionan personal: ADMIN y RRHH.
-- =============================================================================

INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
  ('Generar turnos',   'turnos:generar', NOW()),
  ('Ver turnos',       'turnos:read',    NOW()),
  ('Editar turnos',    'turnos:update',  NOW()),
  ('Cancelar turnos',  'turnos:delete',  NOW())
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO detalles_rol (permiso_id, rol_id, fecha_creacion)
SELECT p.permiso_id, r.rol_id, NOW()
FROM permisos p
CROSS JOIN roles r
WHERE p.codigo IN ('turnos:generar', 'turnos:read', 'turnos:update', 'turnos:delete')
  AND r.nombre IN ('ADMIN', 'RRHH')
ON CONFLICT (permiso_id, rol_id) DO NOTHING;
