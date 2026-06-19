package com.sanfrancisco.api.modules.auditoria.repository;

import com.sanfrancisco.api.modules.auditoria.entity.RegistroAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RegistroAuditoriaRepository
        extends JpaRepository<RegistroAuditoria, Integer>,
                JpaSpecificationExecutor<RegistroAuditoria> {
}
