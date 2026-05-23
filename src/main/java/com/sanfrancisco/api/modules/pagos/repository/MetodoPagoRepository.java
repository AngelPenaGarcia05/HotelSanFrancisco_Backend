package com.sanfrancisco.api.modules.pagos.repository;

import com.sanfrancisco.api.modules.pagos.entity.MetodoPago;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Integer>,
        JpaSpecificationExecutor<MetodoPago> {

    List<MetodoPago> findByEstado(EstadoActivo estado);

    List<MetodoPago> findByRequiereComprobanteTrue();
}
