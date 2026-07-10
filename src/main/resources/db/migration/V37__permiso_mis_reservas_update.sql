-- =============================================================================
-- V37__permiso_mis_reservas_update.sql
-- Añade el permiso 'mis-reservas:update' (editar acompañantes de una reserva
-- propia desde el panel del cliente) y lo asigna SOLO al rol CLIENTE.
--
-- Nota: 'mis-reservas' es un módulo exclusivo del panel del cliente (igual que
-- mis-servicios, servicios-catalogo y notificaciones). NO se asigna a ADMIN a
-- propósito: el contrato de permisos (ApiSmokeTest.endpointsDeClienteRechazanAdmin)
-- exige que ADMIN reciba 403 en estos endpoints. Por eso NO se replica el bloque
-- genérico "ADMIN recibe todos los permisos" que aparece en migraciones antiguas.
-- =============================================================================

-- 1. PERMISO
INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
  ('Editar mis reservas', 'mis-reservas:update', NOW())
ON CONFLICT (codigo) DO NOTHING;

-- 2. CLIENTE — único rol que recibe el permiso de edición
INSERT INTO detalles_rol (permiso_id, rol_id, fecha_creacion)
SELECT p.permiso_id, r.rol_id, NOW()
FROM permisos p
CROSS JOIN roles r
WHERE r.nombre = 'CLIENTE'
  AND p.codigo = 'mis-reservas:update'
ON CONFLICT (permiso_id, rol_id) DO NOTHING;
