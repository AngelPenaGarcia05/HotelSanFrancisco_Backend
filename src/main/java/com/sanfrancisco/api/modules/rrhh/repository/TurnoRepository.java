package com.sanfrancisco.api.modules.rrhh.repository;

import com.sanfrancisco.api.modules.rrhh.entity.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Integer> {

    Optional<Turno> findByUsuarioUsuarioIdAndFecha(Integer usuarioId, LocalDate fecha);

    boolean existsByUsuarioUsuarioIdAndFecha(Integer usuarioId, LocalDate fecha);

    List<Turno> findByFechaBetweenOrderByFechaAscUsuarioUsuarioIdAsc(LocalDate desde, LocalDate hasta);

    List<Turno> findByUsuarioUsuarioIdAndFechaBetweenOrderByFechaAsc(Integer usuarioId, LocalDate desde, LocalDate hasta);
}
