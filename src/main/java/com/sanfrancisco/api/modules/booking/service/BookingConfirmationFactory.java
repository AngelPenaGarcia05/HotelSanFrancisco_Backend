package com.sanfrancisco.api.modules.booking.service;

import com.sanfrancisco.api.modules.booking.dto.BookingConfirmationResponse;
import com.sanfrancisco.api.modules.pagos.enums.TipoPago;
import com.sanfrancisco.api.modules.recepcion.entity.Habitacion;
import com.sanfrancisco.api.modules.recepcion.entity.Huesped;
import com.sanfrancisco.api.modules.recepcion.entity.Reserva;
import com.sanfrancisco.api.modules.recepcion.entity.ReservaHabitacion;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Construye la respuesta de confirmación del flujo público a partir de las
 * entidades persistidas, para que la creación de reserva y la confirmación
 * de pago devuelvan exactamente la misma estructura.
 */
@Component
public class BookingConfirmationFactory {

    public BookingConfirmationResponse build(Reserva reserva, ReservaHabitacion rh, Huesped huesped,
                                             TipoPago tipoPago) {
        Habitacion habitacion = rh.getHabitacion();
        BigDecimal adelanto = reserva.getAdelanto() != null ? reserva.getAdelanto() : BigDecimal.ZERO;
        BigDecimal montoPendiente = reserva.getMontoTotal().subtract(adelanto).max(BigDecimal.ZERO);

        return new BookingConfirmationResponse(
                reserva.getReservaId(),
                reserva.getCodReserva(),
                reserva.getFechaInicio(),
                reserva.getFechaFin(),
                rh.getNoches(),
                habitacion.getNumero(),
                habitacion.getPiso(),
                rh.getTipoHabitacion().getNombre(),
                huesped.getNombre(),
                nombreApellidos(huesped),
                huesped.getNumeroDocumento(),
                huesped.getCorreo(),
                huesped.getTelefono(),
                rh.getTarifaPactada(),
                reserva.getSubtotal(),
                reserva.getImpuesto(),
                reserva.getMontoTotal(),
                tipoPago,
                adelanto,
                montoPendiente,
                reserva.getEstado()
        );
    }

    private String nombreApellidos(Huesped huesped) {
        String paterno = huesped.getApellidoPaterno() != null ? huesped.getApellidoPaterno() : "";
        String materno = huesped.getApellidoMaterno() != null ? huesped.getApellidoMaterno() : "";
        return (paterno + " " + materno).trim();
    }
}
