package com.sanfrancisco.api.modules.servicios.dto.response;

import com.sanfrancisco.api.modules.servicios.enums.EstadoPedidoServicio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoServicioResponse(
        Integer pedidoServicioId,
        Integer tipoServicioId,
        String tipoServicioNombre,
        BigDecimal costoBase,
        Integer cantidad,
        BigDecimal subtotalEstimado,
        String observaciones,
        EstadoPedidoServicio estado,
        String motivoRespuesta,
        Integer estanciaId,
        String codReserva,
        String solicitanteNombre,
        Integer servicioId,
        LocalDateTime fechaSolicitud,
        LocalDateTime fechaRespuesta
) {
}
