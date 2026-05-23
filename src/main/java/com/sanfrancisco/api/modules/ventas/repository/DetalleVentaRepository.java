package com.sanfrancisco.api.modules.ventas.repository;

import com.sanfrancisco.api.modules.ventas.entity.DetalleVenta;
import com.sanfrancisco.api.modules.ventas.entity.DetalleVentaPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, DetalleVentaPK>,
        JpaSpecificationExecutor<DetalleVenta> {

    List<DetalleVenta> findByIdVentaId(Integer ventaId);

    List<DetalleVenta> findByIdProductoId(Integer productoId);
}
