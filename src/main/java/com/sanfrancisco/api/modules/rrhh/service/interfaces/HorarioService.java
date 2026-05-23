package com.sanfrancisco.api.modules.rrhh.service.interfaces;

import com.sanfrancisco.api.modules.rrhh.dto.request.CreateHorarioRequest;
import com.sanfrancisco.api.modules.rrhh.dto.request.UpdateHorarioRequest;
import com.sanfrancisco.api.modules.rrhh.dto.response.HorarioResponse;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HorarioService {
    HorarioResponse create(CreateHorarioRequest request);
    HorarioResponse update(Integer horarioId, UpdateHorarioRequest request);
    HorarioResponse findById(Integer horarioId);
    Page<HorarioResponse> search(String nombreTurno, EstadoActivo estado, Pageable pageable);
    List<HorarioResponse> findByEstado(EstadoActivo estado);
    void deleteById(Integer horarioId);
}
