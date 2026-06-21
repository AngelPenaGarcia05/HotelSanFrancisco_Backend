package com.sanfrancisco.api.modules.seguridad.dto.response;

import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DashboardClienteResponse(
        long totalReservas,
        long reservasCompletadas,
        long reservasCanceladas,
        BigDecimal saldoPendiente,
        ReservaResumenItem proximaReserva,
        ReservaResumenItem reservaActiva
) {

    public record ReservaResumenItem(
            Integer reservaId,
            String codReserva,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            EstadoReserva estado,
            BigDecimal montoTotal,
            BigDecimal adelanto
    ) {}
}
