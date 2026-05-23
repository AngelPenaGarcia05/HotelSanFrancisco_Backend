package com.sanfrancisco.api.modules.recepcion.mapper;

import com.sanfrancisco.api.modules.recepcion.dto.request.CreateReservaRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.UpdateReservaRequest;
import com.sanfrancisco.api.modules.recepcion.dto.response.ReservaResponse;
import com.sanfrancisco.api.modules.recepcion.entity.Canal;
import com.sanfrancisco.api.modules.recepcion.entity.Reserva;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Mapper manual. Calcula monto_total = subtotal - descuento + impuesto.
 * El estado inicial al crear es PENDIENTE; las transiciones de estado se
 * gestionan en el service vía operaciones dedicadas.
 */
@Component
public class ReservaMapper {

    public Reserva toEntity(CreateReservaRequest request, Usuario usuario, Canal canal) {
        BigDecimal montoTotal = calcularMontoTotal(request.subtotal(), request.descuento(), request.impuesto());
        return Reserva.builder()
                .codReserva(request.codReserva())
                .fechaInicio(request.fechaInicio())
                .fechaFin(request.fechaFin())
                .nroAdultos(request.nroAdultos())
                .nroNinos(request.nroNinos())
                .subtotal(request.subtotal())
                .descuento(request.descuento())
                .adelanto(request.adelanto())
                .impuesto(request.impuesto())
                .montoTotal(montoTotal)
                .estado(EstadoReserva.PENDIENTE)
                .observaciones(request.observaciones())
                .usuario(usuario)
                .canal(canal)
                .build();
    }

    public void updateEntity(Reserva target, UpdateReservaRequest request, Canal canal) {
        if (request.fechaInicio() != null) target.setFechaInicio(request.fechaInicio());
        if (request.fechaFin() != null) target.setFechaFin(request.fechaFin());
        if (request.nroAdultos() != null) target.setNroAdultos(request.nroAdultos());
        if (request.nroNinos() != null) target.setNroNinos(request.nroNinos());
        if (request.subtotal() != null) target.setSubtotal(request.subtotal());
        if (request.descuento() != null) target.setDescuento(request.descuento());
        if (request.adelanto() != null) target.setAdelanto(request.adelanto());
        if (request.impuesto() != null) target.setImpuesto(request.impuesto());
        if (request.observaciones() != null) target.setObservaciones(request.observaciones());
        if (canal != null) target.setCanal(canal);

        target.setMontoTotal(calcularMontoTotal(target.getSubtotal(), target.getDescuento(), target.getImpuesto()));
    }

    public ReservaResponse toResponse(Reserva entity) {
        Usuario u = entity.getUsuario();
        Canal c = entity.getCanal();
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
                entity.getObservaciones(),
                u != null ? u.getUsuarioId() : null,
                u != null ? buildNombreCompleto(u) : null,
                c != null ? c.getCanalId() : null,
                c != null ? c.getNombre() : null,
                entity.getFechaCreacion(),
                entity.getFechaModificacion()
        );
    }

    private BigDecimal calcularMontoTotal(BigDecimal subtotal, BigDecimal descuento, BigDecimal impuesto) {
        BigDecimal s = Optional.ofNullable(subtotal).orElse(BigDecimal.ZERO);
        BigDecimal d = Optional.ofNullable(descuento).orElse(BigDecimal.ZERO);
        BigDecimal i = Optional.ofNullable(impuesto).orElse(BigDecimal.ZERO);
        return s.subtract(d).add(i);
    }

    private String buildNombreCompleto(Usuario u) {
        StringBuilder sb = new StringBuilder(u.getNombre()).append(' ').append(u.getApellidoPaterno());
        if (u.getApellidoMaterno() != null && !u.getApellidoMaterno().isBlank()) {
            sb.append(' ').append(u.getApellidoMaterno());
        }
        return sb.toString();
    }
}
