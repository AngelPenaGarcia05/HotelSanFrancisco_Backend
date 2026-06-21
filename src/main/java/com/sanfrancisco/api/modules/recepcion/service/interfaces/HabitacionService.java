package com.sanfrancisco.api.modules.recepcion.service.interfaces;

import com.sanfrancisco.api.modules.recepcion.dto.request.CheckInRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.CheckOutRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.CreateHabitacionRequest;
import com.sanfrancisco.api.modules.recepcion.dto.request.UpdateHabitacionRequest;
import com.sanfrancisco.api.modules.recepcion.dto.response.CalendarioHabitacionResponse;
import com.sanfrancisco.api.modules.recepcion.dto.response.CheckOutLiquidacionResponse;
import com.sanfrancisco.api.modules.recepcion.dto.response.HabitacionResponse;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoHabitacion;

import java.time.LocalDate;
import java.util.List;

public interface HabitacionService {

    HabitacionResponse create(CreateHabitacionRequest request);

    HabitacionResponse update(Integer habitacionId, UpdateHabitacionRequest request);

    HabitacionResponse findById(Integer habitacionId);

    List<HabitacionResponse> findAll();

    List<HabitacionResponse> findByEstado(EstadoHabitacion estado);

    List<HabitacionResponse> findByPiso(Integer piso);

    List<HabitacionResponse> findLimpieza();

    HabitacionResponse cambiarEstado(Integer habitacionId, EstadoHabitacion nuevoEstado);

    void deleteById(Integer habitacionId);

    /** Ejecuta el proceso de check-in para una reserva confirmada. */
    HabitacionResponse checkIn(CheckInRequest request);

    /** Ejecuta el proceso de check-out con liquidación automática. */
    CheckOutLiquidacionResponse checkOut(CheckOutRequest request);

    /** Marca una habitación en LIMPIEZA como DISPONIBLE. */
    HabitacionResponse registrarLimpiezaCompletada(Integer habitacionId);

    /** Vista calendario: todas las habitaciones con sus reservas solapadas en el rango. */
    List<CalendarioHabitacionResponse> getCalendario(LocalDate fechaInicio, LocalDate fechaFin);
}
