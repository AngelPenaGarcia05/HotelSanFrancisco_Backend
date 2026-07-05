package com.sanfrancisco.api.modules.seguridad.repository;

import com.sanfrancisco.api.modules.seguridad.entity.TokenRecuperacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TokenRecuperacionRepository extends JpaRepository<TokenRecuperacion, Integer> {

    Optional<TokenRecuperacion> findByTokenHash(String tokenHash);

    @Modifying
    @Query("DELETE FROM TokenRecuperacion t WHERE t.usuario.usuarioId = :usuarioId")
    void deleteByUsuarioId(Integer usuarioId);

    /** Purga de retención: tokens usados o ya expirados, creados antes del corte. */
    @Modifying
    @Query("""
            DELETE FROM TokenRecuperacion t
            WHERE t.fechaCreacion < :corte
              AND (t.usado = true OR t.fechaExpiracion < CURRENT_TIMESTAMP)
            """)
    int deleteAgotadosAntesDe(LocalDateTime corte);
}
