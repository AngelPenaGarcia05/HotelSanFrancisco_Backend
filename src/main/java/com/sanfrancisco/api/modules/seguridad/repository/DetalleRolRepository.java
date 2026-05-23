package com.sanfrancisco.api.modules.seguridad.repository;

import com.sanfrancisco.api.modules.seguridad.entity.DetalleRol;
import com.sanfrancisco.api.modules.seguridad.entity.DetalleRolPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleRolRepository extends JpaRepository<DetalleRol, DetalleRolPK>,
        JpaSpecificationExecutor<DetalleRol> {

    List<DetalleRol> findByRolRolId(Integer rolId);

    List<DetalleRol> findByPermisoPermisoId(Integer permisoId);
}
