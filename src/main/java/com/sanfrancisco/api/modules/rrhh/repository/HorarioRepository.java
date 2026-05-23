package com.sanfrancisco.api.modules.rrhh.repository;

import com.sanfrancisco.api.modules.rrhh.entity.Horario;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HorarioRepository extends JpaRepository<Horario, Integer>,
        JpaSpecificationExecutor<Horario> {

    List<Horario> findByEstado(EstadoActivo estado);
}
