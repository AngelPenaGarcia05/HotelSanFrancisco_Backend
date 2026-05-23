package com.sanfrancisco.api.modules.pagos.specification;

import com.sanfrancisco.api.modules.pagos.dto.request.PagoFilterRequest;
import com.sanfrancisco.api.modules.pagos.entity.Pago;
import com.sanfrancisco.api.shared.specification.SpecificationUtils;
import org.springframework.data.jpa.domain.Specification;

public final class PagoSpecification {

    private PagoSpecification() {
    }

    public static Specification<Pago> build(PagoFilterRequest filter) {
        if (filter == null) return Specification.unrestricted();

        return Specification.allOf(
                SpecificationUtils.<Pago>equalsIfPresent("metodoPago.metodoPagoId", filter.metodoPagoId()),
                SpecificationUtils.<Pago>equalsIfPresent("tipoPago", filter.tipoPago()),
                SpecificationUtils.<Pago>equalsIfPresent("venta.ventaId", filter.ventaId()),
                SpecificationUtils.<Pago>equalsIfPresent("reserva.reservaId", filter.reservaId()),
                SpecificationUtils.<Pago>likeIfPresent("comprobante", filter.comprobante()),
                SpecificationUtils.<Pago>dateTimeBetween("fecha", filter.fechaDesde(), filter.fechaHasta()),
                SpecificationUtils.<Pago, java.math.BigDecimal>greaterOrEqual("monto", filter.montoMin()),
                SpecificationUtils.<Pago, java.math.BigDecimal>lessOrEqual("monto", filter.montoMax())
        );
    }
}
