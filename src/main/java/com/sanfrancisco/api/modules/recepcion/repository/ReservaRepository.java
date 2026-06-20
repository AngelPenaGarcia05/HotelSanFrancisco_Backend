package com.sanfrancisco.api.modules.recepcion.repository;

import com.sanfrancisco.api.modules.recepcion.entity.Reserva;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Integer>,
        JpaSpecificationExecutor<Reserva> {

    Optional<Reserva> findByCodReserva(String codReserva);

    boolean existsByCodReserva(String codReserva);

    List<Reserva> findByEstado(EstadoReserva estado);

    List<Reserva> findByFechaInicioBetween(LocalDate inicio, LocalDate fin);

    List<Reserva> findByUsuarioUsuarioId(Integer usuarioId);

    Page<Reserva> findByUsuarioUsuarioId(Integer usuarioId, Pageable pageable);

    /**
     * Busca reservas activas del huésped principal que se solapen con el rango dado.
     * Se usa para detectar posibles duplicados antes de crear una nueva reserva.
     */
    @Query("""
            SELECT r FROM Reserva r
            JOIN DetalleHuesped dh ON dh.id.reservaId = r.reservaId
            WHERE dh.id.huespedId = :huespedId
              AND dh.esPrincipal = true
              AND r.estado NOT IN :estadosLibera
              AND r.fechaInicio < :fechaFin
              AND r.fechaFin    > :fechaInicio
            """)
    List<Reserva> findSolapadasPorHuespedPrincipal(
            @Param("huespedId") Integer huespedId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            @Param("estadosLibera") Collection<EstadoReserva> estadosLibera);
}
