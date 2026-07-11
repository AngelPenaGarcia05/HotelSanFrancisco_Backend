package com.sanfrancisco.api.modules.rrhh.service.interfaces;

import com.sanfrancisco.api.modules.rrhh.dto.request.AsignarHorarioRequest;
import com.sanfrancisco.api.modules.rrhh.dto.response.DetalleHorarioResponse;

import java.util.List;

public interface DetalleHorarioService {
    DetalleHorarioResponse asignar(AsignarHorarioRequest request);
    DetalleHorarioResponse update(Integer detalleHorarioId, AsignarHorarioRequest request);
    void remover(Integer detalleHorarioId);
    List<DetalleHorarioResponse> findByUsuarioId(Integer usuarioId);
}
