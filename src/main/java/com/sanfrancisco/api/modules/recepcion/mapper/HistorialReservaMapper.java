package com.sanfrancisco.api.modules.recepcion.mapper;

import com.sanfrancisco.api.modules.recepcion.dto.response.HistorialReservaResponse;
import com.sanfrancisco.api.modules.recepcion.entity.HistorialReserva;
import com.sanfrancisco.api.modules.recepcion.entity.Reserva;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;
import org.springframework.stereotype.Component;

@Component
public class HistorialReservaMapper {

    public HistorialReserva toEntity(Reserva reserva,
                                     EstadoReserva estadoAnterior,
                                     EstadoReserva estadoNuevo,
                                     String motivo) {
        return HistorialReserva.builder()
                .reserva(reserva)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(estadoNuevo)
                .motivo(motivo)
                .build();
    }

    public HistorialReservaResponse toResponse(HistorialReserva entity) {
        return new HistorialReservaResponse(
                entity.getHistorialId(),
                entity.getReserva().getReservaId(),
                entity.getReserva().getCodReserva(),
                entity.getEstadoAnterior(),
                entity.getEstadoNuevo(),
                entity.getMotivo(),
                entity.getFechaCreacion()   // fechaCreacion = timestamp del cambio
        );
    }
}
