package com.sanfrancisco.api.modules.recepcion.specification;

import com.sanfrancisco.api.modules.recepcion.dto.request.ReservaFilterRequest;
import com.sanfrancisco.api.modules.recepcion.entity.Reserva;
import com.sanfrancisco.api.shared.specification.SpecificationUtils;
import org.springframework.data.jpa.domain.Specification;

public final class ReservaSpecification {

    private ReservaSpecification() {
    }

    public static Specification<Reserva> build(ReservaFilterRequest filter) {
        if (filter == null) return Specification.unrestricted();

        return Specification.allOf(
                SpecificationUtils.<Reserva>likeIfPresent("codReserva", filter.codReserva()),
                SpecificationUtils.<Reserva>equalsIfPresent("estado", filter.estado()),
                SpecificationUtils.<Reserva>equalsIfPresent("usuario.usuarioId", filter.usuarioId()),
                SpecificationUtils.<Reserva>equalsIfPresent("canal.canalId", filter.canalId()),
                SpecificationUtils.<Reserva>dateBetween("fechaInicio", filter.fechaInicioDesde(), filter.fechaInicioHasta()),
                SpecificationUtils.<Reserva>dateBetween("fechaFin", filter.fechaFinDesde(), filter.fechaFinHasta()),
                SpecificationUtils.<Reserva, java.math.BigDecimal>greaterOrEqual("montoTotal", filter.montoTotalMin()),
                SpecificationUtils.<Reserva, java.math.BigDecimal>lessOrEqual("montoTotal", filter.montoTotalMax())
        );
    }
}
