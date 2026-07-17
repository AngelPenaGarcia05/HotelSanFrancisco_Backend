package com.sanfrancisco.api.modules.recepcion.repository;

import com.sanfrancisco.api.modules.recepcion.entity.Reserva;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    List<Reserva> findByFechaCreacionBetween(LocalDateTime inicio, LocalDateTime fin);

    List<Reserva> findByEstadoAndFechaInicioBetween(EstadoReserva estado, LocalDate inicio, LocalDate fin);

    long countByEstadoIn(Collection<EstadoReserva> estados);

    /**
     * Reservas con saldo pendiente (adelanto < monto total), excluyendo los
     * estados que liberan la deuda. Sustituye al conteo en memoria del
     * dashboard/reporte gerencial, que cargaba la tabla completa.
     */
    @Query("""
            SELECT COUNT(r) FROM Reserva r
            WHERE r.estado NOT IN :estadosExcluidos
              AND r.adelanto < r.montoTotal
            """)
    long countPendientesDePago(@Param("estadosExcluidos") Collection<EstadoReserva> estadosExcluidos);

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

    /**
     * Pre-reservas web con pago online no completado: PENDIENTE, de un canal
     * dado, creadas antes del límite y sin ningún pago registrado. Son las
     * candidatas a expirar para liberar la habitación.
     */
    @Query("""
            SELECT r FROM Reserva r
            WHERE r.estado = com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva.PENDIENTE
              AND r.canal.nombre = :canalNombre
              AND r.fechaCreacion < :limite
              AND NOT EXISTS (SELECT 1 FROM Pago p WHERE p.reserva = r)
            """)
    List<Reserva> findPendientesWebExpiradas(
            @Param("canalNombre") String canalNombre,
            @Param("limite") LocalDateTime limite);
}
