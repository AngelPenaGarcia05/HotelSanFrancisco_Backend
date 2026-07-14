-- =============================================================================
-- V42__permiso_reporte_read.sql
-- Permiso dedicado para el módulo de reportes.
-- =============================================================================
-- Hasta ahora /api/v1/reportes/** se gateaba con pago:read, lo que daba a
-- RECEPCION acceso al reporte gerencial y a las exportaciones financieras.
-- Se crea reporte:read y se asigna solo a ADMIN y CAJA. RECEPCION conserva
-- su operación (reservas, check-in/out, pagos) pero deja de ver reportes.
-- =============================================================================

-- 1. Permiso
INSERT INTO permisos (nombre, codigo, fecha_creacion)
VALUES ('Consultar reportes', 'reporte:read', NOW())
ON CONFLICT (codigo) DO NOTHING;

-- 2. Asignación a ADMIN y CAJA
INSERT INTO detalles_rol (permiso_id, rol_id, fecha_creacion)
SELECT p.permiso_id, r.rol_id, NOW()
FROM permisos p
CROSS JOIN roles r
WHERE p.codigo = 'reporte:read'
  AND r.nombre IN ('ADMIN', 'CAJA')
ON CONFLICT (permiso_id, rol_id) DO NOTHING;
