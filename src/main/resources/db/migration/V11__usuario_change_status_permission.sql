-- =============================================================================
-- V11__usuario_change_status_permission.sql
-- Agrega el permiso usuario:change-status y lo asigna al rol ADMIN.
-- =============================================================================

INSERT INTO permisos (nombre, codigo, fecha_creacion)
VALUES ('Cambiar estado de usuario', 'usuario:change-status', NOW())
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO detalles_rol (rol_id, permiso_id, fecha_creacion)
SELECT r.rol_id, p.permiso_id, NOW()
FROM roles r
         JOIN permisos p ON p.codigo = 'usuario:change-status'
WHERE r.nombre = 'ADMIN'
ON CONFLICT DO NOTHING;
