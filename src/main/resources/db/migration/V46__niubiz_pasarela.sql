-- =============================================================================
-- V46__niubiz_pasarela.sql
-- Integración pasarela de pagos Niubiz (booking online).
--  1. Tabla transacciones_pasarela: ciclo de vida de cada intento de cobro.
--  2. Método de pago 'Yape / Plin' → 'Yape' (Plin no es procesable por Niubiz).
-- =============================================================================

CREATE TABLE transacciones_pasarela (
    transaccion_id       SERIAL          PRIMARY KEY,
    reserva_id           INTEGER         NOT NULL,
    purchase_number      VARCHAR(12)     NOT NULL,
    monto                NUMERIC(12,2)   NOT NULL,
    moneda               VARCHAR(3)      NOT NULL DEFAULT 'PEN',
    estado               VARCHAR(20)     NOT NULL DEFAULT 'CREADA',
    session_key          VARCHAR(100),
    codigo_autorizacion  VARCHAR(30),
    codigo_accion        VARCHAR(10),
    descripcion_estado   VARCHAR(200),
    tarjeta_enmascarada  VARCHAR(30),
    marca_tarjeta        VARCHAR(30),
    transaction_id_ext   VARCHAR(40),
    respuesta_raw        TEXT,
    fecha_creacion       TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_modificacion   TIMESTAMP,

    CONSTRAINT uk_trx_pasarela_purchase  UNIQUE (purchase_number),
    CONSTRAINT fk_trx_pasarela_reserva   FOREIGN KEY (reserva_id) REFERENCES reservas(reserva_id),
    CONSTRAINT chk_trx_pasarela_estado   CHECK (estado IN ('CREADA','AUTORIZADA','RECHAZADA','ERROR','EXPIRADA')),
    CONSTRAINT chk_trx_pasarela_monto    CHECK (monto >= 0)
);

CREATE INDEX idx_trx_pasarela_reserva ON transacciones_pasarela(reserva_id);
CREATE INDEX idx_trx_pasarela_estado  ON transacciones_pasarela(estado);

-- Niubiz procesa Yape dentro de su checkout; Plin pertenece a otro ecosistema
-- (Interbank/BBVA/Scotiabank) y queda fuera del flujo online.
UPDATE metodos_pago SET nombre = 'Yape' WHERE nombre = 'Yape / Plin';
