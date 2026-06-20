package com.sanfrancisco.api.modules.ventas.mapper;

import com.sanfrancisco.api.modules.recepcion.entity.Estancia;
import com.sanfrancisco.api.modules.recepcion.entity.Huesped;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.ventas.dto.request.CreateVentaRequest;
import com.sanfrancisco.api.modules.ventas.dto.request.UpdateVentaRequest;
import com.sanfrancisco.api.modules.ventas.dto.response.DetalleVentaResponse;
import com.sanfrancisco.api.modules.ventas.dto.response.VentaResponse;
import com.sanfrancisco.api.modules.ventas.entity.Venta;
import com.sanfrancisco.api.modules.ventas.enums.EstadoVenta;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Mapper manual. El monto_total se calcula como la suma de subtotales de detalles.
 * El estado inicial al crear es PENDIENTE; las transiciones de estado se gestionan
 * en el service vía operaciones dedicadas.
 */
@Component
public class VentaMapper {

    public Venta toEntity(CreateVentaRequest request,
                          Usuario usuario,
                          Estancia estancia,
                          Huesped huesped,
                          BigDecimal montoTotal) {
        return Venta.builder()
                .codigoVenta(request.codigoVenta())
                .tipoVenta(request.tipoVenta())
                .montoTotal(montoTotal)
                .fechaVenta(request.fechaVenta())
                .estado(EstadoVenta.PENDIENTE)
                .usuario(usuario)
                .estancia(estancia)
                .huesped(huesped)
                .build();
    }

    public void updateEntity(Venta target,
                             UpdateVentaRequest request,
                             Estancia estancia,
                             Huesped huesped) {
        if (request.tipoVenta() != null) target.setTipoVenta(request.tipoVenta());
        if (request.fechaVenta() != null) target.setFechaVenta(request.fechaVenta());
        if (estancia != null) target.setEstancia(estancia);
        if (huesped != null) target.setHuesped(huesped);
    }

    public VentaResponse toResponse(Venta entity, List<DetalleVentaResponse> detalles) {
        Usuario u = entity.getUsuario();
        Huesped h = entity.getHuesped();
        Estancia est = entity.getEstancia();
        return new VentaResponse(
                entity.getVentaId(),
                entity.getCodigoVenta(),
                entity.getTipoVenta(),
                entity.getMontoTotal(),
                entity.getFechaVenta(),
                entity.getEstado(),
                u != null ? u.getUsuarioId() : null,
                u != null ? buildNombreCompletoUsuario(u) : null,
                est != null ? est.getEstanciaId() : null,
                h != null ? h.getHuespedId() : null,
                h != null ? buildNombreCompletoHuesped(h) : null,
                detalles,
                entity.getFechaCreacion(),
                entity.getFechaModificacion()
        );
    }

    private String buildNombreCompletoUsuario(Usuario u) {
        StringBuilder sb = new StringBuilder(u.getNombre()).append(' ').append(u.getApellidoPaterno());
        if (u.getApellidoMaterno() != null && !u.getApellidoMaterno().isBlank()) {
            sb.append(' ').append(u.getApellidoMaterno());
        }
        return sb.toString();
    }

    private String buildNombreCompletoHuesped(Huesped h) {
        StringBuilder sb = new StringBuilder(h.getNombre()).append(' ').append(h.getApellidoPaterno());
        if (h.getApellidoMaterno() != null && !h.getApellidoMaterno().isBlank()) {
            sb.append(' ').append(h.getApellidoMaterno());
        }
        return sb.toString();
    }
}
