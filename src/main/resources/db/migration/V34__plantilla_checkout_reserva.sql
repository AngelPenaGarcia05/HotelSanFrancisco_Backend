-- =============================================================================
-- V34: Plantilla de correo para check-out de reserva (fin de estadía)
-- Nota: sin datos de factura por ahora (módulo de facturación pendiente);
-- cuando exista, se podrán añadir variables de comprobante a esta plantilla.
-- =============================================================================

-- 1. Ampliar el CHECK constraint de plantillas_correo para incluir STAY_CHECKOUT
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
        'REQUEST_STATUS_CHANGED'
    ));

-- 2. Plantilla de correo de check-out
INSERT INTO plantillas_correo (clave, nombre, asunto, cuerpo_html, activo)
VALUES (
    'STAY_CHECKOUT',
    'Fin de estadía (check-out)',
    'Gracias por tu estadía — reserva {{codReserva}}',
    '<!DOCTYPE html><html lang="es"><body style="margin:0;padding:0;font-family:Arial,sans-serif;background:#f4f4f4;">
<table width="100%" cellpadding="0" cellspacing="0"><tr><td align="center" style="padding:40px 20px;">
<table width="600" style="background:#fff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.1);">
  <tr><td style="background:#1a3c5e;padding:24px 32px;">
    <h1 style="color:#fff;margin:0;font-size:22px;">Hotel San Francisco</h1>
  </td></tr>
  <tr><td style="padding:32px;">
    <p style="font-size:16px;color:#333;">Hola <strong>{{nombreHuesped}}</strong>,</p>
    <p style="color:#555;">Tu check-out se ha completado. Gracias por hospedarte con nosotros; este es el resumen de tu estadía:</p>
    <table width="100%" style="margin:20px 0;border-collapse:collapse;">
      <tr>
        <td style="padding:10px 16px;background:#f9f9f9;border:1px solid #eee;font-weight:bold;width:40%;">Código de reserva</td>
        <td style="padding:10px 16px;border:1px solid #eee;">{{codReserva}}</td>
      </tr>
      <tr>
        <td style="padding:10px 16px;background:#f9f9f9;border:1px solid #eee;font-weight:bold;">Llegada</td>
        <td style="padding:10px 16px;border:1px solid #eee;">{{fechaInicio}}</td>
      </tr>
      <tr>
        <td style="padding:10px 16px;background:#f9f9f9;border:1px solid #eee;font-weight:bold;">Salida</td>
        <td style="padding:10px 16px;border:1px solid #eee;">{{fechaFin}}</td>
      </tr>
      <tr>
        <td style="padding:10px 16px;background:#f9f9f9;border:1px solid #eee;font-weight:bold;">Monto total</td>
        <td style="padding:10px 16px;border:1px solid #eee;color:#1a3c5e;font-weight:bold;">S/ {{montoTotal}}</td>
      </tr>
    </table>
    <p style="color:#555;">Esperamos verte pronto de nuevo. ¡Buen viaje!</p>
    <hr style="border:none;border-top:1px solid #eee;margin:24px 0;">
    <p style="color:#aaa;font-size:12px;text-align:center;">© Hotel San Francisco — Este es un correo automático, no respondas a este mensaje.</p>
  </td></tr>
</table></td></tr></table>
</body></html>',
    TRUE
)
ON CONFLICT (clave) DO NOTHING;
