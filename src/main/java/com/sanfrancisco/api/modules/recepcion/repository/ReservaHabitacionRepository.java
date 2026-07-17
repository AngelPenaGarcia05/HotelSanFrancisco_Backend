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

    /** Asignaciones con habitación y tipo inicializados (para lectores fuera de transacción). */
    @Query("""
            SELECT rh FROM ReservaHabitacion rh
            JOIN FETCH rh.habitacion
            JOIN FETCH rh.tipoHabitacion
            WHERE rh.reserva.reservaId = :reservaId
            """)
    List<ReservaHabitacion> findByReservaIdFetchHabitacion(@Param("reservaId") Integer reservaId);

    List<ReservaHabitacion> findByReservaReservaIdIn(Collection<Integer> reservaIds);

    List<ReservaHabitacion> findByHabitacionHabitacionId(Integer habitacionId);

    List<ReservaHabitacion> findByEstado(EstadoReservaHabitacion estado);

    /** Asignaciones vigentes de reservas en un estado dado (p.ej. CHECK_IN), excluyendo las liberadas. */
    List<ReservaHabitacion> findByReservaEstadoAndEstadoNot(EstadoReserva reservaEstado,
                                                            EstadoReservaHabitacion estadoExcluido);

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

    /**
     * Asignaciones cuya estancia se solapa con el rango dado, contando solo
     * habitaciones efectivamente vendidas/ocupadas (estándar STR/USALI): se
     * excluyen reservas CANCELADA y NO_SHOW, que no representan room-nights
     * vendidas. Semántica de solapamiento: fechaFin >= desde y fechaInicio <= hasta.
     */
    @Query("""
            SELECT rh FROM ReservaHabitacion rh
            WHERE rh.reserva.estado NOT IN (
                    com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva.CANCELADA,
                    com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva.NO_SHOW)
              AND rh.reserva.fechaFin    >= :desde
              AND rh.reserva.fechaInicio <= :hasta
            """)
    List<ReservaHabitacion> findSolapadasConRango(
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

    /** Todas las asignaciones cuya reserva se solapa con el rango dado (para el calendario). */
    @Query("""
            SELECT rh FROM ReservaHabitacion rh
            WHERE rh.reserva.estado NOT IN :estadosExcluir
              AND rh.reserva.fechaInicio < :fechaFin
              AND rh.reserva.fechaFin    > :fechaInicio
            """)
    List<ReservaHabitacion> findEnRango(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            @Param("estadosExcluir") Collection<EstadoReserva> estadosExcluir);
}
