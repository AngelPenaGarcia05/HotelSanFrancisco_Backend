package com.sanfrancisco.api.modules.recepcion.specification;

import com.sanfrancisco.api.modules.recepcion.dto.request.ClienteFilterRequest;
import com.sanfrancisco.api.modules.recepcion.entity.Huesped;
import com.sanfrancisco.api.shared.specification.SpecificationUtils;
import org.springframework.data.jpa.domain.Specification;

public final class ClienteSpecification {

    private ClienteSpecification() {
    }

    public static Specification<Huesped> build(ClienteFilterRequest filter) {
        if (filter == null) return Specification.unrestricted();

        return Specification.allOf(
                SpecificationUtils.<Huesped>likeIfPresent("nombre", filter.nombre()),
                SpecificationUtils.<Huesped>likeIfPresent("apellidoPaterno", filter.apellidoPaterno()),
                SpecificationUtils.<Huesped>likeIfPresent("numeroDocumento", filter.numeroDocumento()),
                SpecificationUtils.<Huesped>likeIfPresent("nacionalidad", filter.nacionalidad()),
                SpecificationUtils.<Huesped>likeIfPresent("correo", filter.correo()),
                SpecificationUtils.<Huesped>equalsIfPresent("estado", filter.estado())
        );
    }
}
