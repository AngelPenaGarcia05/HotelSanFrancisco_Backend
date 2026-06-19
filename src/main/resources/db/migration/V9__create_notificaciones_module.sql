-- =============================================================================
-- MÓDULO NOTIFICACIONES — Configuración SMTP, plantillas de correo, log de envíos
-- =============================================================================

CREATE TABLE smtp_config (
    smtp_config_id      SERIAL          PRIMARY KEY,
    host                 VARCHAR(150)    NOT NULL,
    puerto               INTEGER         NOT NULL,
    usuario              VARCHAR(150)    NOT NULL,
    password_cifrado     VARCHAR(500),
    seguridad            VARCHAR(10)     NOT NULL DEFAULT 'TLS',
    nombre_remitente     VARCHAR(100)    NOT NULL,
    correo_remitente     VARCHAR(150)    NOT NULL,
    responder_a          VARCHAR(150),
    habilitado           BOOLEAN         NOT NULL DEFAULT FALSE,
    fecha_creacion       TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_modificacion   TIMESTAMP,

    CONSTRAINT chk_smtp_config_seguridad CHECK (seguridad IN ('NONE','SSL','TLS'))
);

-- Fila única de configuración (singleton). Se referencia siempre por id = 1.
INSERT INTO smtp_config (smtp_config_id, host, puerto, usuario, seguridad, nombre_remitente, correo_remitente, habilitado)
VALUES (1, 'smtp.gmail.com', 587, 'no-configurado@hotelsanfrancisco.pe', 'TLS', 'Hotel San Francisco', 'no-configurado@hotelsanfrancisco.pe', FALSE);

CREATE TABLE plantillas_correo (
    plantilla_id        SERIAL          PRIMARY KEY,
    clave                VARCHAR(40)     NOT NULL UNIQUE,
    nombre               VARCHAR(100)    NOT NULL,
    asunto               VARCHAR(200)    NOT NULL,
    cuerpo_html          TEXT            NOT NULL,
    activo               BOOLEAN         NOT NULL DEFAULT TRUE,
    fecha_creacion       TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_modificacion   TIMESTAMP,

    CONSTRAINT chk_plantillas_clave CHECK (clave IN (
        'RESERVATION_CONFIRMATION','PAYMENT_CONFIRMATION','RESERVATION_CANCELLATION','STAY_REMINDER'
    ))
);

INSERT INTO plantillas_correo (clave, nombre, asunto, cuerpo_html, activo) VALUES
('RESERVATION_CONFIRMATION', 'Confirmación de reserva', 'Tu reserva {{codReserva}} ha sido confirmada',
 '<p>Hola {{nombreHuesped}},</p><p>Tu reserva <strong>{{codReserva}}</strong> en Hotel San Francisco ha sido confirmada para el {{fechaInicio}} - {{fechaFin}}.</p><p>Monto total: S/ {{montoTotal}}</p>', TRUE),
('PAYMENT_CONFIRMATION', 'Confirmación de pago', 'Hemos recibido tu pago de S/ {{monto}}',
 '<p>Hola {{nombreHuesped}},</p><p>Confirmamos la recepción de tu pago de <strong>S/ {{monto}}</strong> asociado a la reserva {{codReserva}}.</p>', TRUE),
('RESERVATION_CANCELLATION', 'Cancelación de reserva', 'Tu reserva {{codReserva}} ha sido cancelada',
 '<p>Hola {{nombreHuesped}},</p><p>Tu reserva <strong>{{codReserva}}</strong> ha sido cancelada.</p><p>{{motivo}}</p>', TRUE),
('STAY_REMINDER', 'Recordatorio de estadía', 'Tu llegada al Hotel San Francisco es pronto',
 '<p>Hola {{nombreHuesped}},</p><p>Te recordamos que tu check-in para la reserva <strong>{{codReserva}}</strong> es el {{fechaInicio}}.</p>', TRUE);

CREATE TABLE log_correos (
    log_correo_id        SERIAL          PRIMARY KEY,
    destinatario          VARCHAR(150)    NOT NULL,
    asunto                VARCHAR(200)    NOT NULL,
    plantilla_clave       VARCHAR(40)     NOT NULL,
    estado                VARCHAR(10)     NOT NULL,
    reserva_id            INTEGER,
    pago_id               INTEGER,
    enviado_en            TIMESTAMP       NOT NULL DEFAULT NOW(),
    error                 VARCHAR(500),
    intentos              INTEGER         NOT NULL DEFAULT 1,
    fecha_creacion        TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_modificacion    TIMESTAMP,

    CONSTRAINT fk_log_correos_reserva FOREIGN KEY (reserva_id) REFERENCES reservas(reserva_id),
    CONSTRAINT fk_log_correos_pago    FOREIGN KEY (pago_id)    REFERENCES pagos(pago_id),
    CONSTRAINT chk_log_correos_estado CHECK (estado IN ('ENVIADO','PENDIENTE','FALLIDO'))
);

CREATE INDEX idx_log_correos_estado ON log_correos(estado);
CREATE INDEX idx_log_correos_enviado_en ON log_correos(enviado_en);

CREATE TABLE recordatorio_config (
    recordatorio_config_id SERIAL        PRIMARY KEY,
    horas_antes_checkin     INTEGER       NOT NULL DEFAULT 24,
    habilitado               BOOLEAN       NOT NULL DEFAULT TRUE,
    hora_envio                VARCHAR(5)    NOT NULL DEFAULT '08:00',
    fecha_creacion             TIMESTAMP     NOT NULL DEFAULT NOW(),
    fecha_modificacion         TIMESTAMP
);

INSERT INTO recordatorio_config (recordatorio_config_id, horas_antes_checkin, habilitado, hora_envio)
VALUES (1, 24, TRUE, '08:00');
