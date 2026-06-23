-- =============================================================================
-- V23__permisos_servicio_catalogo_cliente.sql
-- Permiso para que el CLIENTE consulte el catálogo de servicios del hotel.
-- El endpoint admin /api/v1/tipos-servicio sigue exigiendo 'tipo-servicio:read'
-- (CLIENTE no lo tiene). El catálogo solo expone los servicios ACTIVO.
-- =============================================================================

INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
('Leer catálogo de servicios', 'servicio-catalogo:read', NOW());

SELECT setval('permisos_permiso_id_seq', COALESCE((SELECT MAX(permiso_id) FROM permisos), 1));

INSERT INTO detalles_rol (permiso_id, rol_id, fecha_creacion)
SELECT p.permiso_id, r.rol_id, NOW()
FROM permisos p
CROSS JOIN roles r
WHERE r.nombre = 'CLIENTE'
  AND p.codigo = 'servicio-catalogo:read'
ON CONFLICT (permiso_id, rol_id) DO NOTHING;
