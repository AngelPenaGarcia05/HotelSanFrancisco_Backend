package com.sanfrancisco.api.modules.ventas.specification;

import com.sanfrancisco.api.modules.ventas.dto.request.VentaFilterRequest;
import com.sanfrancisco.api.modules.ventas.entity.Venta;
import com.sanfrancisco.api.shared.specification.SpecificationUtils;
import org.springframework.data.jpa.domain.Specification;

public final class VentaSpecification {

    private VentaSpecification() {
    }

    public static Specification<Venta> build(VentaFilterRequest filter) {
        if (filter == null) return Specification.unrestricted();

        return Specification.allOf(
                SpecificationUtils.<Venta>likeIfPresent("codigoVenta", filter.codigoVenta()),
                SpecificationUtils.<Venta>equalsIfPresent("estado", filter.estado()),
                SpecificationUtils.<Venta>equalsIfPresent("tipoVenta", filter.tipoVenta()),
                SpecificationUtils.<Venta>equalsIfPresent("usuario.usuarioId", filter.usuarioId()),
                SpecificationUtils.<Venta>equalsIfPresent("estancia.estanciaId", filter.estanciaId()),
                SpecificationUtils.<Venta>equalsIfPresent("huesped.huespedId", filter.huespedId()),
                SpecificationUtils.<Venta>dateTimeBetween("fechaVenta", filter.fechaVentaDesde(), filter.fechaVentaHasta()),
                SpecificationUtils.<Venta, java.math.BigDecimal>greaterOrEqual("montoTotal", filter.montoTotalMin()),
                SpecificationUtils.<Venta, java.math.BigDecimal>lessOrEqual("montoTotal", filter.montoTotalMax())
        );
    }
}
