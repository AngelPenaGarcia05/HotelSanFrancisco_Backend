package com.sanfrancisco.api.modules.recepcion.mapper;

import com.sanfrancisco.api.modules.pagos.entity.Pago;
import com.sanfrancisco.api.modules.pagos.enums.TipoPago;
import com.sanfrancisco.api.modules.pagos.repository.PagoRepository;
import com.sanfrancisco.api.modules.recepcion.dto.ReservaMontos;
import com.sanfrancisco.api.modules.recepcion.dto.request.CreateReservaRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.UpdateReservaRequest;
import com.sanfrancisco.api.modules.recepcion.dto.response.DetalleHuespedResponse;
import com.sanfrancisco.api.modules.recepcion.dto.response.ReservaHabitacionResponse;
import com.sanfrancisco.api.modules.recepcion.dto.response.ReservaResponse;
import com.sanfrancisco.api.modules.recepcion.entity.Canal;
import com.sanfrancisco.api.modules.recepcion.entity.DetalleHuesped;
import com.sanfrancisco.api.modules.recepcion.entity.Reserva;
import com.sanfrancisco.api.modules.recepcion.entity.ReservaHabitacion;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.shared.utils.DateTimeUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Mapper manual para Reserva.
 * El subtotal de la reserva se calcula en el service a partir de las habitaciones;
 * montoTotal = subtotal - descuento + impuesto.
 */
@Component
public class ReservaMapper {

    private final ReservaHabitacionMapper habitacionMapper;
    private final DetalleHuespedMapper huespedMapper;
    private final PagoRepository pagoRepository;

    public ReservaMapper(ReservaHabitacionMapper habitacionMapper, DetalleHuespedMapper huespedMapper,
                         PagoRepository pagoRepository) {
        this.habitacionMapper = habitacionMapper;
        this.huespedMapper = huespedMapper;
        this.pagoRepository = pagoRepository;
    }

    public Reserva toEntity(CreateReservaRequest request, Usuario usuario, Canal canal,
                            ReservaMontos montos) {
        return Reserva.builder()
                .codReserva(montos.codReserva())
                .fechaInicio(request.fechaInicio())
                .fechaFin(request.fechaFin())
                .nroAdultos(request.nroAdultos())
                .nroNinos(request.nroNinos())
                .subtotal(montos.subtotal())
                .descuento(montos.descuento())
                .adelanto(montos.adelanto())
                .impuesto(montos.impuesto())
                .montoTotal(montos.montoTotal())
                .modalidadPago(montos.modalidadPago())
                .estado(EstadoReserva.PENDIENTE)
                .observaciones(request.observaciones())
                .usuario(usuario)
                .canal(canal)
                .build();
    }

    /**
     * Aplica los campos no-monetarios presentes en el request (update parcial) y
     * fija TODOS los montos a partir de {@link ReservaMontos} ya recalculados por el
     * service. Los montos del request (impuesto, adelanto) se ignoran a propósito.
     */
    public void updateEntity(Reserva target, UpdateReservaRequest request, Canal canal,
                             ReservaMontos montos) {
        if (request.fechaInicio() != null) target.setFechaInicio(request.fechaInicio());
        if (request.fechaFin() != null) target.setFechaFin(request.fechaFin());
        if (request.nroAdultos() != null) target.setNroAdultos(request.nroAdultos());
        if (request.nroNinos() != null) target.setNroNinos(request.nroNinos());
        if (request.observaciones() != null) target.setObservaciones(request.observaciones());
        if (canal != null) target.setCanal(canal);

        target.setSubtotal(montos.subtotal());
        target.setDescuento(montos.descuento());
        target.setImpuesto(montos.impuesto());
        target.setMontoTotal(montos.montoTotal());
        target.setAdelanto(montos.adelanto());
        target.setModalidadPago(montos.modalidadPago());
    }

    public ReservaResponse toResponse(Reserva entity,
                                      List<ReservaHabitacion> habitaciones,
                                      List<DetalleHuesped> huespedes,
                                      Integer estanciaId) {
        Usuario u = entity.getUsuario();
        Canal c = entity.getCanal();

        List<ReservaHabitacionResponse> habResp = habitaciones != null
                ? habitaciones.stream().map(habitacionMapper::toResponse).toList()
                : Collections.emptyList();

        List<DetalleHuespedResponse> huespResp = huespedes != null
                ? huespedes.stream().map(huespedMapper::toResponse).toList()
                : Collections.emptyList();

        return new ReservaResponse(
                entity.getReservaId(),
                entity.getCodReserva(),
                entity.getFechaInicio(),
                entity.getFechaFin(),
                entity.getMontoTotal(),
                entity.getEstado(),
                entity.getNroAdultos(),
                entity.getNroNinos(),
                entity.getSubtotal(),
                entity.getDescuento(),
                entity.getAdelanto(),
                entity.getImpuesto(),
                calcularSaldoPendiente(entity),
                entity.getModalidadPago(),
                entity.getObservaciones(),
                u != null ? u.getUsuarioId() : null,
                u != null ? buildNombreCompleto(u) : null,
                c != null ? c.getCanalId() : null,
                c != null ? c.getNombre() : null,
                estanciaId,
                habResp,
                huespResp,
                entity.getFechaCreacion(),
                entity.getFechaModificacion(),
                entity.getFechaInicio() != null
                        && entity.getFechaInicio().isEqual(DateTimeUtils.today())
        );
    }

    public ReservaResponse toResponse(Reserva entity,
                                      List<ReservaHabitacion> habitaciones,
                                      List<DetalleHuesped> huespedes) {
        return toResponse(entity, habitaciones, huespedes, null);
    }

    /** Versión resumida para search/paginación: sin cargar listas anidadas. */
    public ReservaResponse toResponse(Reserva entity) {
        return toResponse(entity, null, null, null);
    }

    /**
     * Calcula el saldo pendiente real de una reserva: montoTotal − totalPagado,
     * donde totalPagado es la suma de pagos registrados (excluyendo REEMBOLSOS).
     * Si la reserva ya está confirmada o activa y no posee filas de pago en la BD,
     * se asume el adelanto nominal como mínimo pagado.
     */
    private BigDecimal calcularSaldoPendiente(Reserva entity) {
        if (entity == null) return BigDecimal.ZERO;
        BigDecimal t = Optional.ofNullable(entity.getMontoTotal()).orElse(BigDecimal.ZERO);
        if (entity.getReservaId() == null) {
            return t;
        }
        BigDecimal totalPagado = pagoRepository.findByReservaReservaId(entity.getReservaId()).stream()
                .filter(p -> p.getTipoPago() != TipoPago.REEMBOLSO)
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPagado.compareTo(BigDecimal.ZERO) == 0
                && entity.getEstado() != EstadoReserva.PENDIENTE
                && entity.getEstado() != EstadoReserva.CANCELADA
                && entity.getAdelanto() != null) {
            totalPagado = entity.getAdelanto();
        }

        return t.subtract(totalPagado).max(BigDecimal.ZERO);
    }

    private String buildNombreCompleto(Usuario u) {
        StringBuilder sb = new StringBuilder(u.getNombre()).append(' ').append(u.getApellidoPaterno());
        if (u.getApellidoMaterno() != null && !u.getApellidoMaterno().isBlank()) {
            sb.append(' ').append(u.getApellidoMaterno());
        }
        return sb.toString();
    }
}
