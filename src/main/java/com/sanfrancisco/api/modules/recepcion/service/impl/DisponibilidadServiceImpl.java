package com.sanfrancisco.api.modules.recepcion.service.impl;

import com.sanfrancisco.api.modules.recepcion.dto.request.ReservaHabitacionRequest;
import com.sanfrancisco.api.modules.recepcion.dto.response.HabitacionResponse;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoHabitacion;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;
import com.sanfrancisco.api.modules.recepcion.mapper.HabitacionMapper;
import com.sanfrancisco.api.modules.recepcion.repository.HabitacionRepository;
import com.sanfrancisco.api.modules.recepcion.repository.ReservaHabitacionRepository;
import com.sanfrancisco.api.modules.recepcion.service.interfaces.DisponibilidadService;
import com.sanfrancisco.api.shared.exception.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class DisponibilidadServiceImpl implements DisponibilidadService {

    // Estados que liberan la habitación: la reserva ya no la ocupa
    private static final Set<EstadoReserva> ESTADOS_LIBERA = Set.of(
            EstadoReserva.CANCELADA,
            EstadoReserva.NO_SHOW
    );

    // Estados físicos que impiden cualquier reserva futura
    private static final Set<EstadoHabitacion> ESTADOS_BLOQUEADOS = Set.of(
            EstadoHabitacion.MANTENIMIENTO,
            EstadoHabitacion.BLOQUEADA
    );

    private final HabitacionRepository habitacionRepository;
    private final ReservaHabitacionRepository reservaHabitacionRepository;
    private final HabitacionMapper habitacionMapper;

    public DisponibilidadServiceImpl(HabitacionRepository habitacionRepository,
                                     ReservaHabitacionRepository reservaHabitacionRepository,
                                     HabitacionMapper habitacionMapper) {
        this.habitacionRepository = habitacionRepository;
        this.reservaHabitacionRepository = reservaHabitacionRepository;
        this.habitacionMapper = habitacionMapper;
    }

    @Override
    public List<HabitacionResponse> buscarDisponibles(LocalDate fechaInicio, LocalDate fechaFin, Integer pisoFiltro) {
        validarRango(fechaInicio, fechaFin);

        List<Integer> ocupadas = reservaHabitacionRepository
                .findHabitacionIdsSolapadas(fechaInicio, fechaFin, ESTADOS_LIBERA);

        return habitacionRepository.findAll().stream()
                .filter(h -> !ESTADOS_BLOQUEADOS.contains(h.getEstado()))
                .filter(h -> !ocupadas.contains(h.getHabitacionId()))
                .filter(h -> pisoFiltro == null || pisoFiltro.equals(h.getPiso()))
                .sorted((a, b) -> {
                    int cmp = Integer.compare(a.getPiso(), b.getPiso());
                    return cmp != 0 ? cmp : a.getNumero().compareTo(b.getNumero());
                })
                .map(habitacionMapper::toResponse)
                .toList();
    }

    @Override
    public void validarDisponibilidad(List<ReservaHabitacionRequest> habitaciones,
                                      LocalDate fechaInicio,
                                      LocalDate fechaFin,
                                      Integer excluirReservaId) {
        validarRango(fechaInicio, fechaFin);

        for (ReservaHabitacionRequest req : habitaciones) {
            boolean ocupada = reservaHabitacionRepository.existeSolapamiento(
                    req.habitacionId(), fechaInicio, fechaFin, ESTADOS_LIBERA, excluirReservaId);
            if (ocupada) {
                throw new ConflictException(
                        "La habitación con id " + req.habitacionId()
                        + " ya tiene una reserva activa entre " + fechaInicio + " y " + fechaFin);
            }
        }
    }

    private void validarRango(LocalDate inicio, LocalDate fin) {
        if (!fin.isAfter(inicio)) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la fecha de inicio");
        }
    }
}
