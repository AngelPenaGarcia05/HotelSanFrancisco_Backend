package com.sanfrancisco.api.modules.ventas.repository;

import com.sanfrancisco.api.modules.ventas.entity.Venta;
import com.sanfrancisco.api.modules.ventas.enums.EstadoVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer>,
        JpaSpecificationExecutor<Venta> {

    Optional<Venta> findByCodigoVenta(String codigoVenta);

    boolean existsByCodigoVenta(String codigoVenta);

    List<Venta> findByEstado(EstadoVenta estado);

    List<Venta> findByFechaVentaBetween(LocalDateTime inicio, LocalDateTime fin);

    List<Venta> findByEstanciaEstanciaId(Integer estanciaId);
}
