package com.sanfrancisco.api.modules.compras.specification;

import com.sanfrancisco.api.modules.compras.dto.request.CompraFilterRequest;
import com.sanfrancisco.api.modules.compras.entity.Compra;
import com.sanfrancisco.api.shared.specification.SpecificationUtils;
import org.springframework.data.jpa.domain.Specification;

public final class CompraSpecification {

    private CompraSpecification() {
    }

    public static Specification<Compra> build(CompraFilterRequest filter) {
        if (filter == null) return Specification.unrestricted();

        return Specification.allOf(
                SpecificationUtils.<Compra>likeIfPresent("numeroFactura", filter.numeroFactura()),
                SpecificationUtils.<Compra>equalsIfPresent("estado", filter.estado()),
                SpecificationUtils.<Compra>equalsIfPresent("proveedor.proveedorId", filter.proveedorId()),
                SpecificationUtils.<Compra>dateBetween("fechaCompra", filter.fechaCompraDesde(), filter.fechaCompraHasta()),
                SpecificationUtils.<Compra, java.math.BigDecimal>greaterOrEqual("montoTotal", filter.montoTotalMin()),
                SpecificationUtils.<Compra, java.math.BigDecimal>lessOrEqual("montoTotal", filter.montoTotalMax())
        );
    }
}
