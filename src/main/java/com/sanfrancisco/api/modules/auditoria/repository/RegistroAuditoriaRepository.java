package com.sanfrancisco.api.modules.auditoria.repository;

import com.sanfrancisco.api.modules.auditoria.entity.RegistroAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface RegistroAuditoriaRepository
        extends JpaRepository<RegistroAuditoria, Integer>,
                JpaSpecificationExecutor<RegistroAuditoria> {

    /** Purga de retención: registros de auditoría anteriores al corte. */
    @Modifying
    @Query("DELETE FROM RegistroAuditoria r WHERE r.fecha < :corte")
    int deleteAnterioresA(@Param("corte") LocalDateTime corte);
}
