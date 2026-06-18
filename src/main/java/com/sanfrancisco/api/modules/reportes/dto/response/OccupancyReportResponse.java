package com.sanfrancisco.api.modules.reportes.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OccupancyReportResponse(
        BigDecimal ocupacionPromedio,
        BigDecimal adrPromedio,
        BigDecimal revparPromedio,
        List<OccupancyPoint> serie,
        List<OccupancyByRoomType> porTipoHabitacion) {

    public record OccupancyPoint(
            LocalDate fecha,
            long habitacionesTotal,
            long habitacionesOcupadas,
            BigDecimal porcentajeOcupacion) {
    }

    public record OccupancyByRoomType(
            String tipoHabitacion,
            long habitacionesTotal,
            long nochesDisponibles,
            long nochesOcupadas,
            BigDecimal porcentajeOcupacion,
            BigDecimal adr,
            BigDecimal revpar) {
    }
}
