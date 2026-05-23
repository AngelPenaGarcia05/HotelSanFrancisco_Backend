package com.sanfrancisco.api.modules.operaciones.specification;

import com.sanfrancisco.api.modules.operaciones.dto.request.IncidenciaFilterRequest;
import com.sanfrancisco.api.modules.operaciones.entity.Incidencia;
import com.sanfrancisco.api.shared.specification.SpecificationUtils;
import org.springframework.data.jpa.domain.Specification;

public final class IncidenciaSpecification {

    private IncidenciaSpecification() {
    }

    public static Specification<Incidencia> build(IncidenciaFilterRequest filter) {
        if (filter == null) return Specification.unrestricted();

        return Specification.allOf(
                SpecificationUtils.<Incidencia>equalsIfPresent("estado", filter.estado()),
                SpecificationUtils.<Incidencia>equalsIfPresent("prioridad", filter.prioridad()),
                SpecificationUtils.<Incidencia>equalsIfPresent("usuario.usuarioId", filter.usuarioId()),
                SpecificationUtils.<Incidencia>equalsIfPresent("reservaHabitacion.reservaHabitacionId", filter.reservaHabitacionId()),
                SpecificationUtils.<Incidencia>dateTimeBetween("fechaReporte", filter.fechaReporteDesde(), filter.fechaReporteHasta())
        );
    }
}
