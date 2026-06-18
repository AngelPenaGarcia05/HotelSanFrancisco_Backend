package com.sanfrancisco.api.modules.recepcion.dto.response;

import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;

import java.time.LocalDateTime;

public record HistorialReservaResponse(
        Integer historialId,
        Integer reservaId,
        String codReserva,
        EstadoReserva estadoAnterior,
        EstadoReserva estadoNuevo,
        String motivo,
        LocalDateTime fechaCambio
) {
}
