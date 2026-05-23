package com.sanfrancisco.api.modules.seguridad.repository;

import com.sanfrancisco.api.modules.seguridad.entity.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermisoRepository extends JpaRepository<Permiso, Integer>,
        JpaSpecificationExecutor<Permiso> {

    Optional<Permiso> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
