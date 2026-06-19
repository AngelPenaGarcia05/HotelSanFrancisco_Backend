package com.sanfrancisco.api.modules.recepcion.mapper;

import com.sanfrancisco.api.modules.recepcion.dto.request.ReservaHabitacionRequest;
import com.sanfrancisco.api.modules.recepcion.dto.response.ReservaHabitacionResponse;
import com.sanfrancisco.api.modules.recepcion.entity.Habitacion;
import com.sanfrancisco.api.modules.recepcion.entity.Reserva;
import com.sanfrancisco.api.modules.recepcion.entity.ReservaHabitacion;
import com.sanfrancisco.api.modules.recepcion.entity.TipoHabitacion;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReservaHabitacion;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ReservaHabitacionMapper {

    public ReservaHabitacion toEntity(ReservaHabitacionRequest req,
                                      Reserva reserva,
                                      Habitacion habitacion,
                                      TipoHabitacion tipo,
                                      long noches) {
        BigDecimal tarifa = req.tarifaPactada() != null ? req.tarifaPactada() : tipo.getPrecioBase();
        BigDecimal subtotal = tarifa.multiply(BigDecimal.valueOf(noches));
        return ReservaHabitacion.builder()
                .reserva(reserva)
                .habitacion(habitacion)
                .tipoHabitacion(tipo)
                .tarifaPactada(tarifa)
                .noches((int) noches)
                .subtotal(subtotal)
                .estado(EstadoReservaHabitacion.RESERVADA)
                .build();
    }

    public ReservaHabitacionResponse toResponse(ReservaHabitacion entity) {
        return new ReservaHabitacionResponse(
                entity.getReservaHabitacionId(),
                entity.getHabitacion().getHabitacionId(),
                entity.getHabitacion().getNumero(),
                entity.getTipoHabitacion().getTipoHabitacionId(),
                entity.getTipoHabitacion().getNombre(),
                entity.getTarifaPactada(),
                entity.getNoches(),
                entity.getSubtotal(),
                entity.getEstado()
        );
    }
}
