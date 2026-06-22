-- =============================================================================
-- V22__permisos_mis_pagos_cliente.sql
-- Permiso para que el CLIENTE consulte sus propios pagos y descargue sus facturas.
-- Los endpoints admin de pagos siguen exigiendo 'pago:read' (CLIENTE no lo tiene).
-- =============================================================================

INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
('Leer pagos propios', 'mis-pagos:read', NOW());

SELECT setval('permisos_permiso_id_seq', COALESCE((SELECT MAX(permiso_id) FROM permisos), 1));

INSERT INTO detalles_rol (permiso_id, rol_id, fecha_creacion)
SELECT p.permiso_id, r.rol_id, NOW()
FROM permisos p
CROSS JOIN roles r
WHERE r.nombre = 'CLIENTE'
  AND p.codigo = 'mis-pagos:read'
ON CONFLICT (permiso_id, rol_id) DO NOTHING;
