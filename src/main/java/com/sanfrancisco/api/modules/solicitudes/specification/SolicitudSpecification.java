package com.sanfrancisco.api.modules.solicitudes.specification;

import com.sanfrancisco.api.modules.solicitudes.dto.request.SolicitudFilterRequest;
import com.sanfrancisco.api.modules.solicitudes.entity.Solicitud;
import com.sanfrancisco.api.shared.specification.SpecificationUtils;
import org.springframework.data.jpa.domain.Specification;

public final class SolicitudSpecification {

    private SolicitudSpecification() {
    }

    public static Specification<Solicitud> build(SolicitudFilterRequest filter) {
        if (filter == null) return Specification.unrestricted();

        return Specification.allOf(
                SpecificationUtils.<Solicitud>equalsIfPresent("estado", filter.estado()),
                SpecificationUtils.<Solicitud>equalsIfPresent("tipoSolicitud", filter.tipoSolicitud()),
                SpecificationUtils.<Solicitud>equalsIfPresent("prioridad", filter.prioridad()),
                SpecificationUtils.<Solicitud>equalsIfPresent("moduloReferido", filter.moduloReferido()),
                SpecificationUtils.<Solicitud>equalsIfPresent("solicitante.usuarioId", filter.solicitanteId()),
                SpecificationUtils.<Solicitud>equalsIfPresent("responsable.usuarioId", filter.responsableId()),
                SpecificationUtils.<Solicitud>dateTimeBetween("fechaRegistro", filter.fechaRegistroDesde(), filter.fechaRegistroHasta())
        );
    }

    /**
     * Restringe los resultados a las solicitudes registradas por un usuario concreto.
     * Se combina con {@link #build} cuando el actor no posee el permiso solicitud:read-all.
     */
    public static Specification<Solicitud> ownedBy(Integer solicitanteId) {
        return SpecificationUtils.equalsIfPresent("solicitante.usuarioId", solicitanteId);
    }
}
