-- =============================================================================
-- V35: Plantilla de correo para el código de verificación de cuenta (registro)
-- =============================================================================

-- 1. Ampliar el CHECK constraint de plantillas_correo para incluir EMAIL_VERIFICATION_CODE
ALTER TABLE plantillas_correo
    DROP CONSTRAINT IF EXISTS chk_plantillas_clave;

ALTER TABLE plantillas_correo
    ADD CONSTRAINT chk_plantillas_clave CHECK (clave IN (
        'RESERVATION_CONFIRMATION',
        'PAYMENT_CONFIRMATION',
        'RESERVATION_CANCELLATION',
        'RESERVATION_RESCHEDULED',
        'STAY_REMINDER',
        'STAY_CHECKOUT',
        'PASSWORD_RESET',
        'EMAIL_VERIFICATION_CODE',
        'REQUEST_STATUS_CHANGED'
    ));

-- 2. Plantilla del código de verificación
INSERT INTO plantillas_correo (clave, nombre, asunto, cuerpo_html, activo)
VALUES (
    'EMAIL_VERIFICATION_CODE',
    'Verificación de cuenta',
    'Verifica tu cuenta — Hotel San Francisco',
    '<!DOCTYPE html><html lang="es"><body style="margin:0;padding:0;font-family:Arial,sans-serif;background:#f4f4f4;">
<table width="100%" cellpadding="0" cellspacing="0"><tr><td align="center" style="padding:40px 20px;">
<table width="600" style="background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.1);">
  <tr><td style="background:#1a3c5e;padding:24px 32px;">
    <h1 style="color:#fff;margin:0;font-size:22px;">Hotel San Francisco</h1>
  </td></tr>
  <tr><td style="padding:32px;">
    <p style="font-size:16px;color:#333;">Hola <strong>{{nombreUsuario}}</strong>,</p>
    <p style="color:#555;">Gracias por registrarte. Para activar tu cuenta, ingresa el siguiente código de verificación:</p>
    <p style="text-align:center;margin:32px 0;">
      <span style="display:inline-block;background:#f4f7fa;border:1px solid #dbe4ee;color:#1a3c5e;font-size:32px;font-weight:bold;letter-spacing:8px;padding:16px 28px;border-radius:6px;">{{codigo}}</span>
    </p>
    <p style="color:#888;font-size:13px;">Este código expira en <strong>15 minutos</strong>. Si no creaste esta cuenta, puedes ignorar este correo con seguridad.</p>
    <hr style="border:none;border-top:1px solid #eee;margin:24px 0;">
    <p style="color:#aaa;font-size:12px;text-align:center;">© Hotel San Francisco — Este es un correo automático, no respondas a este mensaje.</p>
  </td></tr>
</table></td></tr></table>
</body></html>',
    TRUE
)
ON CONFLICT (clave) DO NOTHING;
