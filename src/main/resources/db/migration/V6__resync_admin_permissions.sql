-- =============================================================================
-- V6__resync_admin_permissions.sql
-- Garantiza que el rol ADMIN tenga TODOS los permisos del sistema.
-- Idempotente: ON CONFLICT DO NOTHING no duplica filas existentes.
-- Necesario si V4 corrió parcialmente o en orden incorrecto.
-- =============================================================================

INSERT INTO detalles_rol (permiso_id, rol_id, fecha_creacion)
SELECT p.permiso_id, r.rol_id, NOW()
FROM permisos p
CROSS JOIN roles r
WHERE r.nombre = 'ADMIN'
ON CONFLICT (permiso_id, rol_id) DO NOTHING;
