package com.sanfrancisco.api.modules.auditoria.service;

import com.sanfrancisco.api.modules.auditoria.dto.request.AuditoriaFilterRequest;
import com.sanfrancisco.api.modules.auditoria.dto.response.RegistroAuditoriaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditoriaService {

    /**
     * Persiste un registro de auditoría en una transacción independiente,
     * de modo que sobreviva aunque la transacción de negocio haya hecho rollback.
     * Nunca propaga excepciones: un fallo de auditoría no debe romper el flujo.
     */
    void registrar(AuditoriaCommand command);

    Page<RegistroAuditoriaResponse> search(AuditoriaFilterRequest filter, Pageable pageable);
}
