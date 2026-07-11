package com.sanfrancisco.api.modules.rrhh.repository;

import com.sanfrancisco.api.modules.rrhh.entity.DetalleHorario;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleHorarioRepository extends JpaRepository<DetalleHorario, Integer>,
        JpaSpecificationExecutor<DetalleHorario> {

    List<DetalleHorario> findByUsuarioUsuarioId(Integer usuarioId);

    List<DetalleHorario> findByHorarioHorarioId(Integer horarioId);

    List<DetalleHorario> findByUsuarioUsuarioIdAndDiaSemana(Integer usuarioId, Integer diaSemana);

    /** Verifica si el empleado ya tiene un turno ACTIVO asignado ese día (regla: 1 turno/día). */
    boolean existsByUsuarioUsuarioIdAndDiaSemanaAndEstado(Integer usuarioId, Integer diaSemana, EstadoActivo estado);

    /** Plantilla vigente: todas las asignaciones en un estado dado (p. ej. ACTIVO). */
    List<DetalleHorario> findByEstado(EstadoActivo estado);
}
