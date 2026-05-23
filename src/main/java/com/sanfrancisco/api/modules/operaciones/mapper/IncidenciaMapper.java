package com.sanfrancisco.api.modules.operaciones.mapper;

import com.sanfrancisco.api.modules.operaciones.dto.request.CreateIncidenciaRequest;
import com.sanfrancisco.api.modules.operaciones.dto.request.UpdateIncidenciaRequest;
import com.sanfrancisco.api.modules.operaciones.dto.response.IncidenciaResponse;
import com.sanfrancisco.api.modules.operaciones.entity.Incidencia;
import com.sanfrancisco.api.modules.operaciones.enums.EstadoIncidencia;
import com.sanfrancisco.api.modules.recepcion.entity.ReservaHabitacion;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Mapper manual. El estado inicial al crear es ABIERTA; las transiciones de estado
 * se gestionan en el service. Si no se envía fechaReporte se usa el instante actual.
 */
@Component
public class IncidenciaMapper {

    public Incidencia toEntity(CreateIncidenciaRequest request, Usuario usuario, ReservaHabitacion reservaHabitacion) {
        return Incidencia.builder()
                .descripcion(request.descripcion())
                .fechaReporte(Optional.ofNullable(request.fechaReporte()).orElse(LocalDateTime.now()))
                .prioridad(request.prioridad())
                .estado(EstadoIncidencia.ABIERTA)
                .usuario(usuario)
                .reservaHabitacion(reservaHabitacion)
                .build();
    }

    public void updateEntity(Incidencia target, UpdateIncidenciaRequest request, ReservaHabitacion reservaHabitacion) {
        if (request.descripcion() != null) target.setDescripcion(request.descripcion());
        if (request.prioridad() != null) target.setPrioridad(request.prioridad());
        if (reservaHabitacion != null) target.setReservaHabitacion(reservaHabitacion);
    }

    public IncidenciaResponse toResponse(Incidencia entity) {
        Usuario u = entity.getUsuario();
        ReservaHabitacion rh = entity.getReservaHabitacion();
        return new IncidenciaResponse(
                entity.getIncidenciaId(),
                entity.getDescripcion(),
                entity.getFechaReporte(),
                entity.getFechaResolucion(),
                entity.getPrioridad(),
                entity.getSolucion(),
                entity.getEstado(),
                u != null ? u.getUsuarioId() : null,
                u != null ? buildNombre(u) : null,
                rh != null ? rh.getReservaHabitacionId() : null,
                entity.getFechaCreacion(),
                entity.getFechaModificacion()
        );
    }

    private String buildNombre(Usuario u) {
        StringBuilder sb = new StringBuilder();
        if (u.getNombre() != null) sb.append(u.getNombre());
        if (u.getApellidoPaterno() != null) sb.append(' ').append(u.getApellidoPaterno());
        return sb.toString().trim();
    }
}
