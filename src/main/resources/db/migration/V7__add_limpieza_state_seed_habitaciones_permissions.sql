-- =============================================================================
-- V7__add_limpieza_state_seed_habitaciones_permissions.sql
-- 1. Agrega estado LIMPIEZA al CHECK constraint de habitaciones
-- 2. Inserta datos semilla de habitaciones
-- 3. Agrega permisos del módulo habitaciones y los asigna a roles
-- =============================================================================

-- ─────────────────────────────────────────────────────────────────────────────
-- 1. CHECK CONSTRAINT — agregar LIMPIEZA como estado válido
-- ─────────────────────────────────────────────────────────────────────────────
ALTER TABLE habitaciones DROP CONSTRAINT IF EXISTS chk_habitaciones_estado;
ALTER TABLE habitaciones ADD CONSTRAINT chk_habitaciones_estado
    CHECK (estado IN ('DISPONIBLE','OCUPADA','LIMPIEZA','MANTENIMIENTO','BLOQUEADA'));

-- ─────────────────────────────────────────────────────────────────────────────
-- 2. HABITACIONES SEMILLA — 3 pisos, tipos representativos
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO habitaciones (numero, piso, estado, descripcion, fecha_creacion)
VALUES
  -- Piso 1
  ('101', 1, 'DISPONIBLE', 'Habitación estándar, vista al jardín',          NOW()),
  ('102', 1, 'DISPONIBLE', 'Habitación estándar, vista al jardín',          NOW()),
  ('103', 1, 'DISPONIBLE', 'Habitación doble, cama matrimonial',            NOW()),
  ('104', 1, 'DISPONIBLE', 'Habitación doble, camas twin',                  NOW()),
  ('105', 1, 'MANTENIMIENTO', 'Habitación en reparación de plomería',       NOW()),
  -- Piso 2
  ('201', 2, 'DISPONIBLE', 'Habitación superior, vista al atrio',           NOW()),
  ('202', 2, 'DISPONIBLE', 'Habitación superior, vista al atrio',           NOW()),
  ('203', 2, 'DISPONIBLE', 'Suite junior, sala de estar',                   NOW()),
  ('204', 2, 'OCUPADA',    'Habitación en uso — Check-in activo',           NOW()),
  ('205', 2, 'LIMPIEZA',   'En proceso de limpieza post check-out',         NOW()),
  -- Piso 3
  ('301', 3, 'DISPONIBLE', 'Suite ejecutiva, escritorio y sala',            NOW()),
  ('302', 3, 'DISPONIBLE', 'Suite ejecutiva, escritorio y sala',            NOW()),
  ('303', 3, 'DISPONIBLE', 'Suite presidencial, jacuzzi y terraza',         NOW()),
  ('304', 3, 'BLOQUEADA',  'Bloqueada por refacción de temporada',          NOW())
ON CONFLICT (numero) DO NOTHING;

SELECT setval('habitaciones_habitacion_id_seq', COALESCE((SELECT MAX(habitacion_id) FROM habitaciones), 1));

-- ─────────────────────────────────────────────────────────────────────────────
-- 3. PERMISOS — módulo habitación
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
  ('Leer habitaciones',              'habitacion:read',          NOW()),
  ('Crear habitaciones',             'habitacion:create',        NOW()),
  ('Actualizar habitaciones',        'habitacion:update',        NOW()),
  ('Eliminar habitaciones',          'habitacion:delete',        NOW()),
  ('Cambiar estado de habitación',   'habitacion:change-status', NOW()),
  ('Ejecutar check-in',              'habitacion:checkin',       NOW()),
  ('Ejecutar check-out',             'habitacion:checkout',      NOW()),
  ('Registrar limpieza completada',  'habitacion:limpieza',      NOW())
ON CONFLICT (codigo) DO NOTHING;

SELECT setval('permisos_permiso_id_seq', COALESCE((SELECT MAX(permiso_id) FROM permisos), 1));

-- ─────────────────────────────────────────────────────────────────────────────
-- 4. ASIGNACIÓN — ADMIN recibe todos los permisos nuevos automáticamente
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO detalles_rol (permiso_id, rol_id, fecha_creacion)
SELECT p.permiso_id, r.rol_id, NOW()
FROM permisos p
CROSS JOIN roles r
WHERE r.nombre = 'ADMIN'
ON CONFLICT (permiso_id, rol_id) DO NOTHING;

-- ─────────────────────────────────────────────────────────────────────────────
-- 5. ASIGNACIÓN — RECEPCION: puede leer, hacer check-in y check-out
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO detalles_rol (permiso_id, rol_id, fecha_creacion)
SELECT p.permiso_id, r.rol_id, NOW()
FROM permisos p
CROSS JOIN roles r
WHERE r.nombre = 'RECEPCION'
  AND p.codigo IN ('habitacion:read','habitacion:checkin','habitacion:checkout','habitacion:change-status')
ON CONFLICT (permiso_id, rol_id) DO NOTHING;

-- ─────────────────────────────────────────────────────────────────────────────
-- 6. ASIGNACIÓN — RRHH: solo lectura de habitaciones (reportes)
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO detalles_rol (permiso_id, rol_id, fecha_creacion)
SELECT p.permiso_id, r.rol_id, NOW()
FROM permisos p
CROSS JOIN roles r
WHERE r.nombre = 'RRHH'
  AND p.codigo IN ('habitacion:read','habitacion:limpieza')
ON CONFLICT (permiso_id, rol_id) DO NOTHING;
