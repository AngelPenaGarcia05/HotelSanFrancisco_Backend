package com.sanfrancisco.api.modules.rrhh.repository;

import com.sanfrancisco.api.modules.rrhh.entity.DetalleHorario;
import com.sanfrancisco.api.modules.rrhh.entity.DetalleHorarioPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleHorarioRepository extends JpaRepository<DetalleHorario, DetalleHorarioPK>,
        JpaSpecificationExecutor<DetalleHorario> {

    List<DetalleHorario> findByIdUsuarioId(Integer usuarioId);

    List<DetalleHorario> findByIdHorarioId(Integer horarioId);

    List<DetalleHorario> findByIdUsuarioIdAndDiaSemana(Integer usuarioId, Integer diaSemana);
}
