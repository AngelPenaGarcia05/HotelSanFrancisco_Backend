package com.sanfrancisco.api.modules.compras.repository;

import com.sanfrancisco.api.modules.compras.entity.DetalleCompra;
import com.sanfrancisco.api.modules.compras.entity.DetalleCompraPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleCompraRepository extends JpaRepository<DetalleCompra, DetalleCompraPK>,
        JpaSpecificationExecutor<DetalleCompra> {

    List<DetalleCompra> findByIdCompraId(Integer compraId);

    List<DetalleCompra> findByIdProductoId(Integer productoId);
}
