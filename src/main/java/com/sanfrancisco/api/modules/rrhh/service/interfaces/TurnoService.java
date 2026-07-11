package com.sanfrancisco.api.modules.rrhh.service.interfaces;

import com.sanfrancisco.api.modules.rrhh.dto.request.ActualizarTurnoRequest;
import com.sanfrancisco.api.modules.rrhh.dto.request.GenerarTurnosRequest;
import com.sanfrancisco.api.modules.rrhh.dto.response.GenerarTurnosResponse;
import com.sanfrancisco.api.modules.rrhh.dto.response.TurnoResponse;

import java.time.LocalDate;
import java.util.List;

public interface TurnoService {
    /** Genera turnos fechados desde la plantilla semanal para el rango indicado. Idempotente. */
    GenerarTurnosResponse generar(GenerarTurnosRequest request);

    List<TurnoResponse> listarPorRango(LocalDate desde, LocalDate hasta);

    List<TurnoResponse> listarPorUsuario(Integer usuarioId, LocalDate desde, LocalDate hasta);

    /** Edición puntual: cobertura, cambio de horas o de estado. */
    TurnoResponse actualizar(Integer turnoId, ActualizarTurnoRequest request);

    /** Cancela un turno (feriado, vacaciones, etc.). */
    void cancelar(Integer turnoId);
}
