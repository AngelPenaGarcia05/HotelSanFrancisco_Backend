package com.sanfrancisco.api.modules.seguridad.repository;

import com.sanfrancisco.api.modules.seguridad.entity.Sesion;
import com.sanfrancisco.api.modules.seguridad.enums.EstadoSesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SesionRepository extends JpaRepository<Sesion, Integer>,
        JpaSpecificationExecutor<Sesion> {

    Optional<Sesion> findByTokenHash(String tokenHash);

    List<Sesion> findByUsuarioUsuarioIdAndEstado(Integer usuarioId, EstadoSesion estado);

    /** Purga de retención: sesiones cuya expiración quedó antes del corte. */
    @Modifying
    @Query("DELETE FROM Sesion s WHERE s.fechaExpiracion < :corte")
    int deleteExpiradasAntesDe(@Param("corte") LocalDateTime corte);
}
