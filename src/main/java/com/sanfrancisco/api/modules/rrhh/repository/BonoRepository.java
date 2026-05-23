package com.sanfrancisco.api.modules.rrhh.repository;

import com.sanfrancisco.api.modules.rrhh.entity.Bono;
import com.sanfrancisco.api.modules.rrhh.enums.EstadoBono;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BonoRepository extends JpaRepository<Bono, Integer>,
        JpaSpecificationExecutor<Bono> {

    List<Bono> findByUsuarioUsuarioId(Integer usuarioId);

    List<Bono> findByPagoNominaPagoNominaId(Integer pagoNominaId);

    List<Bono> findByEstado(EstadoBono estado);
}
