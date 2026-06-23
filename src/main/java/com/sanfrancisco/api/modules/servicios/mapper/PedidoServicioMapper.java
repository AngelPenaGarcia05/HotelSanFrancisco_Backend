package com.sanfrancisco.api.modules.servicios.mapper;

import com.sanfrancisco.api.modules.recepcion.entity.Estancia;
import com.sanfrancisco.api.modules.recepcion.entity.Reserva;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.servicios.dto.response.PedidoServicioResponse;
import com.sanfrancisco.api.modules.servicios.entity.PedidoServicio;
import com.sanfrancisco.api.modules.servicios.entity.Servicio;
import com.sanfrancisco.api.modules.servicios.entity.TipoServicio;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PedidoServicioMapper {

    public PedidoServicioResponse toResponse(PedidoServicio p) {
        TipoServicio t = p.getTipoServicio();
        Estancia e = p.getEstancia();
        Reserva r = e != null ? e.getReserva() : null;
        Servicio s = p.getServicio();

        BigDecimal costoBase = t != null ? t.getCostoBase() : null;
        BigDecimal subtotalEstimado = (costoBase != null && p.getCantidad() != null)
                ? p.getCantidad().multiply(costoBase)
                : null;

        return new PedidoServicioResponse(
                p.getPedidoServicioId(),
                t != null ? t.getTipoServicioId() : null,
                t != null ? t.getNombre() : null,
                costoBase,
                p.getCantidad(),
                subtotalEstimado,
                p.getObservaciones(),
                p.getEstado(),
                p.getMotivoRespuesta(),
                e != null ? e.getEstanciaId() : null,
                r != null ? r.getCodReserva() : null,
                nombreSolicitante(r),
                s != null ? s.getServicioId() : null,
                p.getFechaCreacion(),
                p.getFechaRespuesta()
        );
    }

    private String nombreSolicitante(Reserva r) {
        if (r == null || r.getUsuario() == null) {
            return null;
        }
        Usuario u = r.getUsuario();
        String apellido = u.getApellidoPaterno() != null ? " " + u.getApellidoPaterno() : "";
        return (u.getNombre() != null ? u.getNombre() : "") + apellido;
    }
}
