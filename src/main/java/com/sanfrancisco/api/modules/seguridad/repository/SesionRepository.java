package com.sanfrancisco.api.modules.seguridad.repository;

import com.sanfrancisco.api.modules.seguridad.entity.Sesion;
import com.sanfrancisco.api.modules.seguridad.enums.EstadoSesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SesionRepository extends JpaRepository<Sesion, Integer>,
        JpaSpecificationExecutor<Sesion> {

    Optional<Sesion> findByTokenHash(String tokenHash);

    List<Sesion> findByUsuarioUsuarioIdAndEstado(Integer usuarioId, EstadoSesion estado);
}
