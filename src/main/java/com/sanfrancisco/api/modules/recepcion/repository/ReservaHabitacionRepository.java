package com.sanfrancisco.api.modules.recepcion.repository;

import com.sanfrancisco.api.modules.recepcion.entity.ReservaHabitacion;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReservaHabitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface ReservaHabitacionRepository extends JpaRepository<ReservaHabitacion, Integer>,
        JpaSpecificationExecutor<ReservaHabitacion> {

    List<ReservaHabitacion> findByReservaReservaId(Integer reservaId);

    List<ReservaHabitacion> findByReservaReservaIdIn(Collection<Integer> reservaIds);

    List<ReservaHabitacion> findByHabitacionHabitacionId(Integer habitacionId);

    List<ReservaHabitacion> findByEstado(EstadoReservaHabitacion estado);

    /** IDs de habitaciones que se solapan con el rango dado (para buscar disponibles). */
    @Query("""
            SELECT rh.habitacion.habitacionId FROM ReservaHabitacion rh
            WHERE rh.reserva.estado NOT IN :estadosLibera
              AND rh.estado <> com.sanfrancisco.api.modules.recepcion.enums.EstadoReservaHabitacion.LIBERADA
              AND rh.reserva.fechaInicio < :fechaFin
              AND rh.reserva.fechaFin   > :fechaInicio
            """)
    List<Integer> findHabitacionIdsSolapadas(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            @Param("estadosLibera") Collection<EstadoReserva> estadosLibera);

    /** Comprueba si una habitación concreta tiene solape, excluyendo opcionalmente la propia reserva (para update). */
    @Query("""
            SELECT COUNT(rh) > 0 FROM ReservaHabitacion rh
            WHERE rh.habitacion.habitacionId = :habId
              AND rh.reserva.estado NOT IN :estadosLibera
              AND rh.estado <> com.sanfrancisco.api.modules.recepcion.enums.EstadoReservaHabitacion.LIBERADA
              AND rh.reserva.fechaInicio < :fechaFin
              AND rh.reserva.fechaFin   > :fechaInicio
              AND (:excluirReservaId IS NULL OR rh.reserva.reservaId <> :excluirReservaId)
            """)
    boolean existeSolapamiento(
            @Param("habId") Integer habitacionId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            @Param("estadosLibera") Collection<EstadoReserva> estadosLibera,
            @Param("excluirReservaId") Integer excluirReservaId);
}
