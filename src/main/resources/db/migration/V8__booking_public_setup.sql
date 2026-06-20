-- =============================================================================
-- V8__booking_public_setup.sql
-- Habilita el flujo público de reserva web:
--   1. Tipos de habitación estándar
--   2. Columna tipo_habitacion_id en habitaciones y asignación a filas existentes
--   3. Métodos de pago estándar del hotel
--   4. Canal "Web" para reservas del portal público
--   5. Usuario sistema para registrar reservas del canal web
-- =============================================================================

-- ─────────────────────────────────────────────────────────────────────────────
-- 1. TIPOS DE HABITACIÓN
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO tipos_habitacion (nombre, precio_base, descripcion, estado, capacidad_maxima, fecha_creacion)
SELECT 'Simple', 60.00, 'Habitación estándar con cama individual, ideal para viajeros individuales', 'ACTIVO', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM tipos_habitacion WHERE nombre = 'Simple');

INSERT INTO tipos_habitacion (nombre, precio_base, descripcion, estado, capacidad_maxima, fecha_creacion)
SELECT 'Matrimonial', 80.00, 'Habitación con cama matrimonial, perfecta para parejas o individuales', 'ACTIVO', 2, NOW()
WHERE NOT EXISTS (SELECT 1 FROM tipos_habitacion WHERE nombre = 'Matrimonial');

INSERT INTO tipos_habitacion (nombre, precio_base, descripcion, estado, capacidad_maxima, fecha_creacion)
SELECT 'Doble', 100.00, 'Habitación amplia con dos camas individuales, ideal para compartir', 'ACTIVO', 2, NOW()
WHERE NOT EXISTS (SELECT 1 FROM tipos_habitacion WHERE nombre = 'Doble');

INSERT INTO tipos_habitacion (nombre, precio_base, descripcion, estado, capacidad_maxima, fecha_creacion)
SELECT 'Suite Junior', 180.00, 'Suite con sala de estar independiente y escritorio ejecutivo', 'ACTIVO', 3, NOW()
WHERE NOT EXISTS (SELECT 1 FROM tipos_habitacion WHERE nombre = 'Suite Junior');

INSERT INTO tipos_habitacion (nombre, precio_base, descripcion, estado, capacidad_maxima, fecha_creacion)
SELECT 'Suite Ejecutiva', 250.00, 'Suite premium con jacuzzi, terraza privada y vista panorámica', 'ACTIVO', 4, NOW()
WHERE NOT EXISTS (SELECT 1 FROM tipos_habitacion WHERE nombre = 'Suite Ejecutiva');

SELECT setval('tipos_habitacion_tipo_habitacion_id_seq', COALESCE((SELECT MAX(tipo_habitacion_id) FROM tipos_habitacion), 1));

-- ─────────────────────────────────────────────────────────────────────────────
-- 2. COLUMNA tipo_habitacion_id EN habitaciones
-- ─────────────────────────────────────────────────────────────────────────────
ALTER TABLE habitaciones
    ADD COLUMN IF NOT EXISTS tipo_habitacion_id INTEGER
    REFERENCES tipos_habitacion(tipo_habitacion_id) ON DELETE SET NULL;

-- Asignar tipos a las habitaciones sembradas en V7
UPDATE habitaciones SET tipo_habitacion_id =
    (SELECT tipo_habitacion_id FROM tipos_habitacion WHERE nombre = 'Simple' LIMIT 1)
WHERE numero IN ('101', '102', '105', '201', '202');

UPDATE habitaciones SET tipo_habitacion_id =
    (SELECT tipo_habitacion_id FROM tipos_habitacion WHERE nombre = 'Matrimonial' LIMIT 1)
WHERE numero IN ('103', '204');

UPDATE habitaciones SET tipo_habitacion_id =
    (SELECT tipo_habitacion_id FROM tipos_habitacion WHERE nombre = 'Doble' LIMIT 1)
WHERE numero IN ('104', '205', '304');

UPDATE habitaciones SET tipo_habitacion_id =
    (SELECT tipo_habitacion_id FROM tipos_habitacion WHERE nombre = 'Suite Junior' LIMIT 1)
WHERE numero IN ('203');

UPDATE habitaciones SET tipo_habitacion_id =
    (SELECT tipo_habitacion_id FROM tipos_habitacion WHERE nombre = 'Suite Ejecutiva' LIMIT 1)
WHERE numero IN ('301', '302', '303');

-- ─────────────────────────────────────────────────────────────────────────────
-- 3. MÉTODOS DE PAGO
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO metodos_pago (nombre, estado, requiere_comprobante, fecha_creacion)
SELECT 'Tarjeta de crédito / débito', 'ACTIVO', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM metodos_pago WHERE nombre = 'Tarjeta de crédito / débito');

INSERT INTO metodos_pago (nombre, estado, requiere_comprobante, fecha_creacion)
SELECT 'Yape / Plin', 'ACTIVO', false, NOW()
WHERE NOT EXISTS (SELECT 1 FROM metodos_pago WHERE nombre = 'Yape / Plin');

INSERT INTO metodos_pago (nombre, estado, requiere_comprobante, fecha_creacion)
SELECT 'Transferencia bancaria', 'ACTIVO', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM metodos_pago WHERE nombre = 'Transferencia bancaria');

INSERT INTO metodos_pago (nombre, estado, requiere_comprobante, fecha_creacion)
SELECT 'Pago en recepción', 'ACTIVO', false, NOW()
WHERE NOT EXISTS (SELECT 1 FROM metodos_pago WHERE nombre = 'Pago en recepción');

SELECT setval('metodos_pago_metodo_pago_id_seq', COALESCE((SELECT MAX(metodo_pago_id) FROM metodos_pago), 1));

-- ─────────────────────────────────────────────────────────────────────────────
-- 4. CANAL WEB
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO canales (nombre, estado, fecha_creacion)
SELECT 'Web', 'ACTIVO', NOW()
WHERE NOT EXISTS (SELECT 1 FROM canales WHERE nombre = 'Web');

SELECT setval('canales_canal_id_seq', COALESCE((SELECT MAX(canal_id) FROM canales), 1));

-- ─────────────────────────────────────────────────────────────────────────────
-- 5. USUARIO SISTEMA WEB (para registrar reservas del canal público)
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO usuarios (nombre, apellido_paterno, numero_documento, correo,
                      contrasena_hash, estado, rol_id, tipo_documento_id, fecha_creacion)
SELECT
    'Sistema', 'Web', 'SIS-WEB-001', 'sistema.web@hotel-sf.com',
    '$2a$12$XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX',
    'ACTIVO',
    (SELECT rol_id FROM roles WHERE nombre = 'ADMIN' LIMIT 1),
    (SELECT tipo_documento_id FROM tipos_documento LIMIT 1),
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE correo = 'sistema.web@hotel-sf.com');

SELECT setval('usuarios_usuario_id_seq', COALESCE((SELECT MAX(usuario_id) FROM usuarios), 1));
