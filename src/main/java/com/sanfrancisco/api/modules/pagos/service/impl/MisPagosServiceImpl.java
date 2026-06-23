package com.sanfrancisco.api.modules.pagos.service.impl;

import com.sanfrancisco.api.exception.ResourceNotFoundException;
import com.sanfrancisco.api.modules.pagos.dto.response.MiPagoResponse;
import com.sanfrancisco.api.modules.pagos.entity.Pago;
import com.sanfrancisco.api.modules.pagos.enums.TipoPago;
import com.sanfrancisco.api.modules.pagos.repository.PagoRepository;
import com.sanfrancisco.api.modules.pagos.service.interfaces.MisPagosService;
import com.sanfrancisco.api.modules.recepcion.entity.Reserva;
import com.sanfrancisco.api.modules.recepcion.entity.ReservaHabitacion;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;
import com.sanfrancisco.api.modules.recepcion.repository.ReservaHabitacionRepository;
import com.sanfrancisco.api.modules.recepcion.repository.ReservaRepository;
import com.sanfrancisco.api.modules.seguridad.security.UserPrincipal;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class MisPagosServiceImpl implements MisPagosService {

    private final ReservaRepository reservaRepository;
    private final PagoRepository pagoRepository;
    private final ReservaHabitacionRepository reservaHabitacionRepository;

    public MisPagosServiceImpl(ReservaRepository reservaRepository,
                               PagoRepository pagoRepository,
                               ReservaHabitacionRepository reservaHabitacionRepository) {
        this.reservaRepository = reservaRepository;
        this.pagoRepository = pagoRepository;
        this.reservaHabitacionRepository = reservaHabitacionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MiPagoResponse> getMisPagos() {
        Integer usuarioId = currentUserId();
        List<Reserva> reservas = reservaRepository.findByUsuarioUsuarioId(usuarioId);

        List<MiPagoResponse> filas = new ArrayList<>();

        for (Reserva reserva : reservas) {
            agregarFilasDeReserva(reserva, filas);
        }

        // Orden por fecha desc (las cadenas ISO yyyy-MM-dd ordenan cronológicamente)
        filas.sort(Comparator.comparing(MiPagoResponse::fecha).reversed());
        return filas;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MiPagoResponse> getMisPagosPorReserva(Integer reservaId) {
        Integer usuarioId = currentUserId();
        // Se valida propiedad devolviendo 404 (no 403) para no revelar la existencia
        // de reservas ajenas a un cliente que manipule el id en la URL.
        Reserva reserva = reservaRepository.findById(reservaId)
                .filter(r -> r.getUsuario() != null
                        && usuarioId.equals(r.getUsuario().getUsuarioId()))
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", reservaId));

        List<MiPagoResponse> filas = new ArrayList<>();
        agregarFilasDeReserva(reserva, filas);
        filas.sort(Comparator.comparing(MiPagoResponse::fecha).reversed());
        return filas;
    }

    /** Construye las filas PAGADO y PENDIENTE de una reserva y las agrega a {@code filas}. */
    private void agregarFilasDeReserva(Reserva reserva, List<MiPagoResponse> filas) {
        String habitacion = describirHabitacion(reserva.getReservaId());
        List<Pago> pagos = pagoRepository.findByReservaReservaId(reserva.getReservaId());

        // Filas PAGADO: un registro por cada pago real
        for (Pago pago : pagos) {
            filas.add(new MiPagoResponse(
                    pago.getPagoId(),
                    reserva.getReservaId(),
                    reserva.getCodReserva(),
                    habitacion,
                    "PAGADO",
                    pago.getFecha().toLocalDate().toString(),
                    pago.getMetodoPago() != null ? pago.getMetodoPago().getNombre() : null,
                    pago.getMonto(),
                    "/api/v1/mis-facturas/" + pago.getPagoId()
            ));
        }

        // Fila PENDIENTE: saldo restante de reservas activas
        if (reserva.getEstado() != EstadoReserva.CANCELADA
                && reserva.getEstado() != EstadoReserva.NO_SHOW) {
            BigDecimal totalPagado = pagos.stream()
                    .filter(p -> p.getTipoPago() != TipoPago.REEMBOLSO)
                    .map(Pago::getMonto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal saldo = reserva.getMontoTotal().subtract(totalPagado);
            if (saldo.compareTo(BigDecimal.ZERO) > 0) {
                filas.add(new MiPagoResponse(
                        null,
                        reserva.getReservaId(),
                        reserva.getCodReserva(),
                        habitacion,
                        "PENDIENTE",
                        reserva.getFechaInicio().toString(),
                        null,
                        saldo,
                        null
                ));
            }
        }
    }

    private String describirHabitacion(Integer reservaId) {
        List<ReservaHabitacion> asignaciones = reservaHabitacionRepository.findByReservaReservaId(reservaId);
        if (asignaciones.isEmpty()) {
            return "Reserva";
        }
        ReservaHabitacion rh = asignaciones.get(0);
        String tipo = rh.getTipoHabitacion() != null ? rh.getTipoHabitacion().getNombre() : "Habitación";
        String numero = rh.getHabitacion() != null ? rh.getHabitacion().getNumero() : "";
        return numero.isBlank() ? tipo : tipo + " - " + numero;
    }

    private Integer currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BadCredentialsException("No autenticado");
        }
        return principal.userId();
    }
}
