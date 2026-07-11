-- =============================================================================
-- V40__turnos.sql
-- Turnos con fecha concreta (shifts fechados). La plantilla semanal
-- (detalles_horario) actúa como molde; lo que se persiste y se cruza con la
-- asistencia son estos turnos con fecha calendario.
--
-- Relación con asistencia: cada marca (asistencia) puede enlazar al turno
-- planificado de ese día vía asistencia.turno_id, para derivar TARDANZA/horas.
-- =============================================================================

CREATE TABLE turnos (
    turno_id           SERIAL          PRIMARY KEY,
    usuario_id         INTEGER         NOT NULL,
    horario_id         INTEGER         NOT NULL,
    fecha              DATE            NOT NULL,
    hora_inicio        TIME            NOT NULL,
    hora_fin           TIME            NOT NULL,
    estado             VARCHAR(15)     NOT NULL DEFAULT 'PLANIFICADO',
    origen             VARCHAR(10)     NOT NULL DEFAULT 'PLANTILLA',
    fecha_creacion     TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_modificacion TIMESTAMP,

    CONSTRAINT fk_turnos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(usuario_id),
    CONSTRAINT fk_turnos_horario FOREIGN KEY (horario_id) REFERENCES horarios(horario_id),
    CONSTRAINT uk_turnos_usuario_fecha UNIQUE (usuario_id, fecha),
    CONSTRAINT chk_turnos_estado CHECK (estado IN ('PLANIFICADO','CONFIRMADO','CUBIERTO','AUSENTE','CANCELADO')),
    CONSTRAINT chk_turnos_origen CHECK (origen IN ('PLANTILLA','MANUAL'))
);

CREATE INDEX idx_turnos_fecha ON turnos (fecha);

-- Enlace asistencia -> turno planificado (nullable: puede no haber turno ese día)
ALTER TABLE asistencia ADD COLUMN turno_id INTEGER;
ALTER TABLE asistencia ADD CONSTRAINT fk_asistencia_turno
    FOREIGN KEY (turno_id) REFERENCES turnos(turno_id);
