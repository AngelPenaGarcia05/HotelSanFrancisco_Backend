package com.sanfrancisco.api.modules.reportes.dto.response;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReservationsReportResponse(
        long totalReservas,
        long totalCanceladas,
        BigDecimal tasaCancelacion,
        BigDecimal estanciaPromedioNoches,
        BigDecimal ingresosPerdidosCancelaciones,
        List<ReservationsByStatus> porEstado,
        List<ReservationsByRoomType> porTipoHabitacion,
        List<ReservationsByChannel> porCanal,
        List<ReservationsPoint> serie
) {

    public record ReservationsByChannel(
            String canal,
            long reservas,
            BigDecimal ingresos,
            long canceladas,
            BigDecimal tasaCancelacion
    ) {
    }

    public record ReservationsByStatus(
            String estado,
            long cantidad,
            BigDecimal porcentaje
    ) {
    }

    public record ReservationsByRoomType(
            String tipoHabitacion,
            long cantidad,
            BigDecimal ingresos
    ) {
    }

    public record ReservationsPoint(
            LocalDate fecha,
            long nuevas,
            long canceladas,
            long checkIns,
            long checkOuts
    ) {
    }
}
