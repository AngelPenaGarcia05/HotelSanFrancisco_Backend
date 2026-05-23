package com.sanfrancisco.api.modules.rrhh.mapper;

import com.sanfrancisco.api.modules.rrhh.dto.request.AsignarHorarioRequest;
import com.sanfrancisco.api.modules.rrhh.dto.response.DetalleHorarioResponse;
import com.sanfrancisco.api.modules.rrhh.entity.DetalleHorario;
import com.sanfrancisco.api.modules.rrhh.entity.DetalleHorarioPK;
import com.sanfrancisco.api.modules.rrhh.entity.Horario;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class DetalleHorarioMapper {

    public DetalleHorario toEntity(AsignarHorarioRequest request, Usuario usuario, Horario horario) {
        DetalleHorarioPK id = new DetalleHorarioPK(usuario.getUsuarioId(), horario.getHorarioId());
        return DetalleHorario.builder()
                .id(id)
                .usuario(usuario)
                .horario(horario)
                .diaSemana(request.diaSemana())
                .estado(EstadoActivo.ACTIVO)
                .fechaVigenciaInicio(request.fechaVigenciaInicio())
                .fechaVigenciaFin(request.fechaVigenciaFin())
                .build();
    }

    public void updateEntity(DetalleHorario target, AsignarHorarioRequest request) {
        if (request.diaSemana() != null) target.setDiaSemana(request.diaSemana());
        if (request.fechaVigenciaInicio() != null) target.setFechaVigenciaInicio(request.fechaVigenciaInicio());
        target.setFechaVigenciaFin(request.fechaVigenciaFin()); // Puede ser null
    }

    public DetalleHorarioResponse toResponse(DetalleHorario entity) {
        Usuario u = entity.getUsuario();
        Horario h = entity.getHorario();
        return new DetalleHorarioResponse(
                u != null ? u.getUsuarioId() : null,
                u != null ? buildNombreCompleto(u) : null,
                h != null ? h.getHorarioId() : null,
                h != null ? h.getNombreTurno() : null,
                entity.getDiaSemana(),
                entity.getEstado(),
                entity.getFechaVigenciaInicio(),
                entity.getFechaVigenciaFin()
        );
    }

    private String buildNombreCompleto(Usuario u) {
        StringBuilder sb = new StringBuilder(u.getNombre()).append(' ').append(u.getApellidoPaterno());
        if (u.getApellidoMaterno() != null && !u.getApellidoMaterno().isBlank()) {
            sb.append(' ').append(u.getApellidoMaterno());
        }
        return sb.toString();
    }
}
