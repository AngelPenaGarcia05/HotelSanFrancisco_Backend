package com.sanfrancisco.api.modules.inventario.repository;

import com.sanfrancisco.api.modules.inventario.entity.Producto;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer>,
        JpaSpecificationExecutor<Producto> {

    List<Producto> findByEstado(EstadoActivo estado);

    List<Producto> findByCategoriaProductoCategoriaProductoId(Integer categoriaId);

    @Query("SELECT p FROM Producto p WHERE p.stockActual <= p.stockMinimo AND p.estado = 'ACTIVO'")
    List<Producto> findProductosBajoStock();

    List<Producto> findByPrecioVentaBetween(BigDecimal min, BigDecimal max);
}
