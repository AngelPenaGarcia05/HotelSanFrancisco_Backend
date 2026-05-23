package com.sanfrancisco.api.modules.recepcion.repository;

import com.sanfrancisco.api.modules.recepcion.entity.Reserva;
import com.sanfrancisco.api.modules.recepcion.enums.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Integer>,
        JpaSpecificationExecutor<Reserva> {

    Optional<Reserva> findByCodReserva(String codReserva);

    boolean existsByCodReserva(String codReserva);

    List<Reserva> findByEstado(EstadoReserva estado);

    List<Reserva> findByFechaInicioBetween(LocalDate inicio, LocalDate fin);

    List<Reserva> findByUsuarioUsuarioId(Integer usuarioId);
}
