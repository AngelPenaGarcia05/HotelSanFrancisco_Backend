package com.sanfrancisco.api.modules.rrhh.repository;

import com.sanfrancisco.api.modules.rrhh.entity.PagoNomina;
import com.sanfrancisco.api.modules.rrhh.enums.EstadoNomina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoNominaRepository extends JpaRepository<PagoNomina, Integer>,
        JpaSpecificationExecutor<PagoNomina> {

    List<PagoNomina> findByUsuarioUsuarioId(Integer usuarioId);

    List<PagoNomina> findByPeriodo(String periodo);

    List<PagoNomina> findByEstado(EstadoNomina estado);
}
