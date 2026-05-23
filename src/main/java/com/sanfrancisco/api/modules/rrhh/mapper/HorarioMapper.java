package com.sanfrancisco.api.modules.rrhh.mapper;

import com.sanfrancisco.api.modules.rrhh.dto.request.CreateHorarioRequest;
import com.sanfrancisco.api.modules.rrhh.dto.request.UpdateHorarioRequest;
import com.sanfrancisco.api.modules.rrhh.dto.response.HorarioResponse;
import com.sanfrancisco.api.modules.rrhh.entity.Horario;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import org.springframework.stereotype.Component;

@Component
public class HorarioMapper {

    public Horario toEntity(CreateHorarioRequest request) {
        return Horario.builder()
                .nombreTurno(request.nombreTurno())
                .horaEntrada(request.horaEntrada())
                .horaSalida(request.horaSalida())
                .estado(EstadoActivo.ACTIVO)
                .build();
    }

    public void updateEntity(Horario target, UpdateHorarioRequest request) {
        if (request.nombreTurno() != null) target.setNombreTurno(request.nombreTurno());
        if (request.horaEntrada() != null) target.setHoraEntrada(request.horaEntrada());
        if (request.horaSalida() != null) target.setHoraSalida(request.horaSalida());
        if (request.estado() != null) target.setEstado(request.estado());
    }

    public HorarioResponse toResponse(Horario entity) {
        return new HorarioResponse(
                entity.getHorarioId(),
                entity.getNombreTurno(),
                entity.getHoraEntrada(),
                entity.getHoraSalida(),
                entity.getEstado(),
                entity.getFechaCreacion(),
                entity.getFechaModificacion()
        );
    }
}
