-- =============================================================================
-- FASE 4: Recuperación de contraseña
-- Tabla de tokens de recuperación + plantilla de correo PASSWORD_RESET
-- =============================================================================

-- 1. Ampliar el CHECK constraint de plantillas_correo para incluir PASSWORD_RESET
ALTER TABLE plantillas_correo
    DROP CONSTRAINT IF EXISTS chk_plantillas_clave;

ALTER TABLE plantillas_correo
    ADD CONSTRAINT chk_plantillas_clave CHECK (clave IN (
        'RESERVATION_CONFIRMATION',
        'PAYMENT_CONFIRMATION',
        'RESERVATION_CANCELLATION',
        'STAY_REMINDER',
        'PASSWORD_RESET'
    ));

-- 2. Insertar plantilla de correo para recuperación de contraseña
INSERT INTO plantillas_correo (clave, nombre, asunto, cuerpo_html, activo)
VALUES (
    'PASSWORD_RESET',
    'Recuperación de contraseña',
    'Recupera tu contraseña — Hotel San Francisco',
    '<!DOCTYPE html><html lang="es"><body style="margin:0;padding:0;font-family:Arial,sans-serif;background:#f4f4f4;">
<table width="100%" cellpadding="0" cellspacing="0"><tr><td align="center" style="padding:40px 20px;">
<table width="600" style="background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.1);">
  <tr><td style="background:#1a3c5e;padding:24px 32px;">
    <h1 style="color:#fff;margin:0;font-size:22px;">Hotel San Francisco</h1>
  </td></tr>
  <tr><td style="padding:32px;">
    <p style="font-size:16px;color:#333;">Hola <strong>{{nombreUsuario}}</strong>,</p>
    <p style="color:#555;">Recibimos una solicitud para restablecer la contraseña de tu cuenta. Haz clic en el botón de abajo para continuar:</p>
    <p style="text-align:center;margin:32px 0;">
      <a href="{{linkReset}}" style="background:#1a3c5e;color:#fff;padding:14px 28px;text-decoration:none;border-radius:4px;font-size:15px;display:inline-block;">
        Restablecer contraseña
      </a>
    </p>
    <p style="color:#888;font-size:13px;">Este enlace expira en <strong>30 minutos</strong>. Si no solicitaste este cambio, puedes ignorar este correo con seguridad.</p>
    <hr style="border:none;border-top:1px solid #eee;margin:24px 0;">
    <p style="color:#aaa;font-size:12px;text-align:center;">© Hotel San Francisco — Este es un correo automático, no respondas a este mensaje.</p>
  </td></tr>
</table></td></tr></table>
</body></html>',
    TRUE
)
ON CONFLICT (clave) DO NOTHING;

-- 3. Tabla de tokens de recuperación de contraseña
CREATE TABLE IF NOT EXISTS tokens_recuperacion (
    token_id          SERIAL       PRIMARY KEY,
    usuario_id        INTEGER      NOT NULL REFERENCES usuarios(usuario_id) ON DELETE CASCADE,
    token_hash        VARCHAR(255) NOT NULL UNIQUE,
    fecha_expiracion  TIMESTAMP    NOT NULL,
    usado             BOOLEAN      NOT NULL DEFAULT FALSE,
    fecha_creacion    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_tokens_recuperacion_hash
    ON tokens_recuperacion(token_hash);

CREATE INDEX IF NOT EXISTS idx_tokens_recuperacion_usuario
    ON tokens_recuperacion(usuario_id);
