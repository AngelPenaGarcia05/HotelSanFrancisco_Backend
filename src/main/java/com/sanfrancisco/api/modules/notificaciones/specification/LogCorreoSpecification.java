package com.sanfrancisco.api.modules.notificaciones.specification;

import com.sanfrancisco.api.modules.notificaciones.dto.request.EmailLogFilterRequest;
import com.sanfrancisco.api.modules.notificaciones.entity.LogCorreo;
import com.sanfrancisco.api.shared.specification.SpecificationUtils;
import org.springframework.data.jpa.domain.Specification;

public final class LogCorreoSpecification {

    private LogCorreoSpecification() {
    }

    public static Specification<LogCorreo> build(EmailLogFilterRequest filter) {
        if (filter == null) return Specification.unrestricted();

        Specification<LogCorreo> spec = Specification.allOf(
                SpecificationUtils.<LogCorreo>equalsIfPresent("estado", filter.estado()),
                SpecificationUtils.<LogCorreo>equalsIfPresent("plantillaClave", filter.plantilla())
        );

        if (filter.search() != null && !filter.search().isBlank()) {
            String pattern = "%" + filter.search().toLowerCase().trim() + "%";
            Specification<LogCorreo> searchSpec = (root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("destinatario")), pattern),
                    cb.like(cb.lower(root.get("asunto")), pattern)
            );
            spec = spec.and(searchSpec);
        }

        return spec;
    }
}

