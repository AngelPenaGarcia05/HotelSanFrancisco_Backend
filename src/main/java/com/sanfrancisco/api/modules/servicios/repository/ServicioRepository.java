package com.sanfrancisco.api.modules.servicios.repository;

import com.sanfrancisco.api.modules.servicios.entity.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Integer>,
        JpaSpecificationExecutor<Servicio> {

    List<Servicio> findByEstanciaEstanciaId(Integer estanciaId);

    List<Servicio> findByTipoServicioTipoServicioId(Integer tipoServicioId);
}
