package com.sanfrancisco.api.modules.rrhh.mapper;

import com.sanfrancisco.api.modules.rrhh.dto.request.CreateAsistenciaRequest;
import com.sanfrancisco.api.modules.rrhh.dto.request.UpdateAsistenciaRequest;
import com.sanfrancisco.api.modules.rrhh.dto.response.AsistenciaResponse;
import com.sanfrancisco.api.modules.rrhh.entity.Asistencia;
import com.sanfrancisco.api.modules.seguridad.entity.Usuario;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

@Component
public class AsistenciaMapper {

    public Asistencia toEntity(CreateAsistenciaRequest request, Usuario usuario) {
        return Asistencia.builder()
                .fecha(request.fecha())
                .horaIngreso(request.horaIngreso())
                .tipo(request.tipo())
                .observaciones(request.observaciones())
                .usuario(usuario)
                .build();
    }

    public void updateEntity(Asistencia target, UpdateAsistenciaRequest request) {
        if (request.horaIngreso() != null) target.setHoraIngreso(request.horaIngreso());
        if (request.horaEgreso() != null) target.setHoraEgreso(request.horaEgreso());
        if (request.tipo() != null) target.setTipo(request.tipo());
        if (request.observaciones() != null) target.setObservaciones(request.observaciones());

        if (target.getHoraIngreso() != null && target.getHoraEgreso() != null) {
            Duration duration = Duration.between(target.getHoraIngreso(), target.getHoraEgreso());
            long minutes = duration.toMinutes();
            if (minutes < 0) {
                // Si el turno cruza la medianoche (ej. entra a las 22:00, sale a las 06:00)
                minutes += 24 * 60;
            }
            BigDecimal horas = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            target.setHorasTrabajadas(horas);
        }
    }

    public AsistenciaResponse toResponse(Asistencia entity) {
        Usuario u = entity.getUsuario();
        return new AsistenciaResponse(
                entity.getAsistenciaId(),
                entity.getFecha(),
                entity.getHoraIngreso(),
                entity.getHoraEgreso(),
                entity.getHorasTrabajadas(),
                entity.getTipo(),
                entity.getObservaciones(),
                u != null ? u.getUsuarioId() : null,
                u != null ? buildNombreCompleto(u) : null,
                entity.getFechaCreacion(),
                entity.getFechaModificacion()
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
