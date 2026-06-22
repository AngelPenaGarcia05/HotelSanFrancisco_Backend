package com.sanfrancisco.api.modules.pagos.service.impl;

import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.pagos.entity.Pago;
import com.sanfrancisco.api.modules.pagos.repository.PagoRepository;
import com.sanfrancisco.api.modules.pagos.service.interfaces.MiFacturaService;
import com.sanfrancisco.api.modules.recepcion.entity.Reserva;
import com.sanfrancisco.api.modules.recepcion.entity.ReservaHabitacion;
import com.sanfrancisco.api.modules.recepcion.repository.ReservaHabitacionRepository;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.seguridad.security.UserPrincipal;
import com.sanfrancisco.api.shared.exception.ValidationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class MiFacturaServiceImpl implements MiFacturaService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PagoRepository pagoRepository;
    private final ReservaHabitacionRepository reservaHabitacionRepository;

    public MiFacturaServiceImpl(PagoRepository pagoRepository,
                                ReservaHabitacionRepository reservaHabitacionRepository) {
        this.pagoRepository = pagoRepository;
        this.reservaHabitacionRepository = reservaHabitacionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public String generarFacturaHtml(Integer pagoId) {
        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado: " + pagoId));

        Reserva reserva = pago.getReserva();
        if (reserva == null) {
            throw new ValidationException("El pago no está asociado a una reserva");
        }

        // Validación de propiedad: el pago debe ser de una reserva del cliente autenticado
        Integer usuarioId = currentUserId();
        if (reserva.getUsuario() == null || !reserva.getUsuario().getUsuarioId().equals(usuarioId)) {
            throw new ValidationException("No tienes acceso a este comprobante");
        }

        Usuario cliente = reserva.getUsuario();
        String nombreCliente = (cliente.getNombre() + " "
                + (cliente.getApellidoPaterno() != null ? cliente.getApellidoPaterno() : "")).trim();

        List<ReservaHabitacion> habitaciones = reservaHabitacionRepository.findByReservaReservaId(reserva.getReservaId());

        StringBuilder filas = new StringBuilder();
        for (ReservaHabitacion rh : habitaciones) {
            String tipo = rh.getTipoHabitacion() != null ? rh.getTipoHabitacion().getNombre() : "Habitación";
            String numero = rh.getHabitacion() != null ? rh.getHabitacion().getNumero() : "";
            filas.append("<tr>")
                 .append("<td>").append(esc(tipo + (numero.isBlank() ? "" : " - " + numero))).append("</td>")
                 .append("<td class='r'>").append(rh.getNoches()).append("</td>")
                 .append("<td class='r'>S/ ").append(rh.getTarifaPactada()).append("</td>")
                 .append("<td class='r'>S/ ").append(rh.getSubtotal()).append("</td>")
                 .append("</tr>");
        }

        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                  <meta charset="UTF-8">
                  <title>Comprobante %s</title>
                  <style>
                    body { font-family: Arial, sans-serif; color: #1f2937; max-width: 720px; margin: 24px auto; padding: 0 16px; }
                    h1 { color: #C5A048; margin-bottom: 0; }
                    .sub { color: #6b7280; margin-top: 4px; }
                    .box { border: 1px solid #e5e7eb; border-radius: 8px; padding: 16px; margin-top: 16px; }
                    table { width: 100%%; border-collapse: collapse; margin-top: 12px; }
                    th, td { padding: 8px; border-bottom: 1px solid #e5e7eb; text-align: left; font-size: 14px; }
                    th { background: #faf6ec; }
                    .r { text-align: right; }
                    .tot { font-size: 18px; font-weight: bold; color: #C5A048; }
                    .muted { color: #6b7280; font-size: 13px; }
                    @media print { .noprint { display: none; } }
                  </style>
                </head>
                <body>
                  <button class="noprint" onclick="window.print()" style="float:right;padding:8px 16px;background:#C5A048;color:#fff;border:none;border-radius:6px;cursor:pointer;">Imprimir / Guardar PDF</button>
                  <h1>Hotel San Francisco</h1>
                  <p class="sub">Comprobante de pago</p>

                  <div class="box">
                    <strong>Cliente:</strong> %s<br>
                    <strong>Documento:</strong> %s<br>
                    <strong>Reserva:</strong> %s<br>
                    <strong>Estadía:</strong> %s al %s
                  </div>

                  <table>
                    <thead>
                      <tr><th>Concepto</th><th class="r">Noches</th><th class="r">Tarifa</th><th class="r">Subtotal</th></tr>
                    </thead>
                    <tbody>
                      %s
                    </tbody>
                  </table>

                  <div class="box">
                    <table>
                      <tr><td>Subtotal</td><td class="r">S/ %s</td></tr>
                      <tr><td>Descuento</td><td class="r">S/ %s</td></tr>
                      <tr><td>IGV</td><td class="r">S/ %s</td></tr>
                      <tr><td class="tot">Total reserva</td><td class="r tot">S/ %s</td></tr>
                    </table>
                  </div>

                  <div class="box">
                    <strong>Pago registrado</strong><br>
                    <span class="muted">N.° de pago %s — %s</span><br>
                    Método: %s<br>
                    <span class="tot">Monto pagado: S/ %s</span>
                  </div>

                  <p class="muted" style="margin-top:24px;">Documento generado automáticamente. No constituye comprobante electrónico SUNAT.</p>
                </body>
                </html>
                """.formatted(
                        esc(reserva.getCodReserva()),
                        esc(nombreCliente),
                        esc(cliente.getNumeroDocumento() != null ? cliente.getNumeroDocumento() : "-"),
                        esc(reserva.getCodReserva()),
                        reserva.getFechaInicio().format(FECHA),
                        reserva.getFechaFin().format(FECHA),
                        filas.toString(),
                        reserva.getSubtotal(),
                        reserva.getDescuento(),
                        reserva.getImpuesto(),
                        reserva.getMontoTotal(),
                        pago.getPagoId(),
                        pago.getFecha().format(FECHA_HORA),
                        esc(pago.getMetodoPago() != null ? pago.getMetodoPago().getNombre() : "-"),
                        pago.getMonto()
                );
    }

    private String esc(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }

    private Integer currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BadCredentialsException("No autenticado");
        }
        return principal.userId();
    }
}
