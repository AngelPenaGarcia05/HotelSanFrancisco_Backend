package com.sanfrancisco.api.modules.recepcion.repository;

import com.sanfrancisco.api.modules.recepcion.entity.ReservaHabitacion;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReservaHabitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservaHabitacionRepository extends JpaRepository<ReservaHabitacion, Integer>,
        JpaSpecificationExecutor<ReservaHabitacion> {

    List<ReservaHabitacion> findByReservaReservaId(Integer reservaId);

    List<ReservaHabitacion> findByHabitacionHabitacionId(Integer habitacionId);

    List<ReservaHabitacion> findByEstado(EstadoReservaHabitacion estado);
}
