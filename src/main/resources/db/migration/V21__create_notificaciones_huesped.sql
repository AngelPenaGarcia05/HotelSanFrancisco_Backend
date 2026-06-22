-- =============================================================================
-- V21__create_notificaciones_huesped.sql
-- Notificaciones in-app para el cliente (rol CLIENTE).
-- Se generan automáticamente al ocurrir eventos sobre las reservas del cliente.
-- =============================================================================

CREATE TABLE notificaciones_huesped (
    notificacion_id     SERIAL          PRIMARY KEY,
    usuario_id           INTEGER         NOT NULL,
    tipo                 VARCHAR(20)     NOT NULL,
    titulo               VARCHAR(150)    NOT NULL,
    mensaje              VARCHAR(500)    NOT NULL,
    leida                BOOLEAN         NOT NULL DEFAULT FALSE,
    referencia_id        INTEGER,
    fecha_creacion       TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_modificacion   TIMESTAMP,

    CONSTRAINT fk_notif_huesped_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(usuario_id),
    CONSTRAINT chk_notif_huesped_tipo CHECK (tipo IN (
        'CHECK_IN','CHECK_OUT','PAGO','SERVICIO','CONFIRMACION','FACTURA'
    ))
);

CREATE INDEX idx_notif_huesped_usuario ON notificaciones_huesped(usuario_id, fecha_creacion DESC);

-- =============================================================================
-- Permisos del cliente para su bandeja de notificaciones
-- =============================================================================
INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
('Leer notificaciones propias',         'notificacion-cliente:read',   NOW()),
('Actualizar notificaciones propias',   'notificacion-cliente:update', NOW());

SELECT setval('permisos_permiso_id_seq', COALESCE((SELECT MAX(permiso_id) FROM permisos), 1));

-- Asignar al rol CLIENTE
INSERT INTO detalles_rol (permiso_id, rol_id, fecha_creacion)
SELECT p.permiso_id, r.rol_id, NOW()
FROM permisos p
CROSS JOIN roles r
WHERE r.nombre = 'CLIENTE'
  AND p.codigo IN ('notificacion-cliente:read', 'notificacion-cliente:update')
ON CONFLICT (permiso_id, rol_id) DO NOTHING;
