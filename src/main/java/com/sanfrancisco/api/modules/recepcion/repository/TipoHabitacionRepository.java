package com.sanfrancisco.api.modules.recepcion.repository;

import com.sanfrancisco.api.modules.recepcion.entity.TipoHabitacion;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TipoHabitacionRepository extends JpaRepository<TipoHabitacion, Integer>,
        JpaSpecificationExecutor<TipoHabitacion> {

    List<TipoHabitacion> findByEstado(EstadoActivo estado);

    List<TipoHabitacion> findByCapacidadMaximaGreaterThanEqual(Integer capacidad);
}
