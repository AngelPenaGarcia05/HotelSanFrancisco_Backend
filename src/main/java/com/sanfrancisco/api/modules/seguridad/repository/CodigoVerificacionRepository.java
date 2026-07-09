package com.sanfrancisco.api.modules.seguridad.repository;

import com.sanfrancisco.api.modules.seguridad.entity.CodigoVerificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CodigoVerificacionRepository extends JpaRepository<CodigoVerificacion, Integer> {

    Optional<CodigoVerificacion> findByUsuarioUsuarioId(Integer usuarioId);

    @Modifying
    @Query("DELETE FROM CodigoVerificacion c WHERE c.usuario.usuarioId = :usuarioId")
    void deleteByUsuarioId(Integer usuarioId);

    /** Purga de retención: códigos usados o ya expirados, creados antes del corte. */
    @Modifying
    @Query("""
            DELETE FROM CodigoVerificacion c
            WHERE c.fechaCreacion < :corte
              AND (c.usado = true OR c.fechaExpiracion < CURRENT_TIMESTAMP)
            """)
    int deleteAgotadosAntesDe(LocalDateTime corte);
}
