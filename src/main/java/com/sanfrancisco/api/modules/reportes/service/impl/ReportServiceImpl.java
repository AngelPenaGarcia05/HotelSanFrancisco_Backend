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
