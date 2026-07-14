package com.sanfrancisco.api.modules.recepcion.repository;

import com.sanfrancisco.api.modules.recepcion.entity.Habitacion;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoHabitacion;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
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

    /**
     * Inventario de habitaciones por tipo: [nombreTipo, cantidad]. Se usa como
     * denominador real de "habitaciones disponibles por tipo" en el reporte de
     * ocupación (estándar STR/USALI), en lugar de repartir el total a partes
     * iguales entre los tipos. Excluye las habitaciones fuera de servicio
     * (estados OOO) recibidas en {@code estadosExcluir}.
     */
    @Query("SELECT t.nombre, COUNT(h) FROM Habitacion h JOIN h.tipoHabitacion t "
            + "WHERE h.estado NOT IN :estadosExcluir GROUP BY t.nombre")
    List<Object[]> contarPorTipo(@Param("estadosExcluir") Collection<EstadoHabitacion> estadosExcluir);

    boolean existsByNumero(String numero);

    List<Habitacion> findByEstado(EstadoHabitacion estado);

    long countByEstado(EstadoHabitacion estado);

    long countByEstadoNotIn(Collection<EstadoHabitacion> estados);

    List<Habitacion> findByEstadoIn(Collection<EstadoHabitacion> estados);

    List<Habitacion> findByPiso(Integer piso);

    List<Habitacion> findByPisoAndEstado(Integer piso, EstadoHabitacion estado);

    List<Habitacion> findAllByOrderByPisoAscNumeroAsc();

    /**
     * Carga las habitaciones con lock pesimista (SELECT ... FOR UPDATE) para
     * serializar reservas concurrentes sobre las mismas habitaciones: la segunda
     * transacción espera aquí hasta el commit de la primera y luego su validación
     * de solapamiento ya ve la reserva recién creada. El ORDER BY por id fija un
     * orden de adquisición de locks consistente y evita deadlocks cruzados.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT h FROM Habitacion h WHERE h.habitacionId IN :ids ORDER BY h.habitacionId")
    List<Habitacion> findAllByIdForUpdate(@Param("ids") Collection<Integer> ids);

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
