package com.sanfrancisco.api.modules.operaciones.repository;

import com.sanfrancisco.api.modules.operaciones.entity.Incidencia;
import com.sanfrancisco.api.modules.operaciones.enums.EstadoIncidencia;
import com.sanfrancisco.api.modules.operaciones.enums.PrioridadIncidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidenciaRepository extends JpaRepository<Incidencia, Integer>,
        JpaSpecificationExecutor<Incidencia> {

    List<Incidencia> findByEstado(EstadoIncidencia estado);

    List<Incidencia> findByPrioridad(PrioridadIncidencia prioridad);

    List<Incidencia> findByUsuarioUsuarioId(Integer usuarioId);

    List<Incidencia> findByReservaHabitacionReservaHabitacionId(Integer reservaHabitacionId);

    List<Incidencia> findByReservaHabitacionHabitacionHabitacionId(Integer habitacionId);

    List<Incidencia> findByEstadoAndPrioridad(EstadoIncidencia estado, PrioridadIncidencia prioridad);
}
