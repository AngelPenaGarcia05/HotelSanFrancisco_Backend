package com.sanfrancisco.api.modules.rrhh.repository;

import com.sanfrancisco.api.modules.rrhh.entity.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Integer>,
        JpaSpecificationExecutor<Asistencia> {

    List<Asistencia> findByUsuarioUsuarioId(Integer usuarioId);

    List<Asistencia> findByFechaBetween(LocalDate inicio, LocalDate fin);

    List<Asistencia> findByUsuarioUsuarioIdAndFechaBetween(Integer usuarioId, LocalDate inicio, LocalDate fin);
}
