package com.sanfrancisco.api.modules.notificaciones.repository;

import com.sanfrancisco.api.modules.notificaciones.entity.LogCorreo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface LogCorreoRepository extends JpaRepository<LogCorreo, Integer>,
        JpaSpecificationExecutor<LogCorreo> {

    /** Purga de retención: registros de correo anteriores al corte. */
    @Modifying
    @Query("DELETE FROM LogCorreo l WHERE l.enviadoEn < :corte")
    int deleteAnterioresA(@Param("corte") LocalDateTime corte);
}
