package com.sanfrancisco.api.modules.compras.repository;

import com.sanfrancisco.api.modules.compras.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Integer>,
        JpaSpecificationExecutor<Proveedor> {

    Optional<Proveedor> findByRucNitCif(String rucNitCif);

    boolean existsByRucNitCif(String rucNitCif);
}
