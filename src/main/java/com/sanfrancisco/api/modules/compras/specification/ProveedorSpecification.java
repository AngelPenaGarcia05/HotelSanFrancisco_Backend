package com.sanfrancisco.api.modules.compras.specification;

import com.sanfrancisco.api.modules.compras.dto.request.ProveedorFilterRequest;
import com.sanfrancisco.api.modules.compras.entity.Proveedor;
import com.sanfrancisco.api.shared.specification.SpecificationUtils;
import org.springframework.data.jpa.domain.Specification;

public final class ProveedorSpecification {

    private ProveedorSpecification() {
    }

    public static Specification<Proveedor> build(ProveedorFilterRequest filter) {
        if (filter == null) return Specification.unrestricted();

        return Specification.allOf(
                SpecificationUtils.<Proveedor>likeIfPresent("rucNitCif", filter.rucNitCif()),
                SpecificationUtils.<Proveedor>likeIfPresent("razonSocial", filter.razonSocial()),
                SpecificationUtils.<Proveedor>likeIfPresent("email", filter.email())
        );
    }
}
