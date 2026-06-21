package com.sanfrancisco.api.modules.reportes.service.impl;

import com.sanfrancisco.api.modules.pagos.entity.Pago;
import com.sanfrancisco.api.modules.pagos.enums.TipoPago;
import com.sanfrancisco.api.modules.pagos.repository.PagoRepository;
import com.sanfrancisco.api.modules.recepcion.entity.Reserva;
import com.sanfrancisco.api.modules.recepcion.entity.ReservaHabitacion;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;
import com.sanfrancisco.api.modules.recepcion.repository.HabitacionRepository;
import com.sanfrancisco.api.modules.recepcion.repository.ReservaHabitacionRepository;
import com.sanfrancisco.api.modules.recepcion.repository.ReservaRepository;
import com.sanfrancisco.api.modules.reportes.dto.request.ExportReporteRequest;
import com.sanfrancisco.api.modules.reportes.dto.request.ReportRangeRequest;
import com.sanfrancisco.api.modules.reportes.dto.response.ManagementDashboardResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.OccupancyReportResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.ReservationsReportResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.RevenueReportResponse;
import com.sanfrancisco.api.modules.reportes.service.interfaces.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private final PagoRepository pagoRepository;
    private final ReservaRepository reservaRepository;
    private final ReservaHabitacionRepository reservaHabitacionRepository;
    private final HabitacionRepository habitacionRepository;

    public ReportServiceImpl(PagoRepository pagoRepository,
                              ReservaRepository reservaRepository,
                              ReservaHabitacionRepository reservaHabitacionRepository,
                              HabitacionRepository habitacionRepository) {
        this.pagoRepository = pagoRepository;
        this.reservaRepository = reservaRepository;
        this.reservaHabitacionRepository = reservaHabitacionRepository;
        this.habitacionRepository = habitacionRepository;
    }

    // =====================================================================
    // INGRESOS
    // =====================================================================

    @Override
    public RevenueReportResponse buildRevenueReport(ReportRangeRequest range) {
        LocalDate desde = range.resolveDesde();
        LocalDate hasta = range.resolveHasta();
        List<Pago> pagos = pagosEnRango(desde, hasta);

        BigDecimal totalAnticipos = sumarPorTipo(pagos, TipoPago.ANTICIPO);
        BigDecimal totalSaldos = sumarPorTipo(pagos, TipoPago.SALDO).add(sumarPorTipo(pagos, TipoPago.TOTAL));
        BigDecimal totalReembolsos = sumarPorTipo(pagos, TipoPago.REEMBOLSO);
        BigDecimal totalIngresos = totalAnticipos.add(totalSaldos).subtract(totalReembolsos);

        long dias = Math.max(1, ChronoUnit.DAYS.between(desde, hasta) + 1);
        BigDecimal ingresoPromedioDiario = totalIngresos.divide(BigDecimal.valueOf(dias), 2, RoundingMode.HALF_UP);

        Map<LocalDate, List<Pago>> porFecha = pagos.stream()
                .collect(Collectors.groupingBy(p -> p.getFecha().toLocalDate()));

        List<RevenueReportResponse.RevenuePoint> serie = porFecha.entrySet().stream()
                .map(e -> new RevenueReportResponse.RevenuePoint(
                        e.getKey(),
                        sumarPorTipo(e.getValue(), TipoPago.ANTICIPO),
                        sumarPorTipo(e.getValue(), TipoPago.SALDO).add(sumarPorTipo(e.getValue(), TipoPago.TOTAL)),
                        sumarPorTipo(e.getValue(), TipoPago.REEMBOLSO)
                ))
                .sorted(Comparator.comparing(RevenueReportResponse.RevenuePoint::fecha))
                .toList();

        Map<String, BigDecimal> porMetodo = pagos.stream()
                .filter(p -> p.getTipoPago() != TipoPago.REEMBOLSO)
                .collect(Collectors.groupingBy(
                        p -> p.getMetodoPago() != null ? p.getMetodoPago().getNombre() : "Sin método",
                        Collectors.reducing(BigDecimal.ZERO, Pago::getMonto, BigDecimal::add)));

        BigDecimal baseMetodos = porMetodo.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        List<RevenueReportResponse.RevenueByMethod> porMetodoPago = porMetodo.entrySet().stream()
                .map(e -> new RevenueReportResponse.RevenueByMethod(
                        e.getKey(),
                        e.getValue(),
                        porcentaje(e.getValue(), baseMetodos)))
                .sorted(Comparator.comparing(RevenueReportResponse.RevenueByMethod::monto).reversed())
                .toList();

        return new RevenueReportResponse(
                totalIngresos, totalAnticipos, totalSaldos, totalReembolsos,
                ingresoPromedioDiario, serie, porMetodoPago);
    }

    // =====================================================================
    // RESERVAS
    // =====================================================================

    @Override
    public ReservationsReportResponse buildReservationsReport(ReportRangeRequest range) {
        LocalDate desde = range.resolveDesde();
        LocalDate hasta = range.resolveHasta();
        List<Reserva> reservas = reservasCreadasEnRango(desde, hasta);

        long total = reservas.size();
        long canceladas = reservas.stream().filter(r -> r.getEstado() == EstadoReserva.CANCELADA).count();
        BigDecimal tasaCancelacion = total == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(canceladas).multiply(CIEN)
                    .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);

        List<ReservaHabitacion> habitacionesDeReservas = reservas.stream()
                .flatMap(r -> reservaHabitacionRepository.findByReservaReservaId(r.getReservaId()).stream())
                .toList();

        BigDecimal estanciaPromedio = habitacionesDeReservas.isEmpty()
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(habitacionesDeReservas.stream().mapToInt(ReservaHabitacion::getNoches).sum())
                    .divide(BigDecimal.valueOf(habitacionesDeReservas.size()), 1, RoundingMode.HALF_UP);

        Map<EstadoReserva, Long> conteoPorEstado = reservas.stream()
                .collect(Collectors.groupingBy(Reserva::getEstado, Collectors.counting()));

        List<ReservationsReportResponse.ReservationsByStatus> porEstado = conteoPorEstado.entrySet().stream()
                .map(e -> new ReservationsReportResponse.ReservationsByStatus(
                        e.getKey().name(),
                        e.getValue(),
                        total == 0 ? BigDecimal.ZERO
                            : BigDecimal.valueOf(e.getValue()).multiply(CIEN)
                                .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP)))
                .sorted(Comparator.comparing(ReservationsReportResponse.ReservationsByStatus::cantidad).reversed())
                .toList();

        Map<String, List<ReservaHabitacion>> porTipo = habitacionesDeReservas.stream()
                .collect(Collectors.groupingBy(rh -> rh.getTipoHabitacion().getNombre()));

        List<ReservationsReportResponse.ReservationsByRoomType> porTipoHabitacion = porTipo.entrySet().stream()
                .map(e -> new ReservationsReportResponse.ReservationsByRoomType(
                        e.getKey(),
                        e.getValue().size(),
                        e.getValue().stream().map(ReservaHabitacion::getSubtotal)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)))
                .sorted(Comparator.comparing(ReservationsReportResponse.ReservationsByRoomType::ingresos).reversed())
                .toList();

        Map<LocalDate, List<Reserva>> porFecha = reservas.stream()
                .collect(Collectors.groupingBy(r -> r.getFechaCreacion().toLocalDate()));

        List<ReservationsReportResponse.ReservationsPoint> serie = porFecha.entrySet().stream()
                .map(e -> new ReservationsReportResponse.ReservationsPoint(
                        e.getKey(),
                        e.getValue().size(),
                        e.getValue().stream().filter(r -> r.getEstado() == EstadoReserva.CANCELADA).count(),
                        e.getValue().stream().filter(r -> r.getEstado() == EstadoReserva.CHECK_IN).count(),
                        e.getValue().stream().filter(r -> r.getEstado() == EstadoReserva.CHECK_OUT).count()))
                .sorted(Comparator.comparing(ReservationsReportResponse.ReservationsPoint::fecha))
                .toList();

        return new ReservationsReportResponse(
                total, canceladas, tasaCancelacion, estanciaPromedio,
                porEstado, porTipoHabitacion, serie);
    }

    // =====================================================================
    // OCUPACIÓN
    // =====================================================================

    @Override
    public OccupancyReportResponse buildOccupancyReport(ReportRangeRequest range) {
        LocalDate desde = range.resolveDesde();
        LocalDate hasta = range.resolveHasta();
        long totalHabitaciones = Math.max(1, habitacionRepository.count());
        long dias = Math.max(1, ChronoUnit.DAYS.between(desde, hasta) + 1);

        List<ReservaHabitacion> ocupaciones = reservaHabitacionRepository.findAll().stream()
                .filter(rh -> seSuperponeConRango(rh, desde, hasta))
                .toList();

        // Serie diaria: para cada día del rango, cuántas habitaciones están ocupadas
        List<OccupancyReportResponse.OccupancyPoint> serie = Stream.iterate(desde, d -> d.plusDays(1))
                .limit(dias)
                .map(dia -> {
                    long ocupadas = ocupaciones.stream()
                            .filter(rh -> diaDentroDeEstancia(rh, dia))
                            .count();
                    BigDecimal pct = BigDecimal.valueOf(ocupadas).multiply(CIEN)
                            .divide(BigDecimal.valueOf(totalHabitaciones), 1, RoundingMode.HALF_UP);
                    return new OccupancyReportResponse.OccupancyPoint(dia, totalHabitaciones, ocupadas, pct);
                })
                .toList();

        BigDecimal ocupacionPromedio = serie.isEmpty()
                ? BigDecimal.ZERO
                : promedio(serie.stream().map(OccupancyReportResponse.OccupancyPoint::porcentajeOcupacion).toList());

        long nochesOcupadasTotal = ocupaciones.stream().mapToLong(ReservaHabitacion::getNoches).sum();
        BigDecimal ingresosTotal = ocupaciones.stream().map(ReservaHabitacion::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal adrPromedio = nochesOcupadasTotal == 0
                ? BigDecimal.ZERO
                : ingresosTotal.divide(BigDecimal.valueOf(nochesOcupadasTotal), 2, RoundingMode.HALF_UP);
        long nochesDisponiblesTotal = totalHabitaciones * dias;
        BigDecimal revparPromedio = nochesDisponiblesTotal == 0
                ? BigDecimal.ZERO
                : ingresosTotal.divide(BigDecimal.valueOf(nochesDisponiblesTotal), 2, RoundingMode.HALF_UP);

        Map<String, List<ReservaHabitacion>> porTipo = ocupaciones.stream()
                .collect(Collectors.groupingBy(rh -> rh.getTipoHabitacion().getNombre()));

        long habitacionesPorTipoDefault = porTipo.isEmpty() ? totalHabitaciones : totalHabitaciones / Math.max(1, porTipo.size());

        List<OccupancyReportResponse.OccupancyByRoomType> porTipoHabitacion = porTipo.entrySet().stream()
                .map(e -> {
                    List<ReservaHabitacion> lista = e.getValue();
                    long noches = lista.stream().mapToLong(ReservaHabitacion::getNoches).sum();
                    BigDecimal ingresos = lista.stream().map(ReservaHabitacion::getSubtotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    long disponibles = habitacionesPorTipoDefault * dias;
                    BigDecimal pctOcup = disponibles == 0 ? BigDecimal.ZERO
                            : BigDecimal.valueOf(noches).multiply(CIEN)
                                .divide(BigDecimal.valueOf(disponibles), 1, RoundingMode.HALF_UP);
                    BigDecimal adr = noches == 0 ? BigDecimal.ZERO
                            : ingresos.divide(BigDecimal.valueOf(noches), 2, RoundingMode.HALF_UP);
                    BigDecimal revpar = disponibles == 0 ? BigDecimal.ZERO
                            : ingresos.divide(BigDecimal.valueOf(disponibles), 2, RoundingMode.HALF_UP);
                    return new OccupancyReportResponse.OccupancyByRoomType(
                            e.getKey(), habitacionesPorTipoDefault, disponibles, noches, pctOcup, adr, revpar);
                })
                .sorted(Comparator.comparing(OccupancyReportResponse.OccupancyByRoomType::nochesOcupadas).reversed())
                .toList();

        return new OccupancyReportResponse(ocupacionPromedio, adrPromedio, revparPromedio, serie, porTipoHabitacion);
    }

    // =====================================================================
    // DASHBOARD GERENCIAL
    // =====================================================================

    @Override
    public ManagementDashboardResponse buildManagementDashboard(ReportRangeRequest range) {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMesActual = hoy.withDayOfMonth(1);
        LocalDate inicioMesAnterior = inicioMesActual.minusMonths(1);
        LocalDate finMesAnterior = inicioMesActual.minusDays(1);

        ReportRangeRequest rangoMesActual = new ReportRangeRequest("CUSTOM", "DAY", inicioMesActual, hoy);
        ReportRangeRequest rangoMesAnterior = new ReportRangeRequest("CUSTOM", "DAY", inicioMesAnterior, finMesAnterior);

        RevenueReportResponse ingresosMesActual = buildRevenueReport(rangoMesActual);
        RevenueReportResponse ingresosMesAnterior = buildRevenueReport(rangoMesAnterior);
        OccupancyReportResponse ocupacionMesActual = buildOccupancyReport(rangoMesActual);
        OccupancyReportResponse ocupacionMesAnterior = buildOccupancyReport(rangoMesAnterior);
        ReservationsReportResponse reservasMesActual = buildReservationsReport(rangoMesActual);

        BigDecimal variacionIngresos = variacionPorcentual(
                ingresosMesAnterior.totalIngresos(), ingresosMesActual.totalIngresos());
        BigDecimal variacionOcupacion = variacionPorcentual(
                ocupacionMesAnterior.ocupacionPromedio(), ocupacionMesActual.ocupacionPromedio());

        long reservasActivas = reservaRepository.findByEstado(EstadoReserva.CONFIRMADA).size()
                + reservaRepository.findByEstado(EstadoReserva.CHECK_IN).size();

        long reservasPendientesPago = reservaRepository.findAll().stream()
                .filter(r -> r.getEstado() != EstadoReserva.CANCELADA && r.getEstado() != EstadoReserva.NO_SHOW)
                .filter(r -> r.getAdelanto().compareTo(r.getMontoTotal()) < 0)
                .count();

        ManagementDashboardResponse.ManagementKpis kpis = new ManagementDashboardResponse.ManagementKpis(
                ingresosMesActual.totalIngresos(),
                ingresosMesAnterior.totalIngresos(),
                variacionIngresos,
                ocupacionMesActual.ocupacionPromedio(),
                ocupacionMesAnterior.ocupacionPromedio(),
                variacionOcupacion,
                reservasActivas,
                reservasPendientesPago,
                ocupacionMesActual.adrPromedio(),
                ocupacionMesActual.revparPromedio(),
                reservasMesActual.totalCanceladas()
        );

        return new ManagementDashboardResponse(
                LocalDateTime.now(), kpis, ingresosMesActual, reservasMesActual, ocupacionMesActual);
    }

    // =====================================================================
    // EXPORTAR CSV
    // =====================================================================

    @Override
    public byte[] exportar(ExportReporteRequest request) {
        ReportRangeRequest range = request.toRangeRequest();
        String csv = switch (request.tipo() == null ? "" : request.tipo().toLowerCase()) {
            case "ingresos" -> buildIngresosCSV(buildRevenueReport(range));
            case "reservas" -> buildReservasCSV(buildReservationsReport(range));
            case "ocupacion" -> buildOcupacionCSV(buildOccupancyReport(range));
            case "gerencial" -> buildGerencialCSV(buildManagementDashboard(range));
            default -> throw new IllegalArgumentException("Tipo de reporte no válido: " + request.tipo());
        };
        return csv.getBytes(StandardCharsets.UTF_8);
    }

    private String buildIngresosCSV(RevenueReportResponse r) {
        StringBuilder sb = new StringBuilder();
        sb.append("# REPORTE DE INGRESOS\n");
        sb.append("total_ingresos,total_anticipos,total_saldos,total_reembolsos,ingreso_promedio_diario\n");
        sb.append(r.totalIngresos()).append(',')
          .append(r.totalAnticipos()).append(',')
          .append(r.totalSaldos()).append(',')
          .append(r.totalReembolsos()).append(',')
          .append(r.ingresoPromedioDiario()).append('\n');
        sb.append('\n');
        sb.append("# SERIE DIARIA\n");
        sb.append("fecha,anticipos,saldos,reembolsos\n");
        for (RevenueReportResponse.RevenuePoint p : r.serie()) {
            sb.append(p.fecha()).append(',')
              .append(p.ingresosAnticipos()).append(',')
              .append(p.ingresosSaldos()).append(',')
              .append(p.reembolsos()).append('\n');
        }
        sb.append('\n');
        sb.append("# POR MÉTODO DE PAGO\n");
        sb.append("metodo_pago,monto,porcentaje\n");
        for (RevenueReportResponse.RevenueByMethod m : r.porMetodoPago()) {
            sb.append(escape(m.metodoPago())).append(',')
              .append(m.monto()).append(',')
              .append(m.porcentaje()).append('\n');
        }
        return sb.toString();
    }

    private String buildReservasCSV(ReservationsReportResponse r) {
        StringBuilder sb = new StringBuilder();
        sb.append("# REPORTE DE RESERVAS\n");
        sb.append("total_reservas,total_canceladas,tasa_cancelacion,estancia_promedio_noches\n");
        sb.append(r.totalReservas()).append(',')
          .append(r.totalCanceladas()).append(',')
          .append(r.tasaCancelacion()).append(',')
          .append(r.estanciaPromedioNoches()).append('\n');
        sb.append('\n');
        sb.append("# SERIE DIARIA\n");
        sb.append("fecha,nuevas,canceladas,check_ins,check_outs\n");
        for (ReservationsReportResponse.ReservationsPoint p : r.serie()) {
            sb.append(p.fecha()).append(',')
              .append(p.nuevas()).append(',')
              .append(p.canceladas()).append(',')
              .append(p.checkIns()).append(',')
              .append(p.checkOuts()).append('\n');
        }
        sb.append('\n');
        sb.append("# POR ESTADO\n");
        sb.append("estado,cantidad,porcentaje\n");
        for (ReservationsReportResponse.ReservationsByStatus s : r.porEstado()) {
            sb.append(escape(s.estado())).append(',')
              .append(s.cantidad()).append(',')
              .append(s.porcentaje()).append('\n');
        }
        return sb.toString();
    }

    private String buildOcupacionCSV(OccupancyReportResponse r) {
        StringBuilder sb = new StringBuilder();
        sb.append("# REPORTE DE OCUPACIÓN\n");
        sb.append("ocupacion_promedio,adr_promedio,revpar_promedio\n");
        sb.append(r.ocupacionPromedio()).append(',')
          .append(r.adrPromedio()).append(',')
          .append(r.revparPromedio()).append('\n');
        sb.append('\n');
        sb.append("# SERIE DIARIA\n");
        sb.append("fecha,habitaciones_total,habitaciones_ocupadas,porcentaje_ocupacion\n");
        for (OccupancyReportResponse.OccupancyPoint p : r.serie()) {
            sb.append(p.fecha()).append(',')
              .append(p.habitacionesTotal()).append(',')
              .append(p.habitacionesOcupadas()).append(',')
              .append(p.porcentajeOcupacion()).append('\n');
        }
        sb.append('\n');
        sb.append("# POR TIPO DE HABITACIÓN\n");
        sb.append("tipo_habitacion,habitaciones_total,noches_disponibles,noches_ocupadas,porcentaje_ocupacion,adr,revpar\n");
        for (OccupancyReportResponse.OccupancyByRoomType t : r.porTipoHabitacion()) {
            sb.append(escape(t.tipoHabitacion())).append(',')
              .append(t.habitacionesTotal()).append(',')
              .append(t.nochesDisponibles()).append(',')
              .append(t.nochesOcupadas()).append(',')
              .append(t.porcentajeOcupacion()).append(',')
              .append(t.adr()).append(',')
              .append(t.revpar()).append('\n');
        }
        return sb.toString();
    }

    private String buildGerencialCSV(ManagementDashboardResponse r) {
        ManagementDashboardResponse.ManagementKpis k = r.kpis();
        StringBuilder sb = new StringBuilder();
        sb.append("# DASHBOARD GERENCIAL\n");
        sb.append("indicador,valor\n");
        sb.append("ingresos_mes_actual,").append(k.ingresosMesActual()).append('\n');
        sb.append("ingresos_mes_anterior,").append(k.ingresosMesAnterior()).append('\n');
        sb.append("variacion_ingresos_%,").append(k.variacionIngresos()).append('\n');
        sb.append("ocupacion_actual_%,").append(k.ocupacionActual()).append('\n');
        sb.append("ocupacion_mes_anterior_%,").append(k.ocupacionMesAnterior()).append('\n');
        sb.append("variacion_ocupacion_%,").append(k.variacionOcupacion()).append('\n');
        sb.append("reservas_activas,").append(k.reservasActivas()).append('\n');
        sb.append("reservas_pendientes_pago,").append(k.reservasPendientesPago()).append('\n');
        sb.append("adr_actual,").append(k.adrActual()).append('\n');
        sb.append("revpar_actual,").append(k.revparActual()).append('\n');
        sb.append("cancelaciones_mes,").append(k.cancelacionesMes()).append('\n');
        return sb.toString();
    }

    private String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    private List<Pago> pagosEnRango(LocalDate desde, LocalDate hasta) {
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.atTime(23, 59, 59);
        return pagoRepository.findByFechaBetween(inicio, fin);
    }

    private List<Reserva> reservasCreadasEnRango(LocalDate desde, LocalDate hasta) {
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.atTime(23, 59, 59);
        return reservaRepository.findAll().stream()
                .filter(r -> !r.getFechaCreacion().isBefore(inicio) && !r.getFechaCreacion().isAfter(fin))
                .toList();
    }

    private boolean seSuperponeConRango(ReservaHabitacion rh, LocalDate desde, LocalDate hasta) {
        Reserva reserva = rh.getReserva();
        if (reserva == null) return false;
        LocalDate inicioEstancia = reserva.getFechaInicio();
        LocalDate finEstancia = reserva.getFechaFin();
        return !finEstancia.isBefore(desde) && !inicioEstancia.isAfter(hasta);
    }

    private boolean diaDentroDeEstancia(ReservaHabitacion rh, LocalDate dia) {
        Reserva reserva = rh.getReserva();
        if (reserva == null) return false;
        return !dia.isBefore(reserva.getFechaInicio()) && dia.isBefore(reserva.getFechaFin());
    }

    private BigDecimal sumarPorTipo(List<Pago> pagos, TipoPago tipo) {
        return pagos.stream()
                .filter(p -> p.getTipoPago() == tipo)
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal porcentaje(BigDecimal valor, BigDecimal base) {
        if (base == null || base.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return valor.multiply(CIEN).divide(base, 1, RoundingMode.HALF_UP);
    }

    private BigDecimal promedio(List<BigDecimal> valores) {
        if (valores.isEmpty()) return BigDecimal.ZERO;
        BigDecimal suma = valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return suma.divide(BigDecimal.valueOf(valores.size()), 1, RoundingMode.HALF_UP);
    }

    private BigDecimal variacionPorcentual(BigDecimal anterior, BigDecimal actual) {
        if (anterior == null || anterior.compareTo(BigDecimal.ZERO) == 0) {
            return actual != null && actual.compareTo(BigDecimal.ZERO) > 0 ? CIEN : BigDecimal.ZERO;
        }
        return actual.subtract(anterior).multiply(CIEN).divide(anterior, 1, RoundingMode.HALF_UP);
    }
}
