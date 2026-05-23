package com.sanfrancisco.api.modules.inventario.repository;

import com.sanfrancisco.api.modules.inventario.entity.CategoriaProducto;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaProductoRepository extends JpaRepository<CategoriaProducto, Integer>,
        JpaSpecificationExecutor<CategoriaProducto> {

    List<CategoriaProducto> findByEstado(EstadoActivo estado);

    boolean existsByNombre(String nombre);
}
