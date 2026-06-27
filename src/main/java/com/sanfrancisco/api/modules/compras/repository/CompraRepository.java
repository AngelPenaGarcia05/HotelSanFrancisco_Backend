package com.sanfrancisco.api.modules.compras.repository;

import com.sanfrancisco.api.modules.compras.entity.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Integer>,
        JpaSpecificationExecutor<Compra> {

    List<Compra> findByProveedorProveedorId(Integer proveedorId);

    List<Compra> findByFechaCompraBetween(LocalDate inicio, LocalDate fin);

    /**
     * Suma de montoTotal excluyendo SIEMPRE las compras ANULADA, con filtros
     * opcionales (null = sin filtrar). Usado por el endpoint de stats.
     */
    @Query("""
           SELECT COALESCE(SUM(c.montoTotal), 0) FROM Compra c
           WHERE c.estado <> com.sanfrancisco.api.modules.compras.enums.EstadoCompra.ANULADA
             AND (:proveedorId IS NULL OR c.proveedor.proveedorId = :proveedorId)
             AND (:desde IS NULL OR c.fechaCompra >= :desde)
             AND (:hasta IS NULL OR c.fechaCompra <= :hasta)
           """)
    BigDecimal sumMontoNoAnuladas(@Param("proveedorId") Integer proveedorId,
                                  @Param("desde") LocalDate desde,
                                  @Param("hasta") LocalDate hasta);
}
