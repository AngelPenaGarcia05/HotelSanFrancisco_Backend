package com.sanfrancisco.api.modules.servicios.specification;

import com.sanfrancisco.api.modules.servicios.dto.request.ServicioFilterRequest;
import com.sanfrancisco.api.modules.servicios.entity.Servicio;
import com.sanfrancisco.api.shared.specification.SpecificationUtils;
import org.springframework.data.jpa.domain.Specification;

public final class ServicioSpecification {

    private ServicioSpecification() {
    }

    public static Specification<Servicio> build(ServicioFilterRequest filter) {
        if (filter == null) return Specification.unrestricted();

        return Specification.allOf(
                SpecificationUtils.<Servicio>equalsIfPresent("tipoServicio.tipoServicioId", filter.tipoServicioId()),
                SpecificationUtils.<Servicio>equalsIfPresent("estancia.estanciaId", filter.estanciaId()),
                SpecificationUtils.<Servicio>dateTimeBetween("fechaConsumo", filter.fechaConsumoDesde(), filter.fechaConsumoHasta()),
                SpecificationUtils.<Servicio, java.math.BigDecimal>greaterOrEqual("subtotal", filter.subtotalMin()),
                SpecificationUtils.<Servicio, java.math.BigDecimal>lessOrEqual("subtotal", filter.subtotalMax())
        );
    }
}
