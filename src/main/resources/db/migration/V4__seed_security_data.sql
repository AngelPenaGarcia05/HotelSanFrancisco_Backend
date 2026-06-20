-- =============================================================================
-- V4__seed_security_data.sql
-- Datos semilla RBAC Enterprise — Sistema Hotelero San Francisco
-- =============================================================================
-- Arquitectura de permisos: modulo:accion
-- Roles empresariales: ADMIN, RECEPCION, CAJA, RRHH, INVENTARIO
-- =============================================================================

-- 1. Insertar Tipos de Documento
INSERT INTO tipos_documento (tipo_documento_id, acronimo, nombre, estado, fecha_creacion)
VALUES (1, 'DNI', 'Documento Nacional de Identidad', 'ACTIVO', NOW())
ON CONFLICT (tipo_documento_id) DO NOTHING;

SELECT setval('tipos_documento_tipo_documento_id_seq', COALESCE((SELECT MAX(tipo_documento_id) FROM tipos_documento), 1));

-- =============================================================================
-- 2. PERMISOS — Nomenclatura modulo:accion
-- =============================================================================

-- =========================
-- RESERVAS
-- =========================
INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
('Leer reservas',                'reserva:read',          NOW()),
('Crear reservas',               'reserva:create',        NOW()),
('Actualizar reservas',          'reserva:update',        NOW()),
('Eliminar reservas',            'reserva:delete',        NOW()),
('Cambiar estado de reserva',    'reserva:change-status', NOW());

-- =========================
-- TIPO HABITACIÓN
-- =========================
INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
('Leer tipos de habitación',     'tipo-habitacion:read',   NOW()),
('Crear tipos de habitación',    'tipo-habitacion:create', NOW()),
('Actualizar tipos de habitación','tipo-habitacion:update', NOW()),
('Eliminar tipos de habitación', 'tipo-habitacion:delete', NOW());

-- =========================
-- PAGOS
-- =========================
INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
('Leer pagos',                   'pago:read',   NOW()),
('Crear pagos',                  'pago:create', NOW()),
('Actualizar pagos',             'pago:update', NOW()),
('Eliminar pagos',               'pago:delete', NOW());

-- =========================
-- MÉTODOS DE PAGO
-- =========================
INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
('Leer métodos de pago',         'metodo-pago:read',   NOW()),
('Crear métodos de pago',        'metodo-pago:create', NOW()),
('Actualizar métodos de pago',   'metodo-pago:update', NOW()),
('Eliminar métodos de pago',     'metodo-pago:delete', NOW());

-- =========================
-- VENTAS
-- =========================
INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
('Leer ventas',                  'venta:read',          NOW()),
('Crear ventas',                 'venta:create',        NOW()),
('Actualizar ventas',            'venta:update',        NOW()),
('Eliminar ventas',              'venta:delete',        NOW()),
('Cambiar estado de venta',      'venta:change-status', NOW());

-- =========================
-- PRODUCTOS
-- =========================
INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
('Leer productos',               'producto:read',         NOW()),
('Crear productos',              'producto:create',       NOW()),
('Actualizar productos',         'producto:update',       NOW()),
('Eliminar productos',           'producto:delete',       NOW()),
('Ajustar stock de productos',   'producto:adjust-stock', NOW());

-- =========================
-- CATEGORÍAS DE PRODUCTO
-- =========================
INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
('Leer categorías de producto',     'categoria-producto:read',   NOW()),
('Crear categorías de producto',    'categoria-producto:create', NOW()),
('Actualizar categorías de producto','categoria-producto:update', NOW()),
('Eliminar categorías de producto', 'categoria-producto:delete', NOW());

-- =========================
-- COMPRAS
-- =========================
INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
('Leer compras',                 'compra:read',          NOW()),
('Crear compras',                'compra:create',        NOW()),
('Actualizar compras',           'compra:update',        NOW()),
('Eliminar compras',             'compra:delete',        NOW()),
('Cambiar estado de compra',     'compra:change-status', NOW());

-- =========================
-- PROVEEDORES
-- =========================
INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
('Leer proveedores',             'proveedor:read',   NOW()),
('Crear proveedores',            'proveedor:create', NOW()),
('Actualizar proveedores',       'proveedor:update', NOW()),
('Eliminar proveedores',         'proveedor:delete', NOW());

-- =========================
-- SERVICIOS
-- =========================
INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
('Leer servicios',               'servicio:read',   NOW()),
('Crear servicios',              'servicio:create', NOW()),
('Actualizar servicios',         'servicio:update', NOW()),
('Eliminar servicios',           'servicio:delete', NOW());

-- =========================
-- TIPOS DE SERVICIO
-- =========================
INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
('Leer tipos de servicio',       'tipo-servicio:read',   NOW()),
('Crear tipos de servicio',      'tipo-servicio:create', NOW()),
('Actualizar tipos de servicio', 'tipo-servicio:update', NOW()),
('Eliminar tipos de servicio',   'tipo-servicio:delete', NOW());

-- =========================
-- INCIDENCIAS
-- =========================
INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
('Leer incidencias',             'incidencia:read',          NOW()),
('Crear incidencias',            'incidencia:create',        NOW()),
('Actualizar incidencias',       'incidencia:update',        NOW()),
('Eliminar incidencias',         'incidencia:delete',        NOW()),
('Cambiar estado de incidencia', 'incidencia:change-status', NOW());

-- =========================
-- USUARIOS
-- =========================
INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
('Leer usuarios',                'usuario:read',   NOW()),
('Crear usuarios',               'usuario:create', NOW()),
('Actualizar usuarios',          'usuario:update', NOW()),
('Eliminar usuarios',            'usuario:delete', NOW());

-- =========================
-- ROLES
-- =========================
INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
('Leer roles',                   'rol:read',   NOW()),
('Crear roles',                  'rol:create', NOW()),
('Actualizar roles',             'rol:update', NOW()),
('Eliminar roles',               'rol:delete', NOW());

-- =========================
-- PERMISOS (consulta)
-- =========================
INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
('Leer permisos',                'permiso:read', NOW());

-- =========================
-- TIPOS DE DOCUMENTO
-- =========================
INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
('Leer tipos de documento',      'tipo-documento:read',   NOW()),
('Crear tipos de documento',     'tipo-documento:create', NOW()),
('Actualizar tipos de documento','tipo-documento:update', NOW()),
('Eliminar tipos de documento',  'tipo-documento:delete', NOW());

-- =========================
-- HORARIOS
-- =========================
INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
('Leer horarios',                'horario:read',   NOW()),
('Crear horarios',               'horario:create', NOW()),
('Actualizar horarios',          'horario:update', NOW()),
('Eliminar horarios',            'horario:delete', NOW());

-- =========================
-- ASISTENCIA
-- =========================
INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
('Leer asistencias',             'asistencia:read',   NOW()),
('Crear asistencias',            'asistencia:create', NOW()),
('Actualizar asistencias',       'asistencia:update', NOW()),
('Eliminar asistencias',         'asistencia:delete', NOW());

-- =========================
-- NÓMINA (PAGOS DE NÓMINA)
-- =========================
INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
('Leer pagos de nómina',         'nomina:read',          NOW()),
('Crear pagos de nómina',        'nomina:create',        NOW()),
('Eliminar pagos de nómina',     'nomina:delete',        NOW()),
('Cambiar estado de nómina',     'nomina:change-status', NOW());

-- =========================
-- BONOS
-- =========================
INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
('Leer bonos',                   'bono:read',   NOW()),
('Crear bonos',                  'bono:create', NOW()),
('Actualizar bonos',             'bono:update', NOW()),
('Eliminar bonos',               'bono:delete', NOW());

-- =========================
-- ASIGNACIÓN DE HORARIOS
-- =========================
INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
('Leer asignaciones de horario',     'asignacion-horario:read',   NOW()),
('Crear asignaciones de horario',    'asignacion-horario:create', NOW()),
('Actualizar asignaciones de horario','asignacion-horario:update', NOW()),
('Eliminar asignaciones de horario', 'asignacion-horario:delete', NOW());

-- Ajustar secuencia de permisos
SELECT setval('permisos_permiso_id_seq', COALESCE((SELECT MAX(permiso_id) FROM permisos), 1));


-- =============================================================================
-- 3. ROLES EMPRESARIALES
-- =============================================================================
INSERT INTO roles (rol_id, nombre, descripcion, estado, fecha_creacion) VALUES
(1, 'ADMIN',      'Administrador General — Acceso total al sistema',                                      'ACTIVO', NOW()),
(2, 'RECEPCION',  'Recepción — Gestión de reservas, huéspedes, check-in/out y servicios',                 'ACTIVO', NOW()),
(3, 'CAJA',       'Caja — Cobros, pagos, ventas y métodos de pago',                                       'ACTIVO', NOW()),
(4, 'RRHH',       'Recursos Humanos — Gestión de personal, horarios, asistencia, nóminas y bonos',        'ACTIVO', NOW()),
(5, 'INVENTARIO', 'Inventario — Gestión de productos, categorías, compras y proveedores',                  'ACTIVO', NOW())
ON CONFLICT (rol_id) DO UPDATE SET
    nombre      = EXCLUDED.nombre,
    descripcion = EXCLUDED.descripcion,
    estado      = EXCLUDED.estado;

SELECT setval('roles_rol_id_seq', COALESCE((SELECT MAX(rol_id) FROM roles), 1));


-- =============================================================================
-- 4. ASIGNACIÓN ROL ↔ PERMISO
-- =============================================================================

-- ─────────────────────────────────────────────────────────────────────────────
-- ADMIN — Todos los permisos del sistema
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO detalles_rol (permiso_id, rol_id, fecha_creacion)
SELECT p.permiso_id, r.rol_id, NOW()
FROM permisos p
CROSS JOIN roles r
WHERE r.nombre = 'ADMIN'
ON CONFLICT (permiso_id, rol_id) DO NOTHING;

-- ─────────────────────────────────────────────────────────────────────────────
-- RECEPCION — Reservas, Habitaciones, Pagos (lectura/creación), Servicios, Incidencias
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO detalles_rol (permiso_id, rol_id, fecha_creacion)
SELECT p.permiso_id, r.rol_id, NOW()
FROM permisos p
CROSS JOIN roles r
WHERE r.nombre = 'RECEPCION'
  AND p.codigo IN (
    -- Reservas (acceso completo)
    'reserva:read', 'reserva:create', 'reserva:update', 'reserva:delete', 'reserva:change-status',
    -- Tipo Habitación (solo lectura)
    'tipo-habitacion:read',
    -- Pagos (lectura y creación)
    'pago:read', 'pago:create',
    -- Métodos de pago (solo lectura)
    'metodo-pago:read',
    -- Ventas (solo lectura)
    'venta:read',
    -- Servicios (lectura y creación)
    'servicio:read', 'servicio:create',
    -- Tipos de servicio (solo lectura)
    'tipo-servicio:read',
    -- Incidencias (lectura y creación)
    'incidencia:read', 'incidencia:create'
)
ON CONFLICT (permiso_id, rol_id) DO NOTHING;

-- ─────────────────────────────────────────────────────────────────────────────
-- CAJA — Pagos, Ventas, Reservas (lectura), Productos (lectura)
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO detalles_rol (permiso_id, rol_id, fecha_creacion)
SELECT p.permiso_id, r.rol_id, NOW()
FROM permisos p
CROSS JOIN roles r
WHERE r.nombre = 'CAJA'
  AND p.codigo IN (
    -- Pagos (acceso completo)
    'pago:read', 'pago:create', 'pago:update', 'pago:delete',
    -- Métodos de pago (solo lectura)
    'metodo-pago:read',
    -- Ventas (acceso completo)
    'venta:read', 'venta:create', 'venta:update', 'venta:delete', 'venta:change-status',
    -- Reservas (solo lectura para cobrar)
    'reserva:read',
    -- Productos (solo lectura para vender)
    'producto:read'
)
ON CONFLICT (permiso_id, rol_id) DO NOTHING;

-- ─────────────────────────────────────────────────────────────────────────────
-- RRHH — Personal, Horarios, Asistencia, Nóminas, Bonos
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO detalles_rol (permiso_id, rol_id, fecha_creacion)
SELECT p.permiso_id, r.rol_id, NOW()
FROM permisos p
CROSS JOIN roles r
WHERE r.nombre = 'RRHH'
  AND p.codigo IN (
    -- Usuarios (acceso completo)
    'usuario:read', 'usuario:create', 'usuario:update', 'usuario:delete',
    -- Roles (solo lectura)
    'rol:read',
    -- Permisos (solo lectura)
    'permiso:read',
    -- Tipos de documento (acceso completo)
    'tipo-documento:read', 'tipo-documento:create', 'tipo-documento:update', 'tipo-documento:delete',
    -- Horarios (acceso completo)
    'horario:read', 'horario:create', 'horario:update', 'horario:delete',
    -- Asistencia (acceso completo)
    'asistencia:read', 'asistencia:create', 'asistencia:update', 'asistencia:delete',
    -- Nóminas (acceso completo)
    'nomina:read', 'nomina:create', 'nomina:delete', 'nomina:change-status',
    -- Bonos (acceso completo)
    'bono:read', 'bono:create', 'bono:update', 'bono:delete',
    -- Asignación de horarios (acceso completo)
    'asignacion-horario:read', 'asignacion-horario:create', 'asignacion-horario:update', 'asignacion-horario:delete'
)
ON CONFLICT (permiso_id, rol_id) DO NOTHING;

-- ─────────────────────────────────────────────────────────────────────────────
-- INVENTARIO — Productos, Categorías, Compras, Proveedores
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO detalles_rol (permiso_id, rol_id, fecha_creacion)
SELECT p.permiso_id, r.rol_id, NOW()
FROM permisos p
CROSS JOIN roles r
WHERE r.nombre = 'INVENTARIO'
  AND p.codigo IN (
    -- Productos (acceso completo)
    'producto:read', 'producto:create', 'producto:update', 'producto:delete', 'producto:adjust-stock',
    -- Categorías de producto (acceso completo)
    'categoria-producto:read', 'categoria-producto:create', 'categoria-producto:update', 'categoria-producto:delete',
    -- Compras (acceso completo)
    'compra:read', 'compra:create', 'compra:update', 'compra:delete', 'compra:change-status',
    -- Proveedores (acceso completo)
    'proveedor:read', 'proveedor:create', 'proveedor:update', 'proveedor:delete'
)
ON CONFLICT (permiso_id, rol_id) DO NOTHING;


-- =============================================================================
-- 5. USUARIO ADMINISTRADOR POR DEFECTO
-- =============================================================================
-- Contraseña: admin123 (BCrypt hash)
INSERT INTO usuarios (usuario_id, nombre, apellido_paterno, apellido_materno, numero_documento, correo, telefono, fecha_nacimiento, contrasena_hash, estado, fecha_creacion, rol_id, tipo_documento_id)
VALUES (
    1,
    'Angel',
    'Pena',
    'Garcia',
    '12345678',
    'admin@sanfrancisco.com',
    '987654321',
    '1990-01-01',
    '$2a$10$gR5xG29Geqs1Gf9iV1/DCO8EpyqgYmZ54w2xT1gM6hK/P81aLwV3G', -- BCrypt hash de 'admin123'
    'ACTIVO',
    NOW(),
    1, -- ADMIN
    1  -- DNI
)
ON CONFLICT (usuario_id) DO NOTHING;

SELECT setval('usuarios_usuario_id_seq', COALESCE((SELECT MAX(usuario_id) FROM usuarios), 1));
