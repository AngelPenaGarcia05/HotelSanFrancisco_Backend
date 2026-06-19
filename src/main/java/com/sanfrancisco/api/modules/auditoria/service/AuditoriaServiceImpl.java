package com.sanfrancisco.api.modules.auditoria.service;

import com.sanfrancisco.api.modules.auditoria.dto.request.AuditoriaFilterRequest;
import com.sanfrancisco.api.modules.auditoria.dto.response.RegistroAuditoriaResponse;
import com.sanfrancisco.api.modules.auditoria.entity.RegistroAuditoria;
import com.sanfrancisco.api.modules.auditoria.repository.RegistroAuditoriaRepository;
import com.sanfrancisco.api.modules.auditoria.specification.RegistroAuditoriaSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditoriaServiceImpl implements AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaServiceImpl.class);

    private final RegistroAuditoriaRepository repository;

    public AuditoriaServiceImpl(RegistroAuditoriaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(AuditoriaCommand command) {
        try {
            RegistroAuditoria registro = RegistroAuditoria.builder()
                    .usuarioId(command.usuarioId())
                    .usuarioCorreo(command.usuarioCorreo())
                    .accion(command.accion())
                    .modulo(command.modulo())
                    .descripcion(truncar(command.descripcion(), 255))
                    .metodoHttp(command.metodoHttp())
                    .ruta(truncar(command.ruta(), 255))
                    .ipOrigen(command.ipOrigen())
                    .resultado(command.resultado())
                    .detalleError(truncar(command.detalleError(), 500))
                    .fecha(LocalDateTime.now())
                    .build();
            repository.save(registro);
        } catch (Exception e) {
            // La auditoría nunca debe romper el flujo de negocio.
            log.error("No se pudo persistir el registro de auditoría (accion={}, modulo={}): {}",
                    command.accion(), command.modulo(), e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RegistroAuditoriaResponse> search(AuditoriaFilterRequest filter, Pageable pageable) {
        return repository.findAll(RegistroAuditoriaSpecification.build(filter), pageable)
                .map(this::toResponse);
    }

    private RegistroAuditoriaResponse toResponse(RegistroAuditoria r) {
        return new RegistroAuditoriaResponse(
                r.getRegistroId(),
                r.getUsuarioId(),
                r.getUsuarioCorreo(),
                r.getAccion(),
                r.getModulo(),
                r.getDescripcion(),
                r.getMetodoHttp(),
                r.getRuta(),
                r.getIpOrigen(),
                r.getResultado(),
                r.getDetalleError(),
                r.getFecha()
        );
    }

    private String truncar(String texto, int maxLen) {
        if (texto == null) return null;
        return texto.length() > maxLen ? texto.substring(0, maxLen) : texto;
    }
}
