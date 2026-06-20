package com.sanfrancisco.api.modules.rrhh.service.interfaces;

import com.sanfrancisco.api.modules.rrhh.dto.request.AsignarHorarioRequest;
import com.sanfrancisco.api.modules.rrhh.dto.response.DetalleHorarioResponse;

import java.util.List;

public interface DetalleHorarioService {
    DetalleHorarioResponse asignar(AsignarHorarioRequest request);
    DetalleHorarioResponse update(Integer usuarioId, Integer horarioId, AsignarHorarioRequest request);
    void remover(Integer usuarioId, Integer horarioId);
    List<DetalleHorarioResponse> findByUsuarioId(Integer usuarioId);
}
