-- =============================================================================
-- V14__create_solicitudes.sql
-- Módulo: Gestión de Solicitudes de Servicio
-- Tablas: solicitudes, seguimiento_solicitudes
-- Permisos RBAC: solicitud:{create,read,read-all,update,assign,change-status,report,delete}
-- =============================================================================

-- ─────────────────────────────────────────────────────────────────────────────
-- 1. TABLA solicitudes
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE solicitudes (
    solicitud_id       SERIAL          PRIMARY KEY,
    codigo_solicitud   VARCHAR(20)     NOT NULL,
    fecha_registro     TIMESTAMP       NOT NULL DEFAULT NOW(),
    tipo_solicitud     VARCHAR(20)     NOT NULL,
    asunto             VARCHAR(150)    NOT NULL,
    descripcion        TEXT            NOT NULL,
    prioridad          VARCHAR(10)     NOT NULL DEFAULT 'MEDIA',
    modulo_referido    VARCHAR(20),
    estado             VARCHAR(20)     NOT NULL DEFAULT 'REGISTRADA',
    observaciones      TEXT,
    fecha_cierre       TIMESTAMP,

    -- Campos específicos de solicitudes de ACCESO (nullable)
    rol_solicitado     VARCHAR(20),
    tipo_acceso        VARCHAR(30),
    periodo_inicio     DATE,
    periodo_fin        DATE,

    -- FKs
    id_usuario         INTEGER         NOT NULL,
    id_responsable     INTEGER,

    -- Auditoría técnica (AuditedEntity)
    fecha_creacion     TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_modificacion TIMESTAMP,

    CONSTRAINT uk_solicitudes_codigo UNIQUE (codigo_solicitud),
    CONSTRAINT fk_solicitudes_usuario
        FOREIGN KEY (id_usuario)     REFERENCES usuarios(usuario_id) ON DELETE RESTRICT,
    CONSTRAINT fk_solicitudes_responsable
        FOREIGN KEY (id_responsable) REFERENCES usuarios(usuario_id) ON DELETE SET NULL,
    CONSTRAINT chk_solicitudes_tipo       CHECK (tipo_solicitud IN ('INFORMACION','ACCESO')),
    CONSTRAINT chk_solicitudes_prioridad  CHECK (prioridad IN ('ALTA','MEDIA','BAJA')),
    CONSTRAINT chk_solicitudes_estado     CHECK (estado IN ('REGISTRADA','EN_EVALUACION','ATENDIDA','APROBADA','RECHAZADA','CERRADA')),
    CONSTRAINT chk_solicitudes_modulo     CHECK (modulo_referido IS NULL OR modulo_referido IN ('RESERVAS','HABITACIONES','PAGOS','EMPLEADOS','REPORTES','INVENTARIO','OTRO')),
    CONSTRAINT chk_solicitudes_tipo_acceso CHECK (tipo_acceso IS NULL OR tipo_acceso IN ('ACCESO_MODULO','CAMBIO_ROL','ACTIVACION','RECUPERACION'))
);

CREATE INDEX idx_solicitudes_estado        ON solicitudes(estado);
CREATE INDEX idx_solicitudes_tipo          ON solicitudes(tipo_solicitud);
CREATE INDEX idx_solicitudes_prioridad     ON solicitudes(prioridad);
CREATE INDEX idx_solicitudes_usuario       ON solicitudes(id_usuario);
CREATE INDEX idx_solicitudes_responsable   ON solicitudes(id_responsable);
CREATE INDEX idx_solicitudes_fecha_registro ON solicitudes(fecha_registro);

-- ─────────────────────────────────────────────────────────────────────────────
-- 2. TABLA seguimiento_solicitudes (historial inmutable de acciones)
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE seguimiento_solicitudes (
    seguimiento_id     SERIAL          PRIMARY KEY,
    id_solicitud       INTEGER         NOT NULL,
    fecha_accion       TIMESTAMP       NOT NULL DEFAULT NOW(),
    accion             VARCHAR(50)     NOT NULL,
    estado_anterior    VARCHAR(20),
    estado_nuevo       VARCHAR(20)     NOT NULL,
    observacion        TEXT,
    id_responsable     INTEGER         NOT NULL,

    -- Auditoría técnica (AuditedEntity)
    fecha_creacion     TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_modificacion TIMESTAMP,

    CONSTRAINT fk_seguimiento_solicitud
        FOREIGN KEY (id_solicitud)   REFERENCES solicitudes(solicitud_id) ON DELETE CASCADE,
    CONSTRAINT fk_seguimiento_responsable
        FOREIGN KEY (id_responsable) REFERENCES usuarios(usuario_id) ON DELETE RESTRICT,
    CONSTRAINT chk_seguimiento_accion CHECK (accion IN ('CREACION','ASIGNACION','CAMBIO_ESTADO','OBSERVACION','APROBACION','RECHAZO','CIERRE')),
    CONSTRAINT chk_seguimiento_estado_anterior CHECK (estado_anterior IS NULL OR estado_anterior IN ('REGISTRADA','EN_EVALUACION','ATENDIDA','APROBADA','RECHAZADA','CERRADA')),
    CONSTRAINT chk_seguimiento_estado_nuevo    CHECK (estado_nuevo IN ('REGISTRADA','EN_EVALUACION','ATENDIDA','APROBADA','RECHAZADA','CERRADA'))
);

CREATE INDEX idx_seguimiento_solicitud ON seguimiento_solicitudes(id_solicitud);
CREATE INDEX idx_seguimiento_fecha     ON seguimiento_solicitudes(fecha_accion);

-- ─────────────────────────────────────────────────────────────────────────────
-- 3. PERMISOS — módulo solicitudes
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
  ('Crear solicitudes',               'solicitud:create',        NOW()),
  ('Leer solicitudes propias',        'solicitud:read',          NOW()),
  ('Leer todas las solicitudes',      'solicitud:read-all',      NOW()),
  ('Actualizar solicitudes',          'solicitud:update',        NOW()),
  ('Asignar responsable de solicitud','solicitud:assign',        NOW()),
  ('Cambiar estado de solicitud',     'solicitud:change-status', NOW()),
  ('Generar reporte de solicitudes',  'solicitud:report',        NOW()),
  ('Eliminar solicitudes',            'solicitud:delete',        NOW())
ON CONFLICT (codigo) DO NOTHING;

SELECT setval('permisos_permiso_id_seq', COALESCE((SELECT MAX(permiso_id) FROM permisos), 1));

-- ─────────────────────────────────────────────────────────────────────────────
-- 4. ASIGNACIÓN — ADMIN recibe TODOS los permisos de solicitudes
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO detalles_rol (permiso_id, rol_id, fecha_creacion)
SELECT p.permiso_id, r.rol_id, NOW()
FROM permisos p
CROSS JOIN roles r
WHERE r.nombre = 'ADMIN'
  AND p.codigo LIKE 'solicitud:%'
ON CONFLICT (permiso_id, rol_id) DO NOTHING;

-- ─────────────────────────────────────────────────────────────────────────────
-- 5. ASIGNACIÓN — Todos los roles operativos pueden CREAR y LEER sus propias
--    solicitudes (canal universal de tickets). Incluye CLIENTE.
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO detalles_rol (permiso_id, rol_id, fecha_creacion)
SELECT p.permiso_id, r.rol_id, NOW()
FROM permisos p
CROSS JOIN roles r
WHERE r.nombre IN ('RECEPCION','CAJA','RRHH','INVENTARIO','CLIENTE')
  AND p.codigo IN ('solicitud:create','solicitud:read')
ON CONFLICT (permiso_id, rol_id) DO NOTHING;

-- ─────────────────────────────────────────────────────────────────────────────
-- 6. ASIGNACIÓN — RECEPCION además puede cambiar estado (solo solicitudes de
--    tipo INFORMACION; la restricción por tipo se valida en la capa de servicio)
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO detalles_rol (permiso_id, rol_id, fecha_creacion)
SELECT p.permiso_id, r.rol_id, NOW()
FROM permisos p
CROSS JOIN roles r
WHERE r.nombre = 'RECEPCION'
  AND p.codigo = 'solicitud:change-status'
ON CONFLICT (permiso_id, rol_id) DO NOTHING;
