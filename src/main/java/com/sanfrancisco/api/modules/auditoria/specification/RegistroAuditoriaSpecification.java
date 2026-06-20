package com.sanfrancisco.api.modules.auditoria.specification;

import com.sanfrancisco.api.modules.auditoria.dto.request.AuditoriaFilterRequest;
import com.sanfrancisco.api.modules.auditoria.entity.RegistroAuditoria;
import com.sanfrancisco.api.shared.specification.SpecificationUtils;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.time.LocalTime;

public final class RegistroAuditoriaSpecification {

    private RegistroAuditoriaSpecification() {
    }

    public static Specification<RegistroAuditoria> build(AuditoriaFilterRequest filter) {
        if (filter == null) return Specification.unrestricted();

        LocalDateTime desde = filter.fechaDesde() != null
                ? filter.fechaDesde().atStartOfDay() : null;
        LocalDateTime hasta = filter.fechaHasta() != null
                ? filter.fechaHasta().atTime(LocalTime.MAX) : null;

        return Specification.allOf(
                SpecificationUtils.<RegistroAuditoria>equalsIfPresent("usuarioId", filter.usuarioId()),
                SpecificationUtils.<RegistroAuditoria>likeIfPresent("usuarioCorreo", filter.usuarioCorreo()),
                SpecificationUtils.<RegistroAuditoria>likeIfPresent("accion", filter.accion()),
                SpecificationUtils.<RegistroAuditoria>likeIfPresent("modulo", filter.modulo()),
                SpecificationUtils.<RegistroAuditoria>equalsIfPresent("resultado", filter.resultado()),
                SpecificationUtils.<RegistroAuditoria>dateTimeBetween("fecha", desde, hasta)
        );
    }
}
