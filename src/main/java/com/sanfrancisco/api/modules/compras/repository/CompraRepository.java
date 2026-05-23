package com.sanfrancisco.api.modules.compras.repository;

import com.sanfrancisco.api.modules.compras.entity.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Integer>,
        JpaSpecificationExecutor<Compra> {

    List<Compra> findByProveedorProveedorId(Integer proveedorId);

    List<Compra> findByFechaCompraBetween(LocalDate inicio, LocalDate fin);
}
