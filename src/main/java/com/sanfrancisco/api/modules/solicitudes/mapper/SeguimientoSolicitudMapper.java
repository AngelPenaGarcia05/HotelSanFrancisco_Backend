package com.sanfrancisco.api.modules.solicitudes.mapper;

import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import com.sanfrancisco.api.modules.solicitudes.dto.response.SeguimientoSolicitudResponse;
import com.sanfrancisco.api.modules.solicitudes.entity.SeguimientoSolicitud;
import org.springframework.stereotype.Component;

@Component
public class SeguimientoSolicitudMapper {

    public SeguimientoSolicitudResponse toResponse(SeguimientoSolicitud entity) {
        Usuario r = entity.getResponsable();
        return new SeguimientoSolicitudResponse(
                entity.getSeguimientoId(),
                entity.getSolicitud() != null ? entity.getSolicitud().getSolicitudId() : null,
                entity.getFechaAccion(),
                entity.getAccion(),
                entity.getEstadoAnterior(),
                entity.getEstadoNuevo(),
                entity.getObservacion(),
                r != null ? r.getUsuarioId() : null,
                r != null ? buildNombre(r) : null
        );
    }

    public static String buildNombre(Usuario u) {
        StringBuilder sb = new StringBuilder();
        if (u.getNombre() != null) sb.append(u.getNombre());
        if (u.getApellidoPaterno() != null) sb.append(' ').append(u.getApellidoPaterno());
        return sb.toString().trim();
    }
}
