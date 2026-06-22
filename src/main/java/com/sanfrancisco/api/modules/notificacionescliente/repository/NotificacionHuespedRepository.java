package com.sanfrancisco.api.modules.notificacionescliente.repository;

import com.sanfrancisco.api.modules.notificacionescliente.entity.NotificacionHuesped;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificacionHuespedRepository extends JpaRepository<NotificacionHuesped, Integer> {

    Page<NotificacionHuesped> findByUsuarioUsuarioIdOrderByFechaCreacionDesc(Integer usuarioId, Pageable pageable);

    @Modifying
    @Query("""
            UPDATE NotificacionHuesped n
            SET n.leida = true
            WHERE n.usuario.usuarioId = :usuarioId
              AND n.leida = false
            """)
    int marcarTodasLeidas(@Param("usuarioId") Integer usuarioId);
}
