package com.sanfrancisco.api.modules.auditoria.specification;

import com.sanfrancisco.api.modules.auditoria.dto.request.AuditoriaFilterRequest;
import com.sanfrancisco.api.modules.auditoria.entity.RegistroAuditoria;
import com.sanfrancisco.api.shared.specification.SpecificationUtils;
import org.springframework.data.jpa.domain.Specification;

public final class RegistroAuditoriaSpecification {

    private RegistroAuditoriaSpecification() {
    }

    public static Specification<RegistroAuditoria> build(AuditoriaFilterRequest filter) {
        if (filter == null) return Specification.unrestricted();

        return Specification.allOf(
                SpecificationUtils.<RegistroAuditoria>equalsIfPresent("usuarioId", filter.usuarioId()),
                SpecificationUtils.<RegistroAuditoria>likeIfPresent("usuarioCorreo", filter.usuarioCorreo()),
                SpecificationUtils.<RegistroAuditoria>likeIfPresent("accion", filter.accion()),
                SpecificationUtils.<RegistroAuditoria>likeIfPresent("modulo", filter.modulo()),
                SpecificationUtils.<RegistroAuditoria>equalsIfPresent("resultado", filter.resultado()),
                SpecificationUtils.<RegistroAuditoria>dateTimeInDayRange("fecha", filter.fechaDesde(), filter.fechaHasta())
        );
    }
}
