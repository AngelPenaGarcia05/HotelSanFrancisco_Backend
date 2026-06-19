-- =============================================================================
-- FASE 6: Auditoría de acciones
-- Tabla de registros de auditoría + permiso auditoria:read (solo ADMIN)
-- =============================================================================

CREATE TABLE registros_auditoria (
    registro_id     SERIAL        PRIMARY KEY,
    usuario_id      INTEGER,
    usuario_correo  VARCHAR(150),
    accion          VARCHAR(80)   NOT NULL,
    modulo          VARCHAR(60)   NOT NULL,
    descripcion     VARCHAR(255),
    metodo_http     VARCHAR(10),
    ruta            VARCHAR(255),
    ip_origen       VARCHAR(45),
    resultado       VARCHAR(10)   NOT NULL,
    detalle_error   VARCHAR(500),
    fecha           TIMESTAMP     NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_registros_auditoria_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(usuario_id) ON DELETE SET NULL,
    CONSTRAINT chk_registros_auditoria_resultado CHECK (resultado IN ('EXITO','ERROR'))
);

CREATE INDEX idx_registros_auditoria_fecha    ON registros_auditoria(fecha);
CREATE INDEX idx_registros_auditoria_usuario  ON registros_auditoria(usuario_id);
CREATE INDEX idx_registros_auditoria_modulo   ON registros_auditoria(modulo);
CREATE INDEX idx_registros_auditoria_accion   ON registros_auditoria(accion);
CREATE INDEX idx_registros_auditoria_resultado ON registros_auditoria(resultado);

-- Permiso de lectura de auditoría (solo ADMIN)
INSERT INTO permisos (nombre, codigo, fecha_creacion)
VALUES ('Consultar auditoría', 'auditoria:read', NOW())
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO detalle_roles (rol_id, permiso_id, fecha_creacion)
SELECT r.rol_id, p.permiso_id, NOW()
FROM roles r
         JOIN permisos p ON p.codigo = 'auditoria:read'
WHERE r.nombre = 'ADMIN'
ON CONFLICT DO NOTHING;
