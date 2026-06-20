-- =============================================================================
-- V15: Plantilla de correo para notificación de cambio de estado de solicitud
-- =============================================================================

-- 1. Ampliar el CHECK constraint de plantillas_correo para incluir REQUEST_STATUS_CHANGED
ALTER TABLE plantillas_correo
    DROP CONSTRAINT IF EXISTS chk_plantillas_clave;

ALTER TABLE plantillas_correo
    ADD CONSTRAINT chk_plantillas_clave CHECK (clave IN (
        'RESERVATION_CONFIRMATION',
        'PAYMENT_CONFIRMATION',
        'RESERVATION_CANCELLATION',
        'STAY_REMINDER',
        'PASSWORD_RESET',
        'REQUEST_STATUS_CHANGED'
    ));

-- 2. Plantilla de notificación de cambio de estado de solicitud
INSERT INTO plantillas_correo (clave, nombre, asunto, cuerpo_html, activo)
VALUES (
    'REQUEST_STATUS_CHANGED',
    'Actualización de solicitud de servicio',
    'Tu solicitud {{codigoSolicitud}} ha sido actualizada',
    '<!DOCTYPE html><html lang="es"><body style="margin:0;padding:0;font-family:Arial,sans-serif;background:#f4f4f4;">
<table width="100%" cellpadding="0" cellspacing="0"><tr><td align="center" style="padding:40px 20px;">
<table width="600" style="background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.1);">
  <tr><td style="background:#1a3c5e;padding:24px 32px;">
    <h1 style="color:#fff;margin:0;font-size:22px;">Hotel San Francisco</h1>
  </td></tr>
  <tr><td style="padding:32px;">
    <p style="font-size:16px;color:#333;">Hola <strong>{{nombreUsuario}}</strong>,</p>
    <p style="color:#555;">El estado de tu solicitud ha sido actualizado:</p>
    <table width="100%" style="margin:20px 0;border-collapse:collapse;">
      <tr>
        <td style="padding:10px 16px;background:#f9f9f9;border:1px solid #eee;font-weight:bold;width:40%;">Código</td>
        <td style="padding:10px 16px;border:1px solid #eee;">{{codigoSolicitud}}</td>
      </tr>
      <tr>
        <td style="padding:10px 16px;background:#f9f9f9;border:1px solid #eee;font-weight:bold;">Asunto</td>
        <td style="padding:10px 16px;border:1px solid #eee;">{{asuntoSolicitud}}</td>
      </tr>
      <tr>
        <td style="padding:10px 16px;background:#f9f9f9;border:1px solid #eee;font-weight:bold;">Estado anterior</td>
        <td style="padding:10px 16px;border:1px solid #eee;">{{estadoAnterior}}</td>
      </tr>
      <tr>
        <td style="padding:10px 16px;background:#f9f9f9;border:1px solid #eee;font-weight:bold;">Nuevo estado</td>
        <td style="padding:10px 16px;border:1px solid #eee;color:#1a3c5e;font-weight:bold;">{{nuevoEstado}}</td>
      </tr>
    </table>
    <p style="color:#555;">{{observacion}}</p>
    <hr style="border:none;border-top:1px solid #eee;margin:24px 0;">
    <p style="color:#aaa;font-size:12px;text-align:center;">© Hotel San Francisco — Este es un correo automático, no respondas a este mensaje.</p>
  </td></tr>
</table></td></tr></table>
</body></html>',
    TRUE
)
ON CONFLICT (clave) DO NOTHING;
