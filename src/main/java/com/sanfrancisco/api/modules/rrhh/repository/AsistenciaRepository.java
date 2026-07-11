package com.sanfrancisco.api.modules.rrhh.repository;

import com.sanfrancisco.api.modules.rrhh.entity.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Integer>,
        JpaSpecificationExecutor<Asistencia> {

    List<Asistencia> findByUsuarioUsuarioId(Integer usuarioId);

    List<Asistencia> findByFechaBetween(LocalDate inicio, LocalDate fin);

    List<Asistencia> findByUsuarioUsuarioIdAndFechaBetween(Integer usuarioId, LocalDate inicio, LocalDate fin);

    /** Marca del empleado para una fecha (regla: una marca por día). */
    Optional<Asistencia> findByUsuarioUsuarioIdAndFecha(Integer usuarioId, LocalDate fecha);

    boolean existsByUsuarioUsuarioIdAndFecha(Integer usuarioId, LocalDate fecha);

    List<Asistencia> findByUsuarioUsuarioIdOrderByFechaDesc(Integer usuarioId);
}
