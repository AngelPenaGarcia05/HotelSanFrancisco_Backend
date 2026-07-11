package com.sanfrancisco.api.modules.rrhh.mapper;

import com.sanfrancisco.api.modules.rrhh.dto.response.TurnoResponse;
import com.sanfrancisco.api.modules.rrhh.entity.Horario;
import com.sanfrancisco.api.modules.rrhh.entity.Turno;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class TurnoMapper {

    public TurnoResponse toResponse(Turno t) {
        Usuario u = t.getUsuario();
        Horario h = t.getHorario();
        return new TurnoResponse(
                t.getTurnoId(),
                u != null ? u.getUsuarioId() : null,
                u != null ? buildNombreCompleto(u) : null,
                h != null ? h.getHorarioId() : null,
                h != null ? h.getNombreTurno() : null,
                t.getFecha(),
                t.getHoraInicio(),
                t.getHoraFin(),
                t.getEstado(),
                t.getOrigen()
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
