package com.sanfrancisco.api.modules.recepcion.service.interfaces;

import com.sanfrancisco.api.modules.recepcion.dto.request.CreateTipoHabitacionRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.UpdateTipoHabitacionRequest;
import com.sanfrancisco.api.modules.recepcion.dto.response.TipoHabitacionResponse;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TipoHabitacionService {

    TipoHabitacionResponse create(CreateTipoHabitacionRequest request);

    TipoHabitacionResponse update(Integer id, UpdateTipoHabitacionRequest request);

    TipoHabitacionResponse findById(Integer id);

    Page<TipoHabitacionResponse> findAll(Pageable pageable);

    List<TipoHabitacionResponse> findByEstado(EstadoActivo estado);

    void deleteById(Integer id);
}
