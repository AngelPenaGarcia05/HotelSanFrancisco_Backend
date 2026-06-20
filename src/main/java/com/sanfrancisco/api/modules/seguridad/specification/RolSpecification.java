package com.sanfrancisco.api.modules.seguridad.specification;

import com.sanfrancisco.api.modules.seguridad.dto.request.RolFilterRequest;
import com.sanfrancisco.api.modules.seguridad.entity.Rol;
import com.sanfrancisco.api.shared.specification.SpecificationUtils;
import org.springframework.data.jpa.domain.Specification;

public final class RolSpecification {

    private RolSpecification() {
    }

    public static Specification<Rol> build(RolFilterRequest filter) {
        if (filter == null) return Specification.unrestricted();

        return Specification.allOf(
                SpecificationUtils.<Rol>likeIfPresent("nombre", filter.nombre()),
                SpecificationUtils.<Rol>equalsIfPresent("estado", filter.estado())
        );
    }
}
