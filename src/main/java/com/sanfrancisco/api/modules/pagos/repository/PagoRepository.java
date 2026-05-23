package com.sanfrancisco.api.modules.pagos.repository;

import com.sanfrancisco.api.modules.pagos.entity.Pago;
import com.sanfrancisco.api.modules.pagos.enums.TipoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Integer>,
        JpaSpecificationExecutor<Pago> {

    List<Pago> findByReservaReservaId(Integer reservaId);

    List<Pago> findByVentaVentaId(Integer ventaId);

    List<Pago> findByTipoPago(TipoPago tipoPago);

    List<Pago> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);
}
