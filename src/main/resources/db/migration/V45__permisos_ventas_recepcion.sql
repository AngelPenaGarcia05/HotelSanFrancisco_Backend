-- =============================================================================
-- V45__permisos_ventas_recepcion.sql
-- Recepción también realiza ventas de mostrador (POS): se le asignan los
-- permisos de crear y consultar ventas. No se otorgan update/delete/cambio de
-- estado, que siguen siendo de ADMIN.
-- =============================================================================

INSERT INTO detalles_rol (permiso_id, rol_id, fecha_creacion)
SELECT p.permiso_id, r.rol_id, NOW()
FROM permisos p
CROSS JOIN roles r
WHERE p.codigo IN ('venta:read', 'venta:create')
  AND r.nombre = 'RECEPCION'
ON CONFLICT (permiso_id, rol_id) DO NOTHING;
