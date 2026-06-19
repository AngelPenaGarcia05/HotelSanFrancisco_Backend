package com.sanfrancisco.api.modules.recepcion.repository;

import com.sanfrancisco.api.modules.recepcion.entity.Habitacion;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoHabitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface HabitacionRepository extends JpaRepository<Habitacion, Integer>,
        JpaSpecificationExecutor<Habitacion> {

    Optional<Habitacion> findByNumero(String numero);

    boolean existsByNumero(String numero);

    List<Habitacion> findByEstado(EstadoHabitacion estado);

    List<Habitacion> findByEstadoIn(Collection<EstadoHabitacion> estados);

    List<Habitacion> findByPiso(Integer piso);

    List<Habitacion> findByPisoAndEstado(Integer piso, EstadoHabitacion estado);

    List<Habitacion> findAllByOrderByPisoAscNumeroAsc();

    @Query("SELECT h FROM Habitacion h " +
           "LEFT JOIN FETCH h.tipoHabitacion t " +
           "WHERE h.estado = 'DISPONIBLE' " +
           "AND h.tipoHabitacion IS NOT NULL " +
           "AND (:minCapacidad IS NULL OR t.capacidadMaxima >= :minCapacidad) " +
           "AND h.habitacionId NOT IN (" +
           "  SELECT rh.habitacion.habitacionId FROM ReservaHabitacion rh " +
           "  WHERE rh.reserva.estado NOT IN ('CANCELADA', 'CHECK_OUT', 'NO_SHOW') " +
           "  AND rh.reserva.fechaInicio < :fechaFin " +
           "  AND rh.reserva.fechaFin > :fechaInicio" +
           ") ORDER BY h.piso ASC, h.numero ASC")
    List<Habitacion> findDisponiblesParaFechas(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            @Param("minCapacidad") Integer minCapacidad
    );
}
