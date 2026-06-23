-- =============================================================================
-- V24__create_pedidos_servicio.sql
-- Pedidos de servicio del cliente (panel del huésped).
--   - Flujo: PENDIENTE -> APROBADO (genera consumo en 'servicios') | RECHAZADO
--            PENDIENTE -> CANCELADO (por el propio cliente)
--   - Solo se piden durante una estadía activa (estancia con check-in, sin check-out).
--   - Permisos del cliente: mis-servicios:read, mis-servicios:create (rol CLIENTE).
--   - El lado de recepción reutiliza servicio:read y servicio:create (ya existentes).
-- =============================================================================

CREATE TABLE pedidos_servicio (
    pedido_servicio_id   SERIAL          PRIMARY KEY,
    cantidad             NUMERIC(10,2)   NOT NULL,
    observaciones        TEXT,
    estado               VARCHAR(15)     NOT NULL DEFAULT 'PENDIENTE',
    motivo_respuesta     TEXT,
    fecha_respuesta      TIMESTAMP,
    tipo_servicio_id     INTEGER         NOT NULL,
    estancia_id          INTEGER         NOT NULL,
    servicio_id          INTEGER,
    usuario_respuesta_id INTEGER,
    fecha_creacion       TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_modificacion   TIMESTAMP,

    CONSTRAINT fk_pedidos_servicio_tipo      FOREIGN KEY (tipo_servicio_id)     REFERENCES tipos_servicio(tipo_servicio_id),
    CONSTRAINT fk_pedidos_servicio_estancia  FOREIGN KEY (estancia_id)          REFERENCES estancias(estancia_id),
    CONSTRAINT fk_pedidos_servicio_servicio  FOREIGN KEY (servicio_id)          REFERENCES servicios(servicio_id),
    CONSTRAINT fk_pedidos_servicio_usuario   FOREIGN KEY (usuario_respuesta_id) REFERENCES usuarios(usuario_id),
    CONSTRAINT chk_pedidos_servicio_estado   CHECK (estado IN ('PENDIENTE','APROBADO','RECHAZADO','CANCELADO')),
    CONSTRAINT chk_pedidos_servicio_cantidad CHECK (cantidad > 0)
);

CREATE INDEX idx_pedidos_servicio_estancia ON pedidos_servicio(estancia_id);
CREATE INDEX idx_pedidos_servicio_estado   ON pedidos_servicio(estado);

-- ── Permisos del cliente ──
INSERT INTO permisos (nombre, codigo, fecha_creacion) VALUES
('Leer mis pedidos de servicio', 'mis-servicios:read',   NOW()),
('Crear pedidos de servicio',    'mis-servicios:create', NOW());

SELECT setval('permisos_permiso_id_seq', COALESCE((SELECT MAX(permiso_id) FROM permisos), 1));

INSERT INTO detalles_rol (permiso_id, rol_id, fecha_creacion)
SELECT p.permiso_id, r.rol_id, NOW()
FROM permisos p
CROSS JOIN roles r
WHERE r.nombre = 'CLIENTE'
  AND p.codigo IN ('mis-servicios:read', 'mis-servicios:create')
ON CONFLICT (permiso_id, rol_id) DO NOTHING;
