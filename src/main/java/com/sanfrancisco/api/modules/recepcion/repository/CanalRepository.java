package com.sanfrancisco.api.modules.recepcion.repository;

import com.sanfrancisco.api.modules.recepcion.entity.Canal;
import com.sanfrancisco.api.shared.enums.EstadoActivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CanalRepository extends JpaRepository<Canal, Integer>,
        JpaSpecificationExecutor<Canal> {

    List<Canal> findByEstado(EstadoActivo estado);

    Optional<Canal> findByNombreIgnoreCase(String nombre);
}
