package com.sanfrancisco.api.modules.booking.service;

import com.sanfrancisco.api.modules.booking.dto.BookingConfirmationResponse;
import com.sanfrancisco.api.modules.booking.dto.HabitacionReservadaResumen;
import com.sanfrancisco.api.modules.pagos.enums.TipoPago;
import com.sanfrancisco.api.modules.recepcion.entity.Habitacion;
import com.sanfrancisco.api.modules.recepcion.entity.Huesped;
import com.sanfrancisco.api.modules.recepcion.entity.Reserva;
import com.sanfrancisco.api.modules.recepcion.entity.ReservaHabitacion;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Construye la respuesta de confirmación del flujo público a partir de las
 * entidades persistidas, para que la creación de reserva y la confirmación
 * de pago devuelvan exactamente la misma estructura.
 */
@Component
public class BookingConfirmationFactory {

    public BookingConfirmationResponse build(Reserva reserva, List<ReservaHabitacion> rhs, Huesped huesped,
                                             TipoPago tipoPago) {
        if (rhs == null || rhs.isEmpty()) {
            throw new IllegalStateException("Reserva sin habitaciones asignadas: " + reserva.getReservaId());
        }
        ReservaHabitacion primera = rhs.get(0);
        Habitacion habitacion = primera.getHabitacion();
        BigDecimal adelanto = reserva.getAdelanto() != null ? reserva.getAdelanto() : BigDecimal.ZERO;
        BigDecimal montoPendiente = reserva.getMontoTotal().subtract(adelanto).max(BigDecimal.ZERO);

        List<HabitacionReservadaResumen> habitaciones = rhs.stream()
                .map(rh -> new HabitacionReservadaResumen(
                        rh.getHabitacion().getNumero(),
                        rh.getHabitacion().getPiso(),
                        rh.getTipoHabitacion().getNombre(),
                        rh.getTarifaPactada()))
                .toList();

        return new BookingConfirmationResponse(
                reserva.getReservaId(),
                reserva.getCodReserva(),
                reserva.getFechaInicio(),
                reserva.getFechaFin(),
                primera.getNoches(),
                habitacion.getNumero(),
                habitacion.getPiso(),
                primera.getTipoHabitacion().getNombre(),
                habitaciones,
                huesped.getNombre(),
                nombreApellidos(huesped),
                huesped.getNumeroDocumento(),
                huesped.getCorreo(),
                huesped.getTelefono(),
                primera.getTarifaPactada(),
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
