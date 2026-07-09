-- =============================================================================
-- V36: Verificación de correo obligatoria en el registro
--   1. Columna usuarios.correo_verificado (default TRUE: los usuarios existentes
--      y los creados por un admin quedan verificados; solo el auto-registro
--      pondrá FALSE explícitamente).
--   2. Tabla codigos_verificacion (códigos de 6 dígitos, hasheados, con
--      expiración, un solo uso y límite de intentos).
-- =============================================================================

ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS correo_verificado BOOLEAN NOT NULL DEFAULT TRUE;

CREATE TABLE IF NOT EXISTS codigos_verificacion (
    codigo_id         SERIAL       PRIMARY KEY,
    usuario_id        INTEGER      NOT NULL REFERENCES usuarios(usuario_id) ON DELETE CASCADE,
    codigo_hash       VARCHAR(255) NOT NULL,
    fecha_expiracion  TIMESTAMP    NOT NULL,
    usado             BOOLEAN      NOT NULL DEFAULT FALSE,
    intentos          INTEGER      NOT NULL DEFAULT 0,
    fecha_creacion    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_codigos_verificacion_usuario
    ON codigos_verificacion(usuario_id);
