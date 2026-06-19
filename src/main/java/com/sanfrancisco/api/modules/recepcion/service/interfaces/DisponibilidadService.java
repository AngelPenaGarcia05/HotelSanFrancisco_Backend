package com.sanfrancisco.api.modules.recepcion.service.interfaces;

import com.sanfrancisco.api.modules.recepcion.dto.request.ReservaHabitacionRequest;
import com.sanfrancisco.api.modules.recepcion.dto.response.HabitacionResponse;

import java.time.LocalDate;
import java.util.List;

public interface DisponibilidadService {

    /**
     * Devuelve las habitaciones libres en el rango [fechaInicio, fechaFin).
     * Excluye automáticamente las que están en MANTENIMIENTO o BLOQUEADA.
     *
     * @param pisoFiltro si no es null, filtra por piso
     */
    List<HabitacionResponse> buscarDisponibles(LocalDate fechaInicio, LocalDate fechaFin, Integer pisoFiltro);

    /**
     * Valida que cada habitación de la lista esté libre en el rango dado.
     * Lanza ConflictException si alguna está ocupada.
     *
     * @param excluirReservaId ID de la reserva a ignorar en la comprobación (útil en update); null al crear
     */
    void validarDisponibilidad(List<ReservaHabitacionRequest> habitaciones,
                               LocalDate fechaInicio,
                               LocalDate fechaFin,
                               Integer excluirReservaId);
}
