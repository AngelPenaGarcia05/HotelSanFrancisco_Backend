package com.sanfrancisco.api.modules.servicios.repository;

import com.sanfrancisco.api.modules.servicios.entity.TipoServicio;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TipoServicioRepository extends JpaRepository<TipoServicio, Integer>,
        JpaSpecificationExecutor<TipoServicio> {

    List<TipoServicio> findByEstado(EstadoActivo estado);
}
