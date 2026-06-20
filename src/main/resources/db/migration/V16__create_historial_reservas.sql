-- =============================================================================
-- V16: Historial de cambios de estado de reservas
-- =============================================================================

CREATE TABLE historial_reservas (
    historial_id        SERIAL          PRIMARY KEY,
    reserva_id          INTEGER         NOT NULL,
    estado_anterior     VARCHAR(20),
    estado_nuevo        VARCHAR(20)     NOT NULL,
    motivo              TEXT,
    fecha_creacion      TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP,

    CONSTRAINT fk_historial_reservas_reserva
        FOREIGN KEY (reserva_id) REFERENCES reservas(reserva_id) ON DELETE CASCADE,
    CONSTRAINT chk_historial_reservas_estado_nuevo
        CHECK (estado_nuevo IN ('PENDIENTE','CONFIRMADA','CHECK_IN','CHECK_OUT','CANCELADA','NO_SHOW')),
    CONSTRAINT chk_historial_reservas_estado_anterior
        CHECK (estado_anterior IS NULL OR estado_anterior IN ('PENDIENTE','CONFIRMADA','CHECK_IN','CHECK_OUT','CANCELADA','NO_SHOW'))
);

CREATE INDEX idx_historial_reservas_reserva ON historial_reservas(reserva_id);
CREATE INDEX idx_historial_reservas_fecha   ON historial_reservas(fecha_creacion);
